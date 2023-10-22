package com.example.myapplication.cryptography

import android.util.Log
import javax.crypto.Cipher

/**
 *  class to share cryptoObject between activities
 */
class CipherHolder {
    companion object {
        val TAG = "CipherHolder"
        private var cipher: Cipher? = null

        /**
         * sets a cipher created by biometric prompt
         */
        fun setCipher(crypto: Cipher) {

            cipher = crypto
            Log.d(TAG, "cryptoobject created")
        }

        /**
         * gets a cipher created by biometric prompt
         */
        fun getCipher(): Cipher? = cipher

        /**
         * deletes a cipher created by biometric prompt
         */
        fun deleteCipher() {
            cipher = null
            Log.d(TAG, "Cipher deleted")
        }
    }
}