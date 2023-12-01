package com.koshelenkoa.securemessaging.data.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.koshelenkoa.securemessaging.data.local.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    companion object

    @Insert
    fun insertAll(vararg messages: Message)

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    fun getMessageById(vararg messageId: String?): Flow<List<Message>>

    @Delete
    fun deleteMessages(vararg messages: Message)

    @Query("DELETE FROM messages WHERE message_chat_id = :thisChat")
    fun deleteAllMessagesInThisChat(vararg thisChat: String?)

    @Query("SELECT * FROM messages WHERE message_chat_id = :thisChat ORDER BY timestamp DESC")
    fun loadAllMessages(thisChat: String?): PagingSource<Int, Message>


    @Query(
        "SELECT * FROM messages WHERE message_chat_id = :thisChat " +
                "ORDER BY is_sent DESC, timestamp DESC"
    )
    fun pagingSource(thisChat: String?): PagingSource<Int, Message>

    @Query("UPDATE messages  SET timestamp = :timestamp WHERE messageID = :msgId ")
    fun updateTimeStamp(msgId: String?, timestamp: Long?)

    @Query(
        "SELECT * " +
                "FROM messages " +
                "WHERE message_chat_id = :thisChat " +
                "AND timestamp = (SELECT MAX(timestamp) FROM messages WHERE message_chat_id = :thisChat)"
    )
    fun loadLastMessageFromChat(thisChat: String?): Message?

    @Query("UPDATE messages  SET is_sent = 0  WHERE messageID = :msgId")
    fun updateStatusSend(msgId: String?)

    @Query("UPDATE messages  SET is_sent = 2  WHERE messageID = :msgId")
    fun updateFailed(msgId: String?)


}
