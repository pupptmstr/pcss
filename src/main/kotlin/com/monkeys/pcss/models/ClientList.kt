package com.monkeys.pcss.models

import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class ClientList() {
    private val clients = mutableMapOf<String, Pair<InputStream, OutputStream>>()
    private val socketList = mutableMapOf<String, Socket>()

    fun addNewClient(socket: Socket, newId: String) {
        if (clients.keys.contains(newId)) {
            val data = Data(senderName = "server", messageText = "Name is taken, please try to connect again")
            val header = Header(MessageType.LOGIN, false, data.getServerMessage().length)
            socket.getOutputStream().write(Message(header, data).getMessage().toByteArray())
        } else {
            clients[newId] = Pair(socket.getInputStream(), socket.getOutputStream())
            socketList[newId] = socket
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

    fun writeToEveryBody(message: String) {
    }
}