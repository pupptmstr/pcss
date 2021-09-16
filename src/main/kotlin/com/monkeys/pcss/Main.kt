package com.monkeys.pcss

import com.monkeys.pcss.client.Client
import com.monkeys.pcss.models.WorkType.*
import com.monkeys.pcss.server.Server

fun main(args: Array<String>) {
    when(restoreArguments(args.toList())) {
        SERVER -> {
           val server = Server()
           server.start()
        }

        CLIENT -> {
            val client = Client()
            client.start()
        }

        HELP -> {
            printHelp()
        }
    }
}