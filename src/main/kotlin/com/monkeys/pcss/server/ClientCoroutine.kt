package com.monkeys.pcss.server

import com.monkeys.pcss.generateMessageId
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.readMessageFromInputSteam
import kotlinx.coroutines.delay
import java.net.Socket
import java.time.LocalTime
import java.time.ZonedDateTime
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
            val message = readMessageFromInputSteam(receiver)
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
    while (isWorking) {
        if (receiver.available() > 0) {

            val message = readMessageFromInputSteam(receiver)
            val parsedMessage = parseMessage(message)

            val size = parsedMessage.header.fileSize
            val byteArray = ByteArray(size)
            if (parsedMessage.header.isFileAttached) {
                receiver.read(byteArray)
                println(byteArray.size)
            }

            if (parsedMessage.header.type == MessageType.MESSAGE) {
                val messageId = generateMessageId()
                val time = ZonedDateTime.now().toString().replace("[","{").replace("]","}")
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
                        byteArray.size
                    ),
                    data
                )
                clientList.writeToEveryBody(resMessage, byteArray)
            } else if (parsedMessage.data.messageText == "EXIT") {
                clientList.finishConnection(parsedMessage.data.senderName)
                isWorking = false
            } else {
                println("Got message with type '${parsedMessage.header.type}' and text " +
                        "'${parsedMessage.data.messageText}' from '${parsedMessage.data.senderName}'")
            }
        }
    }
}

