package com.example.myapplication.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.ListenableWorker.Result
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class LoadWorkerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor


    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun testSendMessageWorker() {
        val worker = TestListenableWorkerBuilder<SendMessageWorker>(
            context = context)
            .setInputData(Data(mapOf<String, Any?>("URL" to "http://127.0.0.1:5001/messagingapp-399b9/us-central1/",
                "messageId" to "9d3037f0-7cc2-4f6a-a5f9-d61b3457f1c1",
                "sender" to "Alice",
                "chat" to "c1",
                "data" to "Hi, how are you doing?")))

            .build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(Result.success()))
        }
    }
}