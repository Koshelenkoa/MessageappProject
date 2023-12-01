package com.koshelenkoa.securemessaging.util

import android.util.Base64
import android.util.Base64.DEFAULT
import com.koshelenkoa.securemessaging.data.local.Chat
import com.koshelenkoa.securemessaging.data.local.ChatDataHolder
import java.util.UUID


class StringForQRGenerator constructor(
    private val keyGenerator: com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore,
) {

    /**
     * First step in keysharing prossess, called when chat is created at backend,
     * gives another user chatId and public key
     * @param chatId - chatId of created chat
     */
    fun step1(chatId: String): String {

        val alias = UUID.randomUUID().toString()
        val publicKey = keyGenerator.generatePairOfKeys(alias)
        val stringPublicKey = Base64.encodeToString(publicKey, DEFAULT)
        ChatDataHolder.setChat(Chat(chat_id = chatId, alias = alias))
        return "$chatId,$stringPublicKey"
    }

    /**
     * Second step in the QRCode keysharing to be called when QRcode read
     * puts chat data with public key to local database
     * returns chatId to be used in third step
     * @param inputString - string from the QRCode
     */
    fun step2(inputString: String): String {
        val list = inputString.split(",")
        val chatId = list[0]
        val publicKey = Base64.decode(list[1], DEFAULT)
        val alias = UUID.randomUUID().toString()
        ChatDataHolder.setChat(
            Chat(
                chat_id = chatId,
                alias = alias, publicKey = publicKey
            )
        )
        return chatId
    }

    /**
     * Third step in keysharing through QRcode
     * returns chatId and public key of user B to user A
     */
    fun step3(): String {
        lateinit var publicKeyString: String
        val chat = ChatDataHolder.chat
        val publicKey = keyGenerator.generatePairOfKeys(chat!!.alias)
        publicKeyString = Base64.encodeToString(publicKey, DEFAULT)
        return publicKeyString
    }

    /**
     * Forth step in keysharing reads and saves public key
     */
    fun step4(inputString: String) {
        val publicKey = Base64.decode(inputString, DEFAULT)
        ChatDataHolder.update(publicKey = publicKey)
    }
}