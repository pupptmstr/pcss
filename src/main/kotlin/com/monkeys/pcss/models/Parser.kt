package com.monkeys.pcss.models.data

import com.monkeys.pcss.models.header.Header
import com.monkeys.pcss.models.header.MessageType

class Parser {
    

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
    
    fun parseHeader(headerMessage: String) : Header {
        val regex = """\[([0-2])\],\[(true|false)\],\[([0-9]+)\];""".toRegex()
        
        val matchResult = regex.matchEntire(headerMessage)
        return if (matchResult != null) {
            val type = matchResult.groupValues[1]
            val isFileAttached = matchResult.groupValues[2]
            val dataSize = matchResult.groupValues[3]
            Header(MessageType.values()[type.toInt()], isFileAttached.toBoolean(), dataSize.toInt())
        } else {
            Header()
        }
    }
}