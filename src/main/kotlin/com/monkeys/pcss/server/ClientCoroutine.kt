package com.monkeys.pcss.server

import com.monkeys.pcss.models.ClientList
import java.net.Socket

fun clientCoroutine(client: Socket, clientList: ClientList) {
    println("Receiving new client name...")
    val receiver = client.getInputStream()
    val sender = client.getOutputStream()
    val byteArray = ByteArray(100000)
    var name = ""
    while (true) {
        if (receiver.available() > 0) {
            receiver.read(byteArray, 0, receiver.available()).toString()
            name = String(byteArray).replace("\u0000", "")
            println("Client name is $name")
            clientList.addNewClient(client, name)
        }
    }


}