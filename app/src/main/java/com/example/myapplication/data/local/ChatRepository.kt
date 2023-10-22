package com.example.myapplication.data.local

import android.media.AudioTimestamp
import com.example.myapplication.data.local.room.ChatDao
import javax.inject.Inject

open class ChatRepository @Inject constructor(private val chatDao: ChatDao) {

    fun createChat(chats: Chat) = chatDao.insertAll(chats)

    fun deleteChats(chat: Chat) = chatDao.deleteChats(chat)

    fun setTimestamp(chatId: String, timestamp: Long){
        chatDao.updateTimeStamp(chatId, timestamp)
    }

    fun loadChats() = chatDao.loadAllChats()

    fun getChat(chatId: String) = chatDao.getChat(chatId)
    companion object {

        @Volatile
        private var instance: ChatRepository? = null

        fun getInstance(chatDao: ChatDao) =
            instance ?: synchronized(this) {
                instance ?: ChatRepository(chatDao).also { instance = it }
            }
    }
}

