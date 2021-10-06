package com.monkeys.pcss

import com.monkeys.pcss.models.WorkType
import com.monkeys.pcss.models.WorkType.*
import java.io.*
import java.net.Socket
import java.net.SocketException

const val STANDARD_PORT = 8081
const val DOWNLOADS_DIR = "PCSS downloads/"

fun restoreArguments(args: List<String>): WorkType = when {
    args.isEmpty() -> {
        SERVER
    }
    args.contains("-c") && args.size == 1 -> {
        CLIENT
    }
    args.contains("-c") && args.size > 1 -> {
        CLIENT_WITH_ARGUMENTS
    }
    args.contains("-help") -> {
        HELP
    }
    else -> {
        println("Были переданы неверные аргументы, сверитесь с 'help':")
        HELP
    }
}

fun printHelp() {
    println(
        "\nДефолтно запуск без аргументов - запуск сервера (на порте 8080)\n" +
                "-с - запуск клиента\n" +
                "-help - вывод help меню\n" +
                "При неверных аргументах тоже выводится хэлп\n"
    )
}

fun kit(): String =
    "     (\\.-./) \n" +
            "   = (^ Y ^) = \n" +
            "     /`---`\\\n" +
            "    |U_____U|" +
            "\n"

fun generateMessageId(): String {
    //TODO()
    return "testNew"
}

fun readMessageFromInputStream(inputStream: InputStream): String =
    BufferedReader(InputStreamReader(inputStream)).readLine()

fun send(outputStream: OutputStream, byteArray: ByteArray) {
    try {
        outputStream.write(byteArray)
        outputStream.flush()
    } catch (e: SocketException) {
        println("ууупс")
        //обработка отправления при выключении сервера, отключении клиента
    }
}

fun shapingFileName(fileName: String, senderName: String, time: String): String {
    val builder = StringBuilder()
    val split = fileName.split(".")
    builder.append(split[0])
    builder.append("_")
    builder.append(senderName)
    builder.append("_")
    builder.append(time.replace(":", "-"))
    builder.append(".")
    builder.append(split[1])
    return builder.toString()
}



