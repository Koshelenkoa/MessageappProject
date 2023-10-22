package com.example.myapplication.data.room


import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.local.Message
import com.example.myapplication.data.local.room.MessageDao
import com.example.myapplication.data.local.room.ThisAppsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class MessageDaoTest {
    private lateinit var database: ThisAppsDatabase
    private lateinit var messageDao: MessageDao
    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ThisAppsDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        messageDao = database.messageDao()
    }

    @Test
    fun insertWord_returnsTrue() = runBlocking {
        val message = Message("1","a","1",true,
            System.currentTimeMillis(),"Hello word")
        messageDao.insertAll(message)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            messageDao.loadAllMessages("1").collect {
                assert((it).contains(message))
                latch.countDown()

            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun delete_returnsTrue() = runBlocking {
       val message = Message("2","a","2",true,
        System.currentTimeMillis(),"Hello word_")
       val secondMessage = Message("3","b","2",true,
        System.currentTimeMillis(),"Hello word__")

        messageDao.insertAll(message, secondMessage)

        messageDao.deleteMessages(message, secondMessage)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            messageDao.loadAllMessages("2").collect {
                assert(!(it).contains(message))
                latch.countDown()
            }
        }
        latch.await()
        job.cancelAndJoin()

    }

    @Test
    fun updateTimestamp_returnsTrue() = runBlocking {
        val message = Message("1","a","1",true,
            null,"Hello word")
        messageDao.insertAll(message)

        messageDao.updateTimeStamp("1", System.currentTimeMillis())
        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            messageDao.loadAllMessages("1").collect {
                assert(it[0].timeStamp!=null)
                latch.countDown()

            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    fun loadLastMessageFromChat_returnsTrue() = runBlocking {
        val message = Message("1","a","2",true,
            System.currentTimeMillis(),"Hello word")
        val secondMessage = Message("3","b","2",true,
            System.currentTimeMillis()+100000,"Hello word__")
        messageDao.insertAll(message, secondMessage)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            val lastMessage = messageDao.loadLastMessageFromChat("2")
            assert(lastMessage?.messageId.equals("3"))
            latch.countDown()


        }
        latch.await()
        job.cancelAndJoin()
    }


    @After
    fun closeDatabase() {
        database.close()
    }
}

