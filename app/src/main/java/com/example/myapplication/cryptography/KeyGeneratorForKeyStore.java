package com.example.myapplication.cryptography;

import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 */
public class KeyGeneratorForKeyStore {

    private byte[] pubKey;

    private KeyStore keyStore;


    public KeyGeneratorForKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Generates pair of keys in the keystore
     * keysize - 256
     * curve - secp256r1
     * keys require biometric cipher
     * @param alias
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeySpecException
     */
    public void generatePairOfKeys(String alias)
            throws KeyStoreException, CertificateException, IOException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
            InvalidKeySpecException {

        keyStore.load(null);


        //specifing key generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore");
        AlgorithmParameterSpec alspec = new ECGenParameterSpec("secp256r1");
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                //.setUserAuthenticationRequired(true)
                //.setInvalidatedByBiometricEnrollment(true)
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
        PublicKey pubKeyMaterial =
                KeyFactory.getInstance(keyPair.getPublic().getAlgorithm()).generatePublic(
                        new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
        pubKey = keyPair.getPublic().getEncoded();


        Certificate[] cert = keyStore.getCertificateChain(alias);
        KeyStore.PrivateKeyEntry mPrivatekey =
                new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), cert);

    }


    public PrivateKey getPrivateKey(final String alias)
            throws Exception {
        KeyStore.Entry entry;
        try{
            keyStore.load(null);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("KeyStore could not be initialized", e);
        }

        try {
            entry = keyStore.getEntry(alias, null);
        } catch (Exception e){
            throw new Exception("Could not get entry from keystore", e);
        }
        PrivateKey key = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        return key;
    }

    public byte[] getPublicKey(String alias)
            throws Exception {
        return pubKey;
    }
}





