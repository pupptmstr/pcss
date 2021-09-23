package com.monkeys.pcss.client

import com.monkeys.pcss.BYTE_ARRAY
import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.printHelp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import java.net.SocketException

class Client (host: String, port: Int) {

    private var socket: Socket = Socket(host, port)
    private val receiver = socket.getInputStream()
    private val sender = socket.getOutputStream()
    private lateinit var name: String
    private var stillWorking = true

    suspend fun start() = coroutineScope {
        println("Был запущен клиент")
        printHelp()
        var nameExist = true

        println("Enter your nickname or \'q\' to exit.")
        when (val userInput = readLine()) {
            null -> {
                stillWorking = false
            }
            "q" -> {
                sender.write("EXIT".toByteArray())
                stillWorking = false
            }
            else -> {

                val data = Data(null, userInput, "","", null)
                val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
                val message = Message(header, data, ByteArray(0))

                sender.write(message.getMessage().toByteArray())
                var messageInfo = ""
                while (nameExist) {
                    if (receiver.available() > 0) {
                        receiver.read(BYTE_ARRAY, 0, receiver.available()).toString()
                        val serverMessage = String(BYTE_ARRAY).replace("\u0000", "")
                        println(serverMessage)
                        val parseServerMessage = parseMessage(serverMessage)
                        messageInfo = parseServerMessage.data.messageText
                        val type = parseServerMessage.header.type
                        val senderName = parseServerMessage.data.senderName
                        if (messageInfo == "Name is taken, please try to connect again"
                            && type == MessageType.LOGIN && senderName == "server") {
                            stillWorking = false
                            nameExist = false
                        } else {
                            name = userInput
                            nameExist = false
                        }
                    }
                }
                println(messageInfo)
                println("You can attach a picture by writing such a construction at the end of the message [[filepath]]")
            }
        }
        if (nameExist) {
            stopConnection()
        }

        launch(Dispatchers.Default) { sendingMessages() }
        launch(Dispatchers.Default) { receivingMessages() }
    }

    private fun stopConnection() {
        try {
            receiver.close()
            sender.close()
            socket.close()
            println("Bye!")
        } catch (e: SocketException) {
            println("ERROR! Socket wasn't closed!")
            e.printStackTrace()
        }
    }

    private fun sendingMessages() {
        while (stillWorking) {
            val userMessage = readLine()
            if (userMessage == "q") {
                sender.write("EXIT".toByteArray())
               stillWorking = false
            } else {
                val parsedMsg = parseUserMessage(userMessage.toString())
                val msg = parsedMsg.first
                val file = parsedMsg.second
                val fileName = file?.name
                val fileByteArray = file?.readBytes()
                val byteArraySize = fileByteArray?.size ?: 0
                val data = Data(null, name, "", msg, fileName)
                val header = Header(MessageType.MESSAGE, file != null,
                    data.getServerMessage().length + byteArraySize)
                val message = Message(header, data, fileByteArray ?: ByteArray(0))

                sender.write(message.getMessage().toByteArray())

                var messageDelivered = false

                while (!messageDelivered) {
                    if (receiver.available() > 0) {
                        receiver.read(BYTE_ARRAY, 0, receiver.available()).toString()
                        val serverMessage = String(BYTE_ARRAY).replace("\u0000", "")
                        println(serverMessage)
                       val parseServerMessage = parseMessage(serverMessage)
                       val messageInfo = parseServerMessage.data.messageText
                       val type = parseServerMessage.header.type
                       val senderName = parseServerMessage.data.senderName
                       messageDelivered = if (messageInfo == "1" && type == MessageType.SPECIAL
                           && senderName == "server") {
                           true
                       } else {
                           println("not del")
                           true
                       }
                    }
                }
            }
        }
    }

    private fun receivingMessages() {
        while (stillWorking) {
            if (receiver.available() > 0) {
                receiver.read(BYTE_ARRAY, 0, receiver.available()).toString()
                val serverMessage = String(BYTE_ARRAY).replace("\u0000", "")
                println(serverMessage)
                val parseServerMessage = parseMessage(serverMessage)
                val senderName = parseServerMessage.data.senderName
                val time = parseServerMessage.data.time
                val message = parseServerMessage.data.messageText
                val fileName = parseServerMessage.data.fileName

                if (fileName != null) {
                    val fileByteArray = parseServerMessage.file
                    val file1 = File(fileName)
                    file1.createNewFile()
                    file1.writeBytes(fileByteArray)
                }

                //TODO отображение сообщения в консоли

                val id = parseServerMessage.data.messageId
                val dataSpec = Data(id, name, "","1", null)
                val headerSpec = Header(MessageType.SPECIAL, false, dataSpec.getServerMessage().length)
                val messageSpec = Message(headerSpec, dataSpec, ByteArray(0))
                sender.write(messageSpec.getMessage().toByteArray())

            }
        }
    }
}