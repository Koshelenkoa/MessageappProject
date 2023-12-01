package com.koshelenkoa.securemessaging.cryptography;


import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class CipherDecipher {

    private final KeyGeneratorForKeyStore gk;

    public CipherDecipher(final KeyGeneratorForKeyStore keyGeneratorForKeyStore,
                          byte[] publicKey, String alias) throws Exception {
        gk = keyGeneratorForKeyStore;
        this.publicKey = publicKey;
        this.alias = alias;
        key = establishKeyAgreement();
    }

    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    private final byte[] publicKey;
    private final String alias;
    private final SecretKeySpec key;
    static final String TAG = "CipherDechiper Manager";


    /**
     * @return secret key from the shared secret as the byte[]
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws UnrecoverableEntryException
     * @throws InvalidKeySpecException
     */
    public SecretKeySpec establishKeyAgreement()
            throws Exception {

        PrivateKey privateKey = gk.getPrivateKey(alias);
        KeyAgreement keyAgree = KeyAgreement.getInstance("ECDH");

        keyAgree.init(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        keyAgree.doPhase(keyFactory.generatePublic(keySpec), true);
        byte[] key = keyAgree.generateSecret();
        Log.d(TAG, "Key agreement established");

        return new SecretKeySpec(key, "AES");
    }

    /**
     * encrypts plaintext with AES-GSM algorithm
     *
     * @param plaintext unencrypted info in the byte[] form
     * @param cipher    cipher from the biometric prompt
     * @return encrypted text as a byte[]
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws UnrecoverableEntryException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeySpecException
     */
    public byte[] encrypt(byte[] plaintext, Cipher cipher)
            throws Exception {

        byte[] nonce = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
        byte[] ciphertext = cipher.doFinal(plaintext);

        byte[] combined = new byte[nonce.length + ciphertext.length];

        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);

        return combined;
    }

    /**
     * decrypts plaintext with AES-GCM algorithm
     *
     * @param ciphertext encrypted data as byte[]
     * @param cipher     cipher from the biometric prompt
     * @return plaintext as byte[]
     * @throws UnrecoverableEntryException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException                 InvalidKeySpecException,
     *                                     InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
     *                                     BadPaddingException
     */
    public byte[] decrypt(byte[] ciphertext, Cipher cipher)
            throws Exception {
        byte[] nonce = new byte[GCM_IV_LENGTH];
        byte[] encryptedBytes = new byte[ciphertext.length - GCM_IV_LENGTH];

        System.arraycopy(ciphertext, 0, nonce, 0, nonce.length);
        System.arraycopy(ciphertext, nonce.length, encryptedBytes, 0, encryptedBytes.length);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);

        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);

        return plaintext;
    }

}
