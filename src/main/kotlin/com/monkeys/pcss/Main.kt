package com.monkeys.pcss

import com.monkeys.pcss.client.Client
import com.monkeys.pcss.models.WorkType.*
import com.monkeys.pcss.models.message.parseHostAndPort
import com.monkeys.pcss.server.Server

fun main(args: Array<String>) {
    when(restoreArguments(args.toList())) {
        SERVER -> {
           val server = Server()
           server.start()
        }

        CLIENT -> {
            val client = Client("localhost", 8081)
            client.start()
        }

        CLIENT_WITH_ARGUMENTS -> {
            val clientArgumentIndex = args.indexOf("-c") + 1
            val arg = parseHostAndPort(args[clientArgumentIndex])
            val client = Client(arg.first, arg.second)
            client.start()
        }

        HELP -> {
            printHelp()
        }

    }
}