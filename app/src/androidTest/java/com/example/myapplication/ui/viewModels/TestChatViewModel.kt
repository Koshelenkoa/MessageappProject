package com.example.myapplication.ui.viewModels

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.cryptography.CipherHolder
import com.example.myapplication.cryptography.CipherHolder.Companion.getCipher
import com.example.myapplication.cryptography.MessageEncryption
import com.example.myapplication.data.local.ChatRepository
import com.example.myapplication.data.local.Message
import com.example.myapplication.data.local.MessagesRepository
import com.example.myapplication.data.local.room.MessageDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import javax.crypto.Cipher

@RunWith(AndroidJUnit4::class)
class TestChatViewModel {

    private lateinit var messageRepository: MessagesRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var viewModel: ChatViewModel
    private lateinit var messages: List<Message>
    private lateinit var messageDao: MessageDao
    private var flowMessages = flowOf(messages)



    fun deleteFromList(messageId: String?){
        messages.filterNot { it.messageId.equals(messageId)}
    }

    @Before
    fun setUp() {
        messages = listOf(
            Message(messageId="0001", chat = "c1", isSent = true, timeStamp = 1468874326, data  = "Message_text1"),
            Message(messageId="0002", chat = "c1", isSent = false, timeStamp = 1250487227, data  = "Message_text2"),
            Message(messageId="0003", chat = "c2", isSent = true, timeStamp = 947010766, data  = "Message_text3"))
        flowMessages = flowOf(messages)
        messageRepository = Mockito.mock(MessagesRepository::class.java)
        chatRepository = Mockito.mock(ChatRepository::class.java)
        messageDao = Mockito.mock(MessageDao::class.java)
        viewModel = ChatViewModel(messageRepository, chatRepository, messageDao).apply {
        }
    }

    @Test
    fun messages_loaded(){
        runBlocking {
            val encryptor = Mockito.mock(MessageEncryption::class.java)
            val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
            Mockito.`when`(encryptor.decryptMessages(messages)).thenReturn(messages)
            Mockito.`when`(messageRepository.loadMessages("c1")).thenReturn(flowMessages)
            mockStatic(CipherHolder::class.java)
            `when`(CipherHolder.getCipher()).thenReturn(cipher)
            val uid = "uid"
            viewModel.loadMessages()
            val messageItems = messages.map { it -> it.toItem(uid) }
            var currentChatState = viewModel.uiState.value
            assert(currentChatState.messages.containsAll(messageItems))
        }
    }

    @Test
    fun messages_selected_return_true(){
        viewModel.selectMessages("001")
        val currentState = viewModel.uiState.value
        val selected = currentState.selectedMessages
        assert(selected.contains("001"))
    }

    @Test
    fun messages_deleted(){
        runBlocking {
            Mockito.doAnswer {
                deleteFromList("001")
                null
            }.`when`(messageRepository).deleteMessages(messages.first { message: Message ->
                message.messageId.equals(
                    "001"
                )
            }
        )
            viewModel.deleteMessages()
            val currentState = viewModel.uiState.value
            assert(!currentState.messages.any { it ->
                it.messageId.equals("001")
            })
        }
    }

    @Test
    fun chat_set(){
        viewModel.setChat("c1")
        assertEquals("c1", viewModel.uiState.value.chatId)
    }
    @Test
    fun selection_screen_on() {
        viewModel.selectionScreenOn()
        val currentState = viewModel.uiState.value
        assertEquals(true, currentState.selectionScreen)
    }

    @Test
    fun selection_screen_off() {
        viewModel.selectionScreenOff()
        val currentState = viewModel.uiState.value
        assertEquals(false, currentState.selectionScreen)
    }

    @Test
    fun get_selection_screen() {
        viewModel.selectionScreenOn()
        val isSelected = viewModel.getSelectionScreen()
        assertEquals(true, isSelected)
    }

    @Test
    fun update_text_box() {
        viewModel.updateTextBox("New message text")
        val currentState = viewModel.uiState.value
        assertEquals("New message text", viewModel.mText)
    }

    @After
    fun tearDown() {
    }
}