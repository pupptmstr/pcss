package com.monkeys.pcss.server

import com.monkeys.pcss.models.data.Data

class Server {

    fun start() {
        println("запущен сервер")
        println(Data("[test],[pupptmstr],[04:20],[Привет, гляньте котика],[];").getServerMessage())
    }

}