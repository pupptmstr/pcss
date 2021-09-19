package com.monkeys.pcss.client

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
        if (userInput == null) {
            stillWorking = false
        } else if (userInput == "q") {
            receiver.write("EXIT")
            stillWorking = false
        } else {
            receiver.write(userInput)
            val serverMessage : String?
            while (true) {
                if (socket.isConnected) {
                    serverMessage = sender.readLine()
                    if (serverMessage == "Name is taken, please try to connect again") {
                        stillWorking = false
                        nameExist = false
                        break
                    } else {
                        name = userInput
                        break
                    }
                }
            }
            println("$serverMessage")
        }
        if (nameExist)
            stopConnection()
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