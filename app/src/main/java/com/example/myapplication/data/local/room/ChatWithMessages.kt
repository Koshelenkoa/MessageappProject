package com.example.myapplication.data.local.room

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.data.local.Chat
import com.example.myapplication.data.local.Message

data class ChatWithMessages(
    @Embedded val chat: Chat,
    @Relation(
        parentColumn = "chat_id",
        entityColumn = "message_chat_id"
    )
    val messages: List<Message>,
)