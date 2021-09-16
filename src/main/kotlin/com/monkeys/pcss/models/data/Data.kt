package com.monkeys.pcss.models.data

import com.monkeys.pcss.models.kit

data class Data(
    var messageId: String? = null,
    var senderName: String = "",
    var time: String = "",
    var messageText: String = "",
    var fileName: String? = null
) {


    constructor(dataMessage: String) : this() {
        val data = DataParser().parse(dataMessage)
        messageId = data.messageId
        senderName = data.senderName
        time = data.time
        messageText = data.messageText
        fileName = data.fileName
    }

    fun getServerMessage(): String =
        "<$time> [$senderName] :: $messageText :: " +
                "(${if (fileName.isNullOrEmpty()) "no file" else fileName} attached)\n" +
                if (!fileName.isNullOrEmpty()) {
                    //если фото, то
                    //берем файл, преобразуем его в аскии и вставляем
                    //иначе пофиг
                    "$fileName\n${kit()}"
                } else {
                    kit()
                }

    fun getClientMessage(): String =
        "[$messageId],[$senderName],[$time],[$messageText],[$fileName];"
}