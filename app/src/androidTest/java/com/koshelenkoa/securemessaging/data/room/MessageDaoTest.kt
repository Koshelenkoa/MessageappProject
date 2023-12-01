package com.koshelenkoa.securemessaging.data.room


import android.content.Context
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.room.MessageDao
import com.koshelenkoa.securemessaging.data.local.room.ThisAppsDatabase
import com.google.common.truth.Truth.assertThat
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
    private val CONFIG  = PagingConfig(10)
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
        val message = Message("1","a","1",0,
            System.currentTimeMillis(),"Hello word".toByteArray())
        messageDao.insertAll(message)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            val pager = TestPager(CONFIG, messageDao.pagingSource("1"))
            val result = pager.refresh() as PagingSource.LoadResult.Page
            assertThat(result.data)
                .contains(message)
                latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun delete_returnsTrue() = runBlocking {
       val message = Message("2","a","2",0,
        System.currentTimeMillis(),"Hello word_".toByteArray())
       val secondMessage = Message("3","b","2",0,
        System.currentTimeMillis(),"Hello word__".toByteArray())

        messageDao.insertAll(message, secondMessage)

        messageDao.deleteMessages(message, secondMessage)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            val pager = TestPager(CONFIG, messageDao.pagingSource("1"))
            val result = pager.refresh() as PagingSource.LoadResult.Page
            assertThat(result.data)
                .contains(message)
            latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()

    }

    @Test
    fun updateTimestamp_returnsTrue() = runBlocking {
        val message = Message("1","a","1",0,
            null,"Hello word".toByteArray())
        messageDao.insertAll(message)

        messageDao.updateTimeStamp("1", System.currentTimeMillis())
        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            val pager = TestPager(CONFIG, messageDao.pagingSource("1"))
            val result = pager.refresh() as PagingSource.LoadResult.Page
            assertThat(result.data[0].timestamp)
                .isNotEqualTo(null)
            latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()
    }

    fun loadLastMessageFromChat_returnsTrue() = runBlocking {
        val message = Message("1","a","2",0,
            System.currentTimeMillis(),"Hello word".toByteArray())
        val secondMessage = Message("3","b","2",0,
            System.currentTimeMillis()+100000,"Hello word__".toByteArray())
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

