package com.monkeys.pcss.server

import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.ServerSocket
import java.net.SocketException

class Server {

    fun start() = runBlocking {
        try {
            val clientList = ClientList()
            ServerSocket(8081).use { server ->
                println("Server is running")
                while (true) {
                    val client = server.accept()
                    if (client.isConnected) {
                        println("пришел новый клиент")
                        launch(Dispatchers.Default) {
                            clientCoroutine()
                        }
                        println("я после запуска корутины")
                    }
                }
            }

        } catch (e: SocketException) {
            println("Error: server was not closed or there was another problem")
            e.printStackTrace()
        }
        println("запущен сервер")
        println(Data("[test],[pupptmstr],[04:20],[Привет, гляньте котика],[];").getServerMessage())
    }

}