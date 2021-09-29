package com.monkeys.pcss.client

import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.readMessageFromInputSteam
import com.monkeys.pcss.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.Socket
import java.net.SocketException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Client (host: String, port: Int) {

    private var socket: Socket = Socket(host, port)
    private val receiver = BufferedInputStream(socket.getInputStream())
    private val sender = BufferedOutputStream(socket.getOutputStream())
    private lateinit var name: String
    private var stillWorking = true

    suspend fun start() = coroutineScope {

        var nameExist = true

        println("Enter your nickname or \'q\' to exit.")

        when (val userInput = readLine()) {
            null -> {
                stillWorking = false
            }
            "q" -> {
                send(sender, "EXIT".toByteArray())
                stillWorking = false
            }
            else -> {

                val data = Data(null, userInput, "","", null)
                val header = Header(MessageType.LOGIN, false, 0)
                val message = Message(header, data)

                send(sender, message.getMessage())

                var messageInfo = ""

                while (nameExist) {
                    if (receiver.available() > 0) {

                        val serverMessage = readMessageFromInputSteam(receiver)
                        val parsedServerMessage = parseMessage(serverMessage)
                        messageInfo = parsedServerMessage.data.messageText
                        val type = parsedServerMessage.header.type
                        val senderName = parsedServerMessage.data.senderName
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

        launch(Dispatchers.IO) { sendingMessages() }
        launch(Dispatchers.IO) { receivingMessages() }
    }

    private fun sendingMessages() {
        while (stillWorking) {
            when (val userMessage = readLine()) {
                "" -> continue
                "q" -> {
                    send(sender, "EXIT".toByteArray())
                    stillWorking = false
                }
                else -> {
                    val parsedMessage = parseUserMessage(userMessage.toString())
                    val msg = parsedMessage.first
                    val file = parsedMessage.second
                    val fileName = file?.name
                    val fileByteArray = file?.readBytes()

                    val data = Data(null, name, "", msg, fileName)
                    val header = Header(MessageType.MESSAGE, file != null,
                        fileByteArray?.size ?: 0)
                    val message = Message(header, data)

                    send(sender, message.getMessage())

                    if (header.isFileAttached) {
                        send(sender, fileByteArray ?: ByteArray(0))
                    }
                }
            }
        }
    }

    private fun receivingMessages() {
        while (stillWorking) {
            if (receiver.available() > 0) {

                val serverMessage = readMessageFromInputSteam(receiver)
                val parsedServerMessage = parseMessage(serverMessage)

                val serverData = parsedServerMessage.data

                val serverZoneDateTime = serverData.time.replace("{","[").replace("}","]")
                val id = TimeZone.getDefault().id
                val parsedSZDT = ZonedDateTime.parse(serverZoneDateTime)
                val clientSZDT = parsedSZDT.withZoneSameInstant(ZoneId.of(id)).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

                val finalData = Data(serverData.messageId, serverData.senderName,
                    clientSZDT, serverData.messageText, serverData.fileName)

                println(finalData.getClientMessage())

                val size = parsedServerMessage.header.fileSize
                val byteArray = ByteArray(size)
                if (parsedServerMessage.header.isFileAttached) {
                    receiver.read(byteArray)
                }

                val fileName = parsedServerMessage.data.fileName


                if (fileName != null) {
                    val file1 = File(fileName)
                    file1.createNewFile()
                    file1.writeBytes(byteArray)
                }
            }
        }
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
}