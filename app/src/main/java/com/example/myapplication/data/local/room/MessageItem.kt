package com.example.myapplication.data.local.room

import android.content.Context

import com.example.myapplication.MainApplication
import com.example.myapplication.data.local.Message
import com.example.myapplication.util.DateTimeConverter.Companion.getDay
import com.example.myapplication.util.DateTimeConverter.Companion.getTime


class MessageItem {
    var messageId: String
    var isMine: Boolean
    var content: String
    var date: String
    var time: String

    constructor(message: Message, uid: String) {
        var isMine: Boolean
        val timestamp = message.timeStamp!!
        isMine = message.sender.equals(uid)
        messageId = message.messageId!!
        this.isMine = isMine
        content = message.data!!
        date = getDay(timestamp)
        time = getTime(timestamp)
    }
}