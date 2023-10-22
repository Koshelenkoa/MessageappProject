package com.example.myapplication.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.Chat
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp

@Dao
interface ChatDao {

    @Insert
    fun insertAll(vararg chats: Chat)

    @Delete
    fun deleteChats(vararg chats: Chat)

    @Query("SELECT chats.* FROM chats")
    fun loadAllChats(): Flow<List<Chat>>

    @Query("SELECT * FROM chats WHERE chat_id = :chatId")
    fun getChat(chatId: String): Chat?

    @Query("UPDATE  chats  SET timestamp = :timestamp WHERE chat_id = :chatId ")
    fun updateTimeStamp(chatId: String?, timestamp: Long)


    @Query("UPDATE  chats  SET public_key = :publicKey WHERE chat_id = :chatId ")
    fun updatePublicKey(chatId: String?, publicKey: ByteArray?)

    @Query("UPDATE chats SET nick = :name WHERE chat_id = :chat")
    fun setName(chat: String, name: String)
}
