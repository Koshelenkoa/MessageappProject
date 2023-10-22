package com.example.myapplication.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithMessages(
    @Embedded val chat: Chat,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userCreatorId"
    )
    val messages: List<Message>
)