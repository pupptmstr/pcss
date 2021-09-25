package com.monkeys.pcss.server

import com.monkeys.pcss.BYTE_ARRAY_SIZE
import com.monkeys.pcss.generateMessageId
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.*
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
    val sender = client.getOutputStream()
    val byteArray = ByteArray(BYTE_ARRAY_SIZE)
    var name = ""
    var isSuccessfullyLogin = false
    while (true) {
        if (receiver.available() > 0) {
            receiver.read(byteArray, 0, receiver.available()).toString()
            val message = String(byteArray).replace("\u0000", "")
            val parsedMessage = parseMessage(message)
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
    var byteArray = ByteArray(BYTE_ARRAY_SIZE)
    while (isWorking) {
        if (receiver.available() > 0) {
            receiver.read(byteArray, 0, receiver.available()).toString()
            val message = String(byteArray).replace("\u0000", "")
            val parsedMessage = parseMessage(message)
            if (parsedMessage.header.type == MessageType.MESSAGE) {
                val messageId = generateMessageId()
                val now = LocalTime.now()
                val time = now.format(DateTimeFormatter.ofPattern("HH:mm"))
                val resMessage = Message(
                    Header(
                        MessageType.MESSAGE,
                        parsedMessage.header.isFileAttached,
                        message.length
                        ),
                    Data(
                        messageId,
                        parsedMessage.data.senderName,
                        time,
                        parsedMessage.data.messageText,
                        parsedMessage.data.fileName
                    ),
                    parsedMessage.file
                )
                clientList.writeToEveryBody(resMessage)
            } else if (parsedMessage.data.messageText == "EXIT") {
                clientList.finishConnection(parsedMessage.data.senderName)
                isWorking = false
            } else {
                println("Got message with type '${parsedMessage.header.type}' and text '${parsedMessage.data.messageText}' from '${parsedMessage.data.senderName}'")
            }
        }
        byteArray = ByteArray(BYTE_ARRAY_SIZE)
    }
}
