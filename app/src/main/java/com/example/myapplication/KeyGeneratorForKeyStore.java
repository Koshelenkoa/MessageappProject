package com.example.myapplication;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import java.security.cert.CertificateFactory;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.KeyPair;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;



public class KeyGeneratorForKeyStore {
private KeyPair keyPair;
private byte[] pubKey;

private KeyStore ks;





        public void generatePairOfKeys(String alias)
                throws KeyStoreException, CertificateException, IOException,
                NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
                InvalidKeySpecException {


            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);


            //specifing key generator
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC,
                    "AndroidKeyStore");
            AlgorithmParameterSpec alspec = new ECGenParameterSpec("secp256r1");
            KeyGenParameterSpec keyGenParameterSpec =

            new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setAlgorithmParameterSpec(alspec)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(false)
                    .build();
            //generate key pair
            SecureRandom random = new SecureRandom();
                keyPairGenerator.initialize(keyGenParameterSpec, random);
            

            keyPair = keyPairGenerator.generateKeyPair();
            PublicKey pubKeyMaterial =
                    KeyFactory.getInstance(keyPair.getPublic().getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
            byte[] pubKey = keyPair.getPublic().getEncoded();

            Certificate[] cert = ks.getCertificateChain(alias);
            KeyStore.PrivateKeyEntry mPrivatekey =
                    new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), cert);

            String password = null;

            KeyStore.ProtectionParameter protectionParam =
                    new KeyStore.PasswordProtection(null);
            ks.setEntry("privKeyAlias", mPrivatekey, protectionParam);


        }



     public PrivateKey getPrivateKey(final String alias, final String password)
             throws KeyStoreException, UnrecoverableEntryException,
             NoSuchAlgorithmException, CertificateException, IOException {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        KeyStore.ProtectionParameter protectionParam =
                new KeyStore.PasswordProtection();
        final PrivateKey key =
                ((KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, protectionParam)).getPrivateKey();

        return key;
    }

    static public byte[] getPublicKey(String alias)
            throws CertificateException, IOException,
            NoSuchAlgorithmException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Certificate cert = keyStore.getCertificate(alias);

    byte[] publicKey = cert.getPublicKey().getEncoded();
            return publicKey;
    }
}





