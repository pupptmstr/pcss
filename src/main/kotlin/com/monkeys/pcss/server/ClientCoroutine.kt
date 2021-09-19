package com.monkeys.pcss.server

import com.monkeys.pcss.models.ClientList
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

fun clientCoroutine(client: Socket, clientList: ClientList) {
    println("Receiving new client name...")
    val receiver = BufferedReader(InputStreamReader(client.getInputStream()))
    val sender = BufferedWriter(OutputStreamWriter(client.getOutputStream()))
    val name = receiver.readLine()
    clientList.addNewClient(client, name)
    while (!client.isClosed) {
        val message = receiver.readLine()
        clientList.writeToEveryBody(message)
    }
}