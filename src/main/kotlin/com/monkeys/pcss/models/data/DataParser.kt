package com.monkeys.pcss.models.data

class DataParser {
    val regex =
        """\[([A-Za-z]+)?\],\[([A-Za-z0-9]+)\],\[((([0,1][0-9])|(2[0-3])):[0-5][0-9])\],\[([^\[\]]+)\],\[(([^(\[\])]+)\.([a-z]+))?\];""".toRegex()

    fun parse(dataMessage: String): Data {
        val matchResult = regex.matchEntire(dataMessage)
        return if (matchResult != null) {
            var messageId = matchResult.groupValues[1]
            var senderName = matchResult.groupValues[2]
            var time = matchResult.groupValues[3]
            var messageText = matchResult.groupValues[7]
            var fileName = matchResult.groupValues[8]
            Data(messageId, senderName, time, messageText, fileName)
        } else {
            Data()
        }
    }
}