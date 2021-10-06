package com.monkeys.pcss.models.message

import com.monkeys.pcss.kit
import com.monkeys.pcss.models.AsciiArt
import java.io.File

data class Data(
    var messageId: String? = null,
    var senderName: String = "",
    var time: String = "",
    var messageText: String = "",
    var fileName: String? = null
) {


    constructor(dataMessage: String) : this() {
        val data = parseData(dataMessage)
        messageId = data.messageId
        senderName = data.senderName
        time = data.time
        messageText = data.messageText.replace("[", "_%+<+$")
            .replace("]", "_%+>+\$")
        fileName = data.fileName
    }

    fun getClientMessage(image: File?): String =
        "<$time> [$senderName] :: ${messageText.replace( "_%+<+$","[")
            .replace("_%+>+\$", "]")} :: " +
                "(${if (fileName.isNullOrEmpty()) "no file" else fileName} attached)\n" +
                if (image != null) {
                    "\n${AsciiArt().getAsciiArt(image)}"
                } else {
                    kit()
                }

    fun getServerMessage(): String =
        "_[${messageId ?: ""}],[$senderName],[$time],[$messageText],[${fileName?: ""}]_;_"
}