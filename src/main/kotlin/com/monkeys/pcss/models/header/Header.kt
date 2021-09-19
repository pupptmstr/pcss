package com.monkeys.pcss.models.header

import com.monkeys.pcss.models.data.Parser

data class Header(
    var type: MessageType = MessageType.LOGIN,
    var isFileAttached: Boolean = false,
    var dataSize: Int = 0
) {
    constructor(headerMessage: String) : this() {
        val header = Parser().parseHeader(headerMessage)
        type = header.type
        isFileAttached = header.isFileAttached
        dataSize = header.dataSize
    }

fun getHeader() =
    "[${type.ordinal}],[$isFileAttached],[$dataSize];"

}