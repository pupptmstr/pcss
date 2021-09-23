package com.monkeys.pcss.client

import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.printHelp
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException

class Client (host: String, port: Int) {

    private var socket: Socket = Socket(host, port)
    private val receiver = socket.getInputStream()
    private val sender = socket.getOutputStream()
    private lateinit var name: String

    fun start() {
        println("Был запущен клиент")
        printHelp()
        var nameExist = true
        var stillWorking = true

        println("Enter your nickname or \'q\' to exit.")
        val userInput = readLine()
        if (userInput == null) {
            stillWorking = false
        } else if (userInput == "q") {
            sender.write("EXIT".toByteArray())
            stillWorking = false
        } else {

            val data = Data(null, userInput, "","", null)
            val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
            val message = Message(header, data, ByteArray(0))

            sender.write(message.getMessage().toByteArray())
            var serverMessage : String?
            val byteArray = ByteArray(100000)
            while (true) {
                if (receiver.available() > 0) {
                    receiver.read(byteArray, 0, receiver.available()).toString()
                    serverMessage = String(byteArray).replace("\u0000", "")
                    println(serverMessage)
                    val parseServerMessage = parseMessage(serverMessage)
                    println(parseServerMessage)
                    val messageInfo = parseServerMessage.data.messageText
                    println(messageInfo)
                    val type = parseServerMessage.header.type
                    if (messageInfo == "Name is taken, please try to connect again"
                        && type == MessageType.LOGIN) {
                        stillWorking = false
                        break
                    } else  if (messageInfo == "ok" && type == MessageType.LOGIN) {
                        name = userInput
                        nameExist = false
                        break
                    }
            }

        }
        }
        if (nameExist) {
            stopConnection()
        }

        while (stillWorking) {
            print("ok")
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