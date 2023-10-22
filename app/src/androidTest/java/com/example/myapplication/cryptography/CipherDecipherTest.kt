package com.example.myapplication.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher

@RunWith(AndroidJUnit4::class)
class CipherDecipherTest {
    lateinit var keyGen: KeyGeneratorForKeyStore
    lateinit var pubKeyA: ByteArray
    lateinit var pubKeyB: ByteArray
    lateinit var cipherDecipher: CipherDecipher
    lateinit var cipher: Cipher
    lateinit var privKeyA: PrivateKey
    lateinit var privKeyB: PrivateKey
    //decouple setup from key generator later
    //double must return private key
    @Before
    fun generateKeys(){

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val alspec: AlgorithmParameterSpec = ECGenParameterSpec("secp256r1")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "localKeyStore",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )   //.setUserAuthenticationRequired(true)
            //.setInvalidatedByBiometricEnrollment(true)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setAlgorithmParameterSpec(alspec)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(false)
            .build()
        //generate key pair
        val random = SecureRandom()
        keyPairGenerator.initialize(keyGenParameterSpec, random)

        val pairA = keyPairGenerator.generateKeyPair()
        val pairB = keyPairGenerator.generateKeyPair()
        pubKeyA = pairA.public.encoded
        pubKeyB = pairB.public.encoded
        privKeyA = pairA.private
        privKeyB = pairB.private

        keyGen = Mockito.mock(KeyGeneratorForKeyStore::class.java)

        cipherDecipher = CipherDecipher(keyGen)
        cipher = Cipher.getInstance("AES/GCM/NoPadding")
    }

    @Test
    fun encrypted_deciphered_message_is_the_same(){
        Mockito.`when`(keyGen.getPrivateKey("aliasA")).thenReturn(privKeyA)
        Mockito.`when`(keyGen.getPrivateKey("aliasB")).thenReturn(privKeyB)
        val plaintext = "plaintext".toByteArray()
        val ciphertext = cipherDecipher.encrypt(plaintext,"aliasA",pubKeyB, cipher)
        val text = cipherDecipher.decrypt(ciphertext,"aliasB", pubKeyA, cipher).toString()
        assertThat("plaintext",equalTo(text))
    }
}


