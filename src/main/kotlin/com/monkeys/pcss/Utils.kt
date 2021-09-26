package com.monkeys.pcss

import com.monkeys.pcss.models.WorkType
import com.monkeys.pcss.models.WorkType.*
import java.io.InputStream

//const val BYTE_ARRAY_SIZE = 268435456/4
//val BYTE_ARRAY = ByteArray(BYTE_ARRAY_SIZE)

fun restoreArguments(args: List<String>): WorkType = when {
    //TODO(обработать флаги для передачи порта с ip")
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

fun shapingFileName(fileName: String, senderName: String, time: String): String {
    val builder = StringBuilder()
    val split = fileName.split(".")
    builder.append(split[0])
    builder.append("_")
    builder.append(senderName)
    builder.append("_")
    builder.append(time)
    builder.append(".")
    builder.append(split[1])
    return builder.toString()
}
