package com.monkeys.pcss.models

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class ClientList() {
    private val clients = mutableMapOf<String, Pair<InputStream, OutputStream>>()
    private val socketList = mutableMapOf<String, Socket>()

    fun addNewClient(socket: Socket, newId: String) {
        if (clients.keys.contains(newId)) {
            socket.getOutputStream().write("Этот ник уже занят, возьмите другой".toByteArray())
        } else {
            clients[newId] = Pair(socket.getInputStream(), socket.getOutputStream())
            socketList[newId] = socket
            //ClientThread(newId, this) - вместо треда запускать новую корутину
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