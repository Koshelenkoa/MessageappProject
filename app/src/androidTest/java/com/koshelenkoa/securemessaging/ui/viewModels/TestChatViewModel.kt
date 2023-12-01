package com.koshelenkoa.securemessaging.ui.viewModels

import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koshelenkoa.securemessaging.cryptography.MessageEncryption
import com.koshelenkoa.securemessaging.data.local.ChatRepository
import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.MessagesRepository
import com.koshelenkoa.securemessaging.data.local.room.MessageDao
import com.koshelenkoa.securemessaging.viewModels.ChatViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import javax.crypto.Cipher

@RunWith(AndroidJUnit4::class)
class TestChatViewModel {

    private lateinit var messageRepository: MessagesRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var viewModel: ChatViewModel
    private lateinit var messages: List<Message>
    private lateinit var messageDao: MessageDao
    private var flowMessages = flowOf(messages as PagingData<Message>)



    fun deleteFromList(messageId: String?){
        messages.filterNot { it.messageId.equals(messageId)}
    }

    @Before
    fun setUp() {
        messages = listOf(
            Message(
                messageId ="0001", chat = "c1", isSent = 0,
                timestamp = 1468874326, data = "Message_text1".toByteArray()),
            Message(
                messageId ="0002", chat = "c1", isSent = 0,
                timestamp = 1250487227, data = "Message_text2".toByteArray()),
            Message(
                messageId ="0003", chat = "c2", isSent = 0,
                timestamp = 947010766, data = "Message_text3".toByteArray()))
        flowMessages =  flowOf(messages as PagingData<Message>)
        messageRepository = Mockito.mock(MessagesRepository::class.java)
        chatRepository = Mockito.mock(ChatRepository::class.java)
        messageDao = Mockito.mock(MessageDao::class.java)
        viewModel = ChatViewModel(messageRepository, chatRepository).apply {
        }
    }

    @Test
    fun messages_loaded(){
        runBlocking {
            val encryptor = Mockito.mock(MessageEncryption::class.java)
            val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
            Mockito.`when`(encryptor.decryptMessages(messages)).thenReturn(messages)
            Mockito.`when`(messageRepository.getMessagesStream("c1")).thenReturn(flowMessages)
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