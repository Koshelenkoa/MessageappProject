package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.local.room.MessageItem


@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    /**
     * messageId - generated unique id
     */
    var messageId: String,
    /**
     * sender - login of the sender
     */
    @ColumnInfo(name = "sender") var sender: String? = null,
    /**
     * chat - chat id
     */
    @ColumnInfo(name = "message_chat_id") var chat: String? = null,
    /**
     * isSent - was message sent succesfully or still pending
     */
    @ColumnInfo(name = "is_sent") var isSent: Boolean? = null,
    /**
     * timeStamp - server timestamp
     */
    @ColumnInfo(name = "timestamp") var timeStamp: Long? = null,
    /**
     * data - text of the message a string
     */
    @ColumnInfo(name = "text") var data: String? = null,
    //@ColumnInfo (name = "is_seen") val isSeen:Boolean?
) {
    @Override
    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "messageId" to messageId,
            "sender" to sender,
            "message_chat_id" to chat,
            "is_sent" to isSent,
            "timestamp" to timeStamp,
            "text" to data
        )
    }

    fun toItem(uid: String): MessageItem{
        return MessageItem(this, uid)
    }

    constructor(message: Map<String, String?>) :
            this(
                message.get("messageId")?:"-1",
                message.get("sender"),
                message.get("message_chat_id"),
                message.get("is_sent").toBoolean(),
                message.get("timestamp")?.toLong(),
                message.get("text")
            )
}