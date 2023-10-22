package com.example.myapplication.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.Chat
import com.example.myapplication.data.local.room.ChatDao
import com.example.myapplication.data.local.room.ThisAppsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class ChatDaoTest {

    private lateinit var database: ThisAppsDatabase
    private lateinit var chatDao: ChatDao


    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
        context, ThisAppsDatabase::class.java
        ).allowMainThreadQueries().build()

        chatDao = database.chatDao()
    }

    @Test
    @Throws(Exception::class)
    fun insertWord_returnsTrue() = runBlocking {
        val chat = Chat("1",null, null, null, null)
        chatDao.insertAll(chat)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            chatDao.loadAllChats().collect {
                assert((it).contains(chat))
                latch.countDown()

            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }
}