package com.koshelenkoa.securemessaging.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koshelenkoa.securemessaging.data.local.Chat
import com.koshelenkoa.securemessaging.data.local.ChatRepository
import com.koshelenkoa.securemessaging.data.local.Message
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import javax.crypto.Cipher

@RunWith(AndroidJUnit4::class)
class MessageEncryptorTest{

    lateinit var encryption : MessageEncryption
    lateinit var cipherDecipher: com.koshelenkoa.securemessaging.cryptography.CipherDecipher
    lateinit var cipher: Cipher
    lateinit var chatRepository: ChatRepository


    @Before
    fun setup(){
        cipherDecipher =Mockito.mock(com.koshelenkoa.securemessaging.cryptography.CipherDecipher::class.java)
        chatRepository = Mockito.mock(ChatRepository::class.java)
        encryption = MessageEncryption(chatRepository, "c1")
        cipher = Mockito.mock(Cipher::class.java)

    }

    @Test
    fun encrypted_message_returns_true(){
        runBlocking {
            val plainMessage = Message(
                messageId = "messageId",
                data = "plainMessage".toByteArray()
            )

            Mockito.`when`(
                cipherDecipher.encrypt(
                    "plainText".toByteArray(), cipher
                )
            )
                .thenReturn("encryptedText".toByteArray())
            Mockito.`when`(chatRepository.getChat("c1")).thenReturn(
                Chat(chat_id = "c1", alias = "alias", publicKey = "key".toByteArray())
            )

            val message = encryption.encryptMessage(plainMessage)
            assert("encryptedText".equals(message.data))
        }
    }

    @Test
    fun decrypted_message_returns_true(){
        runBlocking {
            val encryptedMessage = Message(
                messageId = "001", chat = "c1",
                data = "cipherText".toByteArray()
            )
            Mockito.`when`(
                cipherDecipher.decrypt(
                    "cipherText".toByteArray(), cipher)
            ).thenReturn("plainText".toByteArray())
            Mockito.`when`(chatRepository.getChat("c1")).thenReturn(
                Chat(chat_id = "c1", alias = "alias", publicKey = "key".toByteArray())
            )
            val message = encryption.decryptMessage(encryptedMessage)
            assert("plainText".toByteArray() == message.data)
        }
    }

}
