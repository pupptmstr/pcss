package com.monkeys.pcss.client

import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.printHelp
import com.monkeys.pcss.readFromInputSteam
import com.monkeys.pcss.substring
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
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

                sender.write(message.getMessage())
                var messageInfo = ""
                while (nameExist) {
                    if (receiver.available() > 0) {
                        val serverMessage = readFromInputSteam(receiver).first
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
            when (val userMessage = readLine()) {
                "" -> continue
                "q" -> {
                    sender.write("EXIT".toByteArray())
                    stillWorking = false
                }
                else -> {
                    val parsedMsg = parseUserMessage(userMessage.toString())
                    val msg = parsedMsg.first
                    val file = parsedMsg.second
                    val fileName = file?.name
                    val fileByteArray = file?.readBytes()
                    val data = Data(null, name, "", msg, fileName)
                    val header = Header(MessageType.MESSAGE, file != null,
                        data.getServerMessage().toByteArray().size)
                    val message = Message(header, data, fileByteArray ?: ByteArray(0))
                    val messageRes = message.getMessage()
                    sender.write(messageRes)
                }
            }
        }
    }

    private fun receivingMessages() {
        while (stillWorking) {
            if (receiver.available() > 0) {
                val inputStreamStr = readFromInputSteam(receiver)
                val serverMessage = inputStreamStr.first
                println(serverMessage)
                val parsedServerMessage = parseMessage(serverMessage)
                val senderName = parsedServerMessage.data.senderName
                val time = parsedServerMessage.data.time
                val message = parsedServerMessage.data.messageText
                val fileName = parsedServerMessage.data.fileName

                val fileByteArray = substring(inputStreamStr.second,
                    parsedServerMessage.header.getHeader().toByteArray().size + parsedServerMessage.header.dataSize)

                if (fileName != null && fileByteArray != null) {
                    val file1 = File(fileName)
                    file1.createNewFile()
                    file1.writeBytes(fileByteArray)
                }

                println(parsedServerMessage.data.getClientMessage())

                val id = parsedServerMessage.data.messageId
                val dataSpec = Data(id, name, "","1", null)
                val headerSpec = Header(MessageType.SPECIAL, false, dataSpec.getServerMessage().length)
                val messageSpec = Message(headerSpec, dataSpec, ByteArray(0))
                sender.write(messageSpec.getMessage())

            }
        }
    }
}