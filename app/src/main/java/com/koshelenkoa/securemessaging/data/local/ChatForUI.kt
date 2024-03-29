package com.koshelenkoa.securemessaging.data.local

import com.google.gson.GsonBuilder

/**
 * class for presenting minimal information about the chat to UI
 */
class ChatForUI(
    val chatId: String? = null,
    val nick: String? = null,
    val lastText: String? = null,
    val timestamp: Long? = null,
) {
    companion object {
        fun createChatForUi(chat: Chat, lastMessage: Message?): ChatForUI {
            return if (lastMessage != null) {
                val lastText = lastMessage.data!!.decodeToString()
                val gson = GsonBuilder().create()
                val messageData = gson.fromJson(lastText, MessageData::class.java)
                val text = messageData.text ?: "attachment"

                ChatForUI(
                    chat.chat_id,
                    chat.nick,
                    text,
                    lastMessage.timestamp
                )
            } else {
                ChatForUI(
                    chat.chat_id,
                    chat.nick,
                    null,
                    chat.timestamp
                )
            }
        }
    }
}