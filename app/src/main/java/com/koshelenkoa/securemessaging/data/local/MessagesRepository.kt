package com.koshelenkoa.securemessaging.data.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.koshelenkoa.securemessaging.data.local.room.MessageDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MessagesRepository @Inject constructor(private val messageDao: MessageDao) {

    fun createMessage(message: Message) {
        messageDao.insertAll(message)
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

    fun getMessageList(chatId: String): Flow<PagingData<Message>> {
        var pager = Pager(
            config = PagingConfig(pageSize = 30),
            pagingSourceFactory = {
                messageDao.pagingSource(chatId)
            }
        )
        return pager.flow
    }

    /**
     * called when message is sent succesfully
     * sets a server timestamp to it
     * sets is_sent flag
     */
    fun updateTime(messageId: String?, timeStamp: Long?) {
        messageDao.updateTimeStamp(messageId, timeStamp)
        messageDao.updateStatusSend(messageId)
    }

    fun updateStatusFailed(messageId: String?) {
        messageDao.updateFailed(messageId)
    }

    fun getMessagesStream(chatId: String): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(
                pageSize = 25,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { messageDao.pagingSource(chatId) }
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