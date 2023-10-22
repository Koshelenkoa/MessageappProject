package com.example.myapplication.data.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myapplication.data.local.room.MessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MessagesRepository @Inject constructor(private val messageDao: MessageDao) {

    fun createMessage(message: Message) {
        messageDao.insertAll(message)
    }
    fun loadMessages(chat: String?): Flow<List<Message>> {
        return messageDao.loadAllMessages(chat)
    }

    fun getMessageById(vararg messages: String): Flow<List<Message>> =
        messageDao.getMessageById(*messages)

    fun deleteMessages(vararg messages: Message) {
            messageDao.deleteMessages(*messages)
    }


    fun loadLastMessage(chatId: String?): Message? {

        return messageDao.loadLastMessageFromChat(chatId)
    }

    fun deleteMessagesFromChat(chat: String?) {
        messageDao.deleteAllMessagesInThisChat(chat)
    }

    /**
     * called when message is sent succesfully
     * sets a server timestamp to it
     * sets is_sent flag
     */
    open fun updateTime(chatId: String?, timeStamp: Long?) {
        messageDao.updateTimeStamp(chatId, timeStamp)
        messageDao.updateStatus(chatId, true)
    }

    fun getMessagesStream(chatId: String): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(
                pageSize = 25,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PagingSource(messageDao, chatId) }
        ).flow
    }

    companion object {

        @Volatile
        private var instance: MessagesRepository? = null

        fun getInstance(messageDao: MessageDao) =
            instance ?: synchronized(this) {
                instance ?: MessagesRepository(messageDao).also { instance = it }
            }
    }

}