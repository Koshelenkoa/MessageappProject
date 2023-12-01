package com.koshelenkoa.securemessaging.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class KeyGeneratorTest {

    lateinit var keygen: com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore
    @Before
    fun setup(){
         keygen = com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore(
             KeyStore.getInstance("AndroidKeyStore")
         )
    }
    @Test
    fun keys_are_created()= runBlocking{
        keygen = com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore(
            KeyStore.getInstance("AndroidKeyStore")
        )
        assert(keygen.generatePairOfKeys("alias1")!=null)
        assert(keygen.getPrivateKey("alias1")!=null)
    }
}