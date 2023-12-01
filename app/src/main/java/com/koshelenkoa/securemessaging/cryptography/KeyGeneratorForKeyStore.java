package com.koshelenkoa.securemessaging.cryptography;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;

/**
 *
 */
public class KeyGeneratorForKeyStore {

    private byte[] pubKey;

    private final KeyStore keyStore;


    public KeyGeneratorForKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Generates pair of keys in the keystore
     * keysize - 256
     * curve - secp256r1
     * keys require biometric cipher
     *
     * @param alias
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     */
    public byte[] generatePairOfKeys(String alias)
            throws KeyStoreException, CertificateException, IOException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

        keyStore.load(null);

        //specifing key generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore");

        AlgorithmParameterSpec alspec = new ECGenParameterSpec("secp256r1");
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_AGREE_KEY)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setAlgorithmParameterSpec(alspec)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(false)
                .build();

        //generate key pair
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(keyGenParameterSpec, random);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        pubKey = keyPair.getPublic().getEncoded();
        Certificate[] cert = keyStore.getCertificateChain(alias);
        KeyStore.PrivateKeyEntry mPrivatekey =
                new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), cert);
        return pubKey;
    }


    public PrivateKey getPrivateKey(final String alias)
            throws Exception {
        PrivateKey privateKey;
        try {
            keyStore.load(null);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("KeyStore could not be initialized", e);
        }

        try {
            privateKey = (PrivateKey) keyStore.getKey(alias, null);
        } catch (Exception e) {
            throw new Exception("Could not get entry from keystore", e);
        }
        return privateKey;
    }

}





