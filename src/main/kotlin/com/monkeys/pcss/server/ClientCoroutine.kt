package com.monkeys.pcss.server

import com.monkeys.pcss.generateMessageId
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.readMessageFromInputStream
import java.net.Socket
import java.time.ZonedDateTime


suspend fun clientCoroutine(client: Socket, clientList: ClientList) {
    val loginRes = login(client, clientList)
    if (loginRes.first) {
        startCommunication(loginRes.second, clientList)
    } else {
        client.close()
    }
}

fun login(client: Socket, clientList: ClientList): Pair<Boolean, String> {
    try {
        val receiver = client.getInputStream()
        var name = ""
        var isSuccessfullyLogin = false
        while (true) {
            if (receiver.available() > 0) {
                val message = readMessageFromInputStream(receiver)
                val parsedMessage = parseMessage(message)
                name = parsedMessage!!.data.senderName
                isSuccessfullyLogin = clientList.addNewClient(client, name)
                if (isSuccessfullyLogin) {
                    val data = Data(null, name, "", "Client $name connected to chat", null)
                    val header = Header(MessageType.SPECIAL, false, 0)
                    clientList.writeToEveryBody(Message(header, data), ByteArray(0))
                }
                break
            }
        }
        return Pair(isSuccessfullyLogin, name)
    } catch (e: Exception) {
        println("!E: Client connection was closed! He will come later probably?")
        return Pair(false, "server")
    }
}

fun startCommunication(clientId: String, clientList: ClientList) {
    try {
        println("Client $clientId connected to chat")
        var isWorking = true
        val receiver = clientList.getInputStream(clientId)
        while (isWorking) {
            if (receiver.available() > 0) {

                val message = readMessageFromInputStream(receiver)
                val parsedMessage = parseMessage(message)

                if (parsedMessage != null) {
                    val size = parsedMessage.header.fileSize
                    val byteArray = ByteArray(size)
                    if (parsedMessage.header.isFileAttached) {
                        receiver.readNBytes(byteArray, 0, byteArray.size)
                        println(byteArray.size)
                    }

                    if (parsedMessage.header.type == MessageType.MESSAGE) {
                        val messageId = generateMessageId()
                        val time = ZonedDateTime.now().toString().replace("[", "{").replace("]", "}")
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
                        println(
                            "Got message with type '${parsedMessage.header.type}' and text " +
                                    "'${parsedMessage.data.messageText}' from '${parsedMessage.data.senderName}'"
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        println("!E: Connection with client was closed! Deleting him from clients list...")
        clientList.finishConnection(clientId)
    }
}

