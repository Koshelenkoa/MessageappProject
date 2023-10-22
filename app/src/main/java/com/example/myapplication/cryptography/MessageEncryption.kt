package com.example.myapplication.cryptography

import androidx.compose.runtime.collectAsState
import com.example.myapplication.data.local.ChatRepository
import com.example.myapplication.data.local.Message
import java.security.KeyStore
import javax.crypto.Cipher
import javax.inject.Inject

class MessageEncryption @Inject constructor(
    private val chatRepository: ChatRepository
) {
    private val keyGen = KeyGeneratorForKeyStore(KeyStore.
    getInstance("AndroidKeyStore"))
    private val encryptor  = CipherDecipher(keyGen)
    /**
     *Encrypts contents of the given message
     *@param message message contents of which is going to be encrypted
     *@param chatId String of the chatId to whom message gets adressed
     */
    suspend fun encryptMessage(message: Message, chatId: String): Message {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        lateinit var message: Message
        val chat = chatRepository.getChat(chatId)
            val alias: String? = chat?.alias
            val publicKey = chat?.publicKey
            val encryptedText: String = encryptor.encrypt(
                message.data!!.toByteArray(), alias,
                publicKey, cipher
            ).toString()
            message = Message(
                message.messageId,
                message.sender,
                chatId,
                false,
                null,
                encryptedText
            )

        return message
    }

    /**
     * Decrypts text in the message object, returns Message object
     */
    suspend fun decryptMessage(message: Message): Message {
        lateinit var message: Message
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val chatId: String = message.chat.toString()
        val chat = chatRepository.getChat(chatId)
            val alias: String? = chat?.alias
            val publicKey = chat?.publicKey
            val decryptedText = encryptor.decrypt(
                message.data!!.toByteArray(), alias,
                publicKey, cipher
            ).toString()

            message =  Message(
                message.messageId,
                message.sender,
                message.chat,
                true,
                message.timeStamp,
                decryptedText
            )

        return message
    }

    /**
     * decryptMessage() for List of messages
     */
    suspend fun decryptMessages(encryptedListOfMessages: List<Message>): List<Message> {
        var decryptedMessages: MutableList<Message> = mutableListOf()
        for (message in encryptedListOfMessages) {
            decryptMessage(message)
            decryptedMessages.add(message)
        }
        return decryptedMessages.toList()
    }

}
