package com.monkeys.pcss.models

import com.monkeys.pcss.DOWNLOADS_DIR
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import com.monkeys.pcss.sendMessage
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.net.Socket
import java.util.*

class ClientList() {
    private val clients = Collections.synchronizedMap(mutableMapOf<String,
            Pair<BufferedInputStream, BufferedOutputStream>>())
    private val socketList = Collections.synchronizedMap(mutableMapOf<String, Socket>())

    fun addNewClient(socket: Socket, newId: String): Boolean {
        return if (clients.keys.contains(newId) || newId == "server") {
            val data = Data(senderName = "server", messageText =
            "Name is taken, please try to connect again")
            val header = Header(MessageType.LOGIN, false, 0)
            sendMessage(socket.getOutputStream(), Message(header, data).getMessage(), null)
            false
        } else {
            val downloadDir = File(DOWNLOADS_DIR)
            if (!downloadDir.exists())
                downloadDir.mkdir()
            clients[newId] = Pair(BufferedInputStream(socket.getInputStream()),
                BufferedOutputStream(socket.getOutputStream()))
            socketList[newId] = socket
            val data = Data(senderName = "server", messageText =
            "Great, your name now is $newId, you can communicate. There are ${clients.size - 1} people in the chat excepts you.")
            val header = Header(MessageType.LOGIN, false, 0)
            sendMessage(socket.getOutputStream(), Message(header, data).getMessage(), null)
            true
        }
    }

    fun finishConnection(id: String) {
        clients.remove(id)
        socketList[id]!!.close()
        socketList.remove(id)
        val data = Data(null, id, "", "Client $id disconnected from chat", null)
        val header = Header(MessageType.SPECIAL, false, 0)
        writeToEveryBody(Message(header, data), ByteArray(0))
    }

    fun getInputStream(id: String): BufferedInputStream {
        return clients[id]!!.first
    }

    fun getOutputStream(id: String): OutputStream {
        return clients[id]!!.second
    }

    fun getSocket(id: String): Socket {
        return socketList[id]!!
    }

    fun writeToEveryBody(message: Message, fileByteArray: ByteArray) {
        val name = message.data.senderName
        val names = mutableListOf<String>()
        clients.forEach { client ->
            try {
                if (client.key != name) {
                    val sender = client.value.second
                    sendMessage(sender, message.getMessage(), fileByteArray)
                    names.add(client.key)
                }
            } catch (e: Exception) {
                println("!E: Connection with client ${client.key} was closed!")
                e.printStackTrace()
                names.remove(client.key)
                finishConnection(client.key)
            }
        }
        println("Message '${String(message.getMessage())}' was send to this clients: $names")
    }
}