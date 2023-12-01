package com.koshelenkoa.securemessaging.cryptography

import com.koshelenkoa.securemessaging.data.local.ChatRepository
import com.koshelenkoa.securemessaging.data.local.Message
import java.security.KeyStore
import javax.crypto.Cipher
import javax.inject.Inject

class MessageEncryption @Inject constructor(
    private val chatRepository: ChatRepository,
    chatId: String,
) {
    private val keyGen =
        com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore(KeyStore.getInstance("AndroidKeyStore"))
    private val chat = chatRepository.getChat(chatId)
    private val alias: String? = chat?.alias
    private val publicKey = chat?.publicKey
    private val encryptor =
        com.koshelenkoa.securemessaging.cryptography.CipherDecipher(keyGen, publicKey, alias)

    /**
     *Encrypts contents of the given message
     *@param message message contents of which is going to be encrypted
     */
    fun encryptMessage(message: Message): Message {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val encryptedText = encryptor.encrypt(message.data!!, cipher)
        return message.copy(isSent = Message.PENDING, data = encryptedText)
    }

    /**
     * Decrypts text in the message object, returns Message object
     */
    fun decryptMessage(message: Message): Message {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val decryptedText = encryptor.decrypt(message.data!!, cipher)

        return message.copy(data = decryptedText)
    }

    /**
     * decryptMessage() for List of messages
     */
    fun decryptMessages(encryptedListOfMessages: List<Message>): List<Message> {
        var decryptedMessages: MutableList<Message> = mutableListOf()
        for (message in encryptedListOfMessages) {
            decryptMessage(message)
            decryptedMessages.add(message)
        }
        return decryptedMessages.toList()
    }

}
