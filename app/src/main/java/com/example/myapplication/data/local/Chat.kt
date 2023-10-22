package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

/**
 * nick - at start received from the user database from the server but can be overriden by client
 * alias - alias of the private key associated with the chat
 * public key - encoded public key gotten from the recipient
 */
@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    val chat_id: String,
    @ColumnInfo(name = "nick") var nick: String? = null,
    @ColumnInfo(name = "alias") var alias: String? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "timestamp") var timestamp: Long? = null,
    @ColumnInfo(
        name = "public_key",
        typeAffinity = ColumnInfo.BLOB
    ) var publicKey: ByteArray? = null,
    //@ColumnInfo(name = "user_pic", typeAffinity = ColumnInfo.BLOB) var userPic: Bitmap? = null,
) {
    @Override
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "chat_Id" to chat_id,
            "nick" to nick,
            "alias" to alias
        )
    }
}