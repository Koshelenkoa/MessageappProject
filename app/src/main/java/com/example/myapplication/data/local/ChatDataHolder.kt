package com.example.myapplication.data.local

/**
 * Class to hold chat when it is created from QRcode
 */
class ChatDataHolder {
    companion object{
        fun setChat(chat: Chat?) {
            this.chat = chat
        }

        fun update(nick: String? = chat?.nick,
                   alias: String? = chat?.alias,
                   address: String? = chat?.address,
                   timestamp: Long? = chat?.timestamp,
                   publicKey: ByteArray? = chat?.publicKey){
            if(chat != null)

            setChat(Chat(
                chat!!.chat_id,
                nick,
                alias,
                address,
                timestamp,
                publicKey
            ))
        }
        var chat: Chat? = null
            private set
    }
}