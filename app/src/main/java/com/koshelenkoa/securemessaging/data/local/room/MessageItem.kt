package com.koshelenkoa.securemessaging.data.local.room

import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.MessageData
import com.koshelenkoa.securemessaging.util.DateTimeConverter.Companion.getDay
import com.koshelenkoa.securemessaging.util.DateTimeConverter.Companion.getTime
import com.google.gson.GsonBuilder


class MessageItem {
    var messageId: String
    var isMine: Boolean
    var messageData: MessageData
    var date: String?
    var time: String?
    var sent: Int

    constructor(message: Message, uid: String) {
        var isMine: Boolean
        val timestamp = message.timestamp
        isMine = message.sender.equals(uid)
        messageId = message.messageId
        this.isMine = isMine
        //convert json string to MessageData
        val contentJson = message.data!!.decodeToString()
        val gson = GsonBuilder().create()
        messageData = gson.fromJson(contentJson, MessageData::class.java)

        sent = message.isSent!!
        if (timestamp != null) {
            date = getDay(timestamp)
            time = getTime(timestamp)
        } else {
            date = null
            time = null
        }
    }
}