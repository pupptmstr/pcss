package com.monkeys.pcss.models.message

fun parseData(dataMessage: String): Data {
    val regex =
        """\[([A-Za-z]+)?\],\[([A-Za-z0-9]+)\],\[((([0,1][0-9])|(2[0-3])):[0-5][0-9])\],\[([^\[\]]+)\],\[(([^(\[\])]+)\.([a-z]+))?\];""".toRegex()
    val matchResult = regex.matchEntire(dataMessage)
    return if (matchResult != null) {
        val messageId = matchResult.groupValues[1]
        val senderName = matchResult.groupValues[2]
        val time = matchResult.groupValues[3]
        val messageText = matchResult.groupValues[7]
        val fileName = matchResult.groupValues[8]
        Data(messageId, senderName, time, messageText, fileName)
    } else {
        Data()
    }
}

fun parseHeader(headerMessage: String): Header {
    val regex =
        """\[([0-2])\],\[([01])\],\[([0-9]+)\];""".toRegex()

    val matchResult = regex.matchEntire(headerMessage)
    return if (matchResult != null) {
        val type = matchResult.groupValues[1]
        val isFileAttached = matchResult.groupValues[2]
        val dataSize = matchResult.groupValues[3]
        Header(MessageType.values()[type.toInt()], (isFileAttached == "1"), dataSize.toInt())
    } else {
        Header()
    }
}

fun parseMessage(message: String) : Message {
    val splitMessage = message.split("]_;_[")
    val header = parseHeader(splitMessage[0])
    val data = parseData(splitMessage[1])
    val file = if (splitMessage.size > 2) splitMessage[2].toByteArray() else ByteArray(0)
    return Message(header, data, file)
}

fun parseHostAndPort(arg: String) : Pair<String,Int> =
    Pair(arg.split(":")[0], arg.split(":")[1].toInt())