package com.example.myapplication.cryptography;


import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class CipherDecipher {

    private final KeyGeneratorForKeyStore gk;

    public CipherDecipher(final KeyGeneratorForKeyStore keyGeneratorForKeyStore) {
        gk = keyGeneratorForKeyStore;
    }

    private SecureRandom random;
    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    private KeyPair akey;
    private KeyPair bkey;
    private byte[] apubKey;
    private byte[] bpubKey;

    static final String TAG = "CipherDechiper Manager";

    KeyPair getAkey() {
        return akey;
    }

    KeyPair getBkey() {
        return bkey;
    }

    public byte[] getPubkeyB() {
        return bpubKey;
    }

    public byte[] getPubkeyA() {
        return apubKey;
    }


    /**
     * @param publicKey    encoded public key
     * @param privKeyAlias private key
     * @return secret key from the shared secret as the byte[]
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws UnrecoverableEntryException
     * @throws InvalidKeySpecException
     */
    public byte[] establishKeyAgreement(byte[] publicKey, String privKeyAlias)
            throws Exception {

        PrivateKey privateKey = gk.getPrivateKey(privKeyAlias);
        KeyAgreement keyAgree = KeyAgreement.getInstance("ECDH");

        keyAgree.init(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("ECDH");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        keyAgree.doPhase(keyFactory.generatePublic(keySpec), true);
        byte[] key = keyAgree.generateSecret();
        Log.d(TAG, "Key agreement established");

        return key;
    }

    public String getPubKeyAString() {

        return Base64.getEncoder().encodeToString(apubKey).substring(36);
    }

    public String getPubKeyBString() {
        return Base64.getEncoder().encodeToString(bpubKey).substring(36);

    }

    /**
     * encrypts plaintext with AES-GSM algorithm
     *
     * @param plaintext      unencrypted info in the byte[] form
     * @param AkeyPairAlias  key alies associated with this secret
     * @param encodedPubKeyB public key
     * @param cipher         cipher from the biometric prompt
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
    public byte[] encrypt(byte[] plaintext, String AkeyPairAlias,
                          byte[] encodedPubKeyB, Cipher cipher)
            throws Exception {

        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);


        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        byte[] key = establishKeyAgreement(encodedPubKeyB, AkeyPairAlias);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");


        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] ciphertext = cipher.doFinal(plaintext);

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        return byteBuffer.array();
    }

    /**
     * decrypts plaintext with AES-GCM algorithm
     *
     * @param ciphertext  encrypted data as byte[]
     * @param alies       alias of the private key associated with the secret
     * @param encodedAKey public key
     * @param cipher      cipher from the biometric prompt
     * @return plaintext as byte[]
     * @throws UnrecoverableEntryException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException                 InvalidKeySpecException,
     *                                     InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
     *                                     BadPaddingException
     */
    public byte[] decrypt(byte[] ciphertext, String alies,
                          byte[] encodedAKey, Cipher cipher)
            throws Exception {

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, ciphertext, 0, GCM_IV_LENGTH);

        byte[] key = establishKeyAgreement(encodedAKey, alies);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);


        byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);

        return plaintext;

    }

}
