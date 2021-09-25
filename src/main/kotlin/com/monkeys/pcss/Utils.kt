package com.monkeys.pcss

import com.monkeys.pcss.models.WorkType
import com.monkeys.pcss.models.WorkType.*
import java.io.InputStream

const val BYTE_ARRAY_SIZE = 268435456/4
val BYTE_ARRAY = ByteArray(BYTE_ARRAY_SIZE)

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
    return "test6556"
}

fun readFromInputSteam(inputStream: InputStream): Pair<String, ByteArray> {
    val byteArray = ByteArray(inputStream.available())
    inputStream.read(byteArray, 0, inputStream.available())
    return Pair(String(byteArray).replace("\u0000", ""), byteArray)
}

fun substring(array: ByteArray, start: Int): ByteArray? {
    return substring(array, start, array.size)
}

fun substring(array: ByteArray?, start: Int, end: Int): ByteArray? {
    if (end <= start) return null
    val length = end - start
    val newArray = ByteArray(length)
    System.arraycopy(array, start, newArray, 0, length)
    return newArray
}
