package com.koshelenkoa.securemessaging.data.local.room

import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.util.DateTimeConverter.Companion.getDay
import com.koshelenkoa.securemessaging.util.DateTimeConverter.Companion.getTime

class UIMessage {
    val messageId: String
    var time: String
    var date: String
    var text: String

    constructor(message: Message) {
        messageId = message.messageId
        val timestamp = message.timestamp!!
        time = getTime(timestamp)
        date = getDay(timestamp)
        text = message.data!!.decodeToString()
    }
}