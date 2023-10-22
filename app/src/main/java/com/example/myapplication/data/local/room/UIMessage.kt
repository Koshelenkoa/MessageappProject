package com.example.myapplication.data.local.room

import com.example.myapplication.data.local.Message
import com.example.myapplication.util.DateTimeConverter.Companion.getDay
import com.example.myapplication.util.DateTimeConverter.Companion.getTime

class UIMessage {
    val messageId: String
    var time: String
    var date: String
    var text: String

    constructor(message: Message) {
        messageId = message.messageId!!
        val timestamp = message.timeStamp!!
        time = getTime(timestamp)
        date = getDay(timestamp)
        text = message.data!!
    }
}