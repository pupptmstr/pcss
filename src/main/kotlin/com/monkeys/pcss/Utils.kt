package com.monkeys.pcss

import com.monkeys.pcss.models.WorkType
import com.monkeys.pcss.models.WorkType.*
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import java.io.BufferedInputStream
import java.io.OutputStream
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

fun readMessageFromInputStream(inputStream: BufferedInputStream): ByteArray {
    val out = arrayListOf<Byte>()
    var count: Int
    val buffer = ByteArray(4096) // or 4096, or more

    while (inputStream.read(buffer).also { count = it } > 0) {
        out.addAll(buffer.slice(0 until count))
        val available = inputStream.available()
        if (count < 4096 && available == 0) {
            Thread.sleep(500)
            if (inputStream.available() == 0) break
        }
    }
    val res = out.toByteArray()
    return res
}

fun send(outputStream: OutputStream, byteArray: ByteArray) {
    try {
        outputStream.write(byteArray)
    } catch (e: SocketException) {
        println("ууупс")
        //обработка отправления при выключении сервера, отключении клиента
    }
}

fun sendMessage(outputStream: OutputStream, message: ByteArray, file: ByteArray?) {
    send(outputStream, message)
    if (file != null && file.isNotEmpty()) {
        send(outputStream, file)
    }
    outputStream.flush()
}

fun getNewMessage(inputStream: BufferedInputStream): Pair<Message, ByteArray> {
    val fixedHeaderSize = 20
    val headerByteArray = ByteArray(fixedHeaderSize)
    inputStream.readNBytes(headerByteArray,0 ,fixedHeaderSize)
    val sHeader = String(headerByteArray).replace("\u0000", "")
    val header = Header(sHeader)
    val dataByteArray = ByteArray(header.dataSize)
    inputStream.readNBytes(dataByteArray,0 ,header.dataSize)
    val sData = String(dataByteArray).replace("\u0000", "")
    val data = Data(sData)
    val message = Message(header, data)
    if (header.isFileAttached) {
        val fileByteArray = ByteArray(data.fileSize)
        inputStream.readNBytes(fileByteArray, 0, data.fileSize)
        return Pair(message, fileByteArray)
    }
    return Pair(message, ByteArray(0))
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

fun getFixedLengthString(dataSize: Int): String {
    return String.format("%1$" + 8 + "s", dataSize).replace(' ', '0')
}



