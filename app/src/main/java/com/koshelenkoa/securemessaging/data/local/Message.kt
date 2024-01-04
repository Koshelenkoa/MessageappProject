package com.koshelenkoa.securemessaging.data.local

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.koshelenkoa.securemessaging.data.local.room.MessageItem
import java.util.Base64


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
    @ColumnInfo(name = "is_sent") var isSent: Int? = PENDING,
    /**
     * timeStamp - server timestamp
     */
    @ColumnInfo(name = "timestamp") var timestamp: Long? = null,
    /**
     * data - text of the message a string
     */
    @ColumnInfo(name = "text", typeAffinity = ColumnInfo.BLOB) var data: ByteArray? = null,
    //@ColumnInfo (name = "is_seen") val isSeen:Boolean?
) {
    companion object {
        const val SENT = 0
        const val PENDING = 1
        const val FAILED = 2
    }

    @Override
    fun toMap(): Map<String, String?> = mapOf(
        "messageId" to messageId,
        "sender" to sender,
        "chat" to chat,
        "data" to Base64.getEncoder().encodeToString(data)
        )

    fun toItem(uid: String): MessageItem {
        return MessageItem(this, uid)
    }

    constructor(message: Map<String, String?>) : this(
        message["messageId"] ?: "-1",
        message["sender"],
        message["chat"],
        isSent = 0,
        message["timestamp"]?.toLong(),
        Base64.getDecoder().decode(message["data"])
    )

    /**
     * TransitMessage with bitmaps as attachments to
     * message stored in the local store with mediaStoreUris
     */

    fun toLocalMessage(attachmentsUris: List<Uri>): Message{
        val gson = Gson()
        val messageData = this.getMessageData()
        val urisAsStrings = attachmentsUris.map{ uri -> uri.path?: ""}
        val finalMessageData = MessageData(messageData.text, urisAsStrings.toTypedArray())
        val json = gson.toJson(finalMessageData)
        return this.copy(data = json.toByteArray())
    }

    fun toTransitMessage(attachmentsUrls: List<String>): Message{
        val gson = Gson()
        val messageData = this.getMessageData()
        val finalMessageData = MessageData(messageData.text, attachmentsUrls.toTypedArray())
        val json = gson.toJson(finalMessageData)
        return this.copy(data = json.toByteArray())
    }

    fun getMessageData(): MessageData{
        val gson = Gson()
        val messageData = gson.fromJson(data?.decodeToString(), MessageData::class.java)
        return messageData
    }
}