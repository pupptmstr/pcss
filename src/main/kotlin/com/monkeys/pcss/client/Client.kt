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
    private var sender: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private var receiver: BufferedWriter = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    private lateinit var name: String

    fun start() {
        println("Был запущен клиент")
        printHelp()
        var nameExist = true
        var stillWorking = true

        println("Enter your nickname or \'q\' to exit.")
        val userInput = readLine()
        print("userInput")
        if (userInput == null) {
            stillWorking = false
        } else if (userInput == "q") {
            receiver.write("EXIT")
            stillWorking = false
        } else {

            val data = Data(null, userInput, "","", null)
            val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
            val message = Message(header, data, ByteArray(0))

            receiver.write(message.getMessage())
            receiver.flush()

            val serverMessage : String?
            while (true) {
                if (socket.isConnected) {
                    serverMessage = sender.readLine()
                    val parseServerMessage = parseMessage(serverMessage)
                    val messageInfo = parseServerMessage.data.messageText
                    val type = parseServerMessage.header.type
                    if (messageInfo == "Name is taken, please try to connect again"
                        && type == MessageType.LOGIN) {
                        stillWorking = false
                        break
                    } else {
                        name = userInput
                        nameExist = false
                        break
                    }
                }
            }
            println("$serverMessage")
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