package com.monkeys.pcss.server

import com.monkeys.pcss.generateMessageId
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.readFromInputSteam
import com.monkeys.pcss.substring
import kotlinx.coroutines.delay
import java.net.Socket
import java.time.LocalTime
import java.time.format.DateTimeFormatter


suspend fun clientCoroutine(client: Socket, clientList: ClientList) {
    val loginRes = login(client, clientList)
    if (loginRes.first) {
        startCommunication(loginRes.second, clientList)
    } else {
        delay(5000)
        client.close()
    }
}

fun login(client: Socket, clientList: ClientList): Pair<Boolean, String> {
    println("Receiving new client name...")
    val receiver = client.getInputStream()
    var name = ""
    var isSuccessfullyLogin = false
    while (true) {
        if (receiver.available() > 0) {
            val message = readFromInputSteam(receiver)
            val parsedMessage = parseMessage(message.first)
            name = parsedMessage.data.senderName
            println("Client name is $name")
            isSuccessfullyLogin = clientList.addNewClient(client, name)
            break
        }
    }
    return Pair(isSuccessfullyLogin, name)
}

fun startCommunication(clientId: String, clientList: ClientList) {
    println("Client $clientId connected to chat")
    var isWorking = true
    val receiver = clientList.getInputStream(clientId)
    while (isWorking) {
        if (receiver.available() > 0) {
            val message = readFromInputSteam(receiver)
            val parsedMessage = parseMessage(message.first)
            if (parsedMessage.header.type == MessageType.MESSAGE) {
                val messageId = generateMessageId()
                val now = LocalTime.now()
                val time = now.format(DateTimeFormatter.ofPattern("HH:mm"))
                val file = substring(message.second, parsedMessage.header.getHeader().toByteArray().size + parsedMessage.header.dataSize)
                val data = Data(
                    messageId,
                    parsedMessage.data.senderName,
                    time,
                    parsedMessage.data.messageText,
                    parsedMessage.data.fileName
                )
                val resMessage = Message(
                    Header(
                        MessageType.MESSAGE,
                        parsedMessage.header.isFileAttached,
                        data.getServerMessage().toByteArray().size
                        ),
                    data,
                    file ?: ByteArray(0)
                )
                clientList.writeToEveryBody(resMessage)
            } else if (parsedMessage.data.messageText == "EXIT") {
                clientList.finishConnection(parsedMessage.data.senderName)
                isWorking = false
            } else {
                println("Got message with type '${parsedMessage.header.type}' and text '${parsedMessage.data.messageText}' from '${parsedMessage.data.senderName}'")
            }
        }
    }
}
