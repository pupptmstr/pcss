package com.monkeys.pcss.client

import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.printHelp
import com.monkeys.pcss.shapingFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import java.net.SocketException

class Client(host: String, port: Int) {

    private var socket = Socket(host, port)
    private var receiver = BufferedReader(InputStreamReader(socket.getInputStream()))
    private var sender = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
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
                sender.write("EXIT")
                sender.flush()
                stillWorking = false
            }
            else -> {

                val data = Data(null, userInput, "", "", null)
                val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
                val message = Message(header, data, ByteArray(0))

                sender.write(message.getMessage())
                sender.flush()
                println("send message with name ${message.getMessage()}")
                var messageInfo = ""
                while (nameExist) {
                    val serverMessage = receiver.readLine()
                    println(serverMessage)
                    val parseServerMessage = parseMessage(serverMessage)
                    messageInfo = parseServerMessage.data.messageText
                    val type = parseServerMessage.header.type
                    val senderName = parseServerMessage.data.senderName
                    if (messageInfo == "Name is taken, please try to connect again"
                        && type == MessageType.LOGIN && senderName == "server"
                    ) {
                        stillWorking = false
                        nameExist = false
                    } else {
                        name = userInput
                        nameExist = false
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
                    sender.write("EXIT")
                    stillWorking = false
                }
                else -> {
                    val parsedMsg = parseUserMessage(userMessage.toString())
                    val msg = parsedMsg.first
                    val file = parsedMsg.second
                    val fileName = file?.name
                    val fileByteArray = file?.readBytes()

                    println(fileByteArray?.size ?: "no file")
                    println(fileByteArray?.get(0) ?: "1 byte")

                    val data = Data(null, name, "", msg, fileName)
                    val header = Header(
                        MessageType.MESSAGE, file != null,
                        data.getServerMessage().toByteArray(Charsets.UTF_8).size
                    )
                    val message = Message(header, data, fileByteArray ?: ByteArray(0))
                    val messageRes = message.getMessage()
                    sender.write(messageRes)
                    sender.flush()
                }
            }
        }
    }

    private fun receivingMessages() {
        println("receivingMessages()")
        while (stillWorking) {
            val serverMessage = receiver.readLine()
            val message = parseMessage(serverMessage)
            val fileName = message.data.fileName
            val time = message.data.time
            val senderName = message.data.senderName
            val fileByteArray = message.file

            if (!fileName.isNullOrEmpty() && fileByteArray.isNotEmpty()) {
                val file1 = File(shapingFileName(fileName,senderName,time))
                file1.createNewFile()
                file1.writeBytes(fileByteArray)
            }

            println(message.data.getClientMessage())
        }
    }
}