package com.example.myapplication.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class KeyGeneratorTest {

    lateinit var keygen: KeyGeneratorForKeyStore
    @Before
    fun setup(){
         keygen = KeyGeneratorForKeyStore(KeyStore.getInstance("AndroidKeyStore"))
    }
    @Test
    fun keys_are_created()= runBlocking{
        keygen = KeyGeneratorForKeyStore(KeyStore.getInstance("AndroidKeyStore"))
        keygen.generatePairOfKeys("alias1")
        assert(keygen.getPublicKey("alias1")!=null)
        assert(keygen.getPrivateKey("alias1")!=null)
    }
}