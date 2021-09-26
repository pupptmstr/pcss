package com.monkeys.pcss.models

import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.*

class ClientList() {
    private val clients = Collections.synchronizedMap(mutableMapOf<String, Pair<InputStream, OutputStream>>())
    private val socketList = Collections.synchronizedMap(mutableMapOf<String, Socket>())

    fun addNewClient(socket: Socket, newId: String): Boolean {
        val sender = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        return if (clients.keys.contains(newId)) {
            val data = Data(senderName = "server", messageText = "Name is taken, please try to connect again")
            val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
            sender.write(Message(header, data).getMessage())
            sender.flush()
            false
        } else {
            clients[newId] = Pair(socket.getInputStream(), socket.getOutputStream())
            socketList[newId] = socket
            val data = Data(senderName = "server", messageText = "Great, your name now is $newId, you can communicate")
            val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
            sender.write(Message(header, data).getMessage())
            sender.flush()
            true
        }
    }

    fun finishConnection(id: String) {
        clients.remove(id)
        socketList[id]!!.close()
        socketList.remove(id)
    }

    fun getInputStream(id: String): InputStream {
        return clients[id]!!.first
    }

    fun getOutputStream(id: String): OutputStream {
        return clients[id]!!.second
    }

    fun getSocket(id: String): Socket {
        return socketList[id]!!
    }

    fun writeToEveryBody(message: Message) {
        val name = message.data.senderName
        clients.forEach { client ->
            try {
                if (client.key != name) {
                    val sender = BufferedWriter(OutputStreamWriter(client.value.second))
                    sender.write(message.getMessage())
                    sender.flush()
                }
            } catch (e: Exception) {
                println("Connection with client ${client.key} was closed!")
                e.printStackTrace()
            }
        }
        //println("text is $message")
    }
}