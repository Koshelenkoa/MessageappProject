package com.example.myapplication.QRcode

import android.util.Base64
import android.util.Base64.DEFAULT
import com.example.myapplication.cryptography.KeyGeneratorForKeyStore
import com.example.myapplication.data.local.Chat
import com.example.myapplication.data.local.ChatDataHolder
import java.util.UUID


class StringForQRGenerator constructor(
    private val keyGenerator: KeyGeneratorForKeyStore
) {

    /**
     * @param
     */
     fun step1(chatId: String): String {

        val alias = UUID.randomUUID().toString()
        keyGenerator.generatePairOfKeys(alias)
        val publicKey = keyGenerator.getPublicKey(alias)
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
    fun step2(inputString: String) : String{
        val list = inputString.split(",")
        val chatId = list.get(0)
        val publicKey = Base64.decode(list.get(1), DEFAULT)
        val alias = UUID.randomUUID().toString()
        keyGenerator.generatePairOfKeys(alias)
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
        val publicKey = chat?.publicKey
        publicKeyString = Base64.encodeToString(publicKey, DEFAULT)
        return publicKeyString
    }

    fun step4(inputString: String) {
        val publicKey = Base64.decode(inputString, DEFAULT)
        val chat = ChatDataHolder.chat!!
        ChatDataHolder.update(publicKey = publicKey)
    }
}