package com.example.myapplication.data.local

import kotlinx.coroutines.flow.collect

/**
 * class for presenting minimal information about the chat to UI
 */
class ChatForUI(
    val chatId: String? = null,
    val nick: String? = null,
    val lastText: Message? = null,
    val timestamp: Long? = null,
) {
    companion object {
        fun createChatForUi(chat: Chat, lastText: Message?): ChatForUI {
            if (lastText != null) {
                return ChatForUI(
                    chat.chat_id,
                    chat.nick,
                    lastText,
                    lastText.timeStamp
                )
            } else {
                return ChatForUI(
                    chat.chat_id,
                    chat.nick,
                    null,
                    chat.timestamp
                )
            }
        }
    }
}