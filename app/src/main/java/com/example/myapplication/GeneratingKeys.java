package com.example.myapplication;
import static java.security.cert.CertificateFactory.*;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.security.keystore.WrappedKeyEntry;


import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
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
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.KeyPair;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.security.cert.X509Certificate;


public class GeneratingKeys {
private KeyPair keyPair;
private byte[] pubKey;

private KeyStore ks;




        @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.M)
        public void generatePairOfKeys(String alias) throws KeyStoreException, CertificateException, IOException,
                NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
                InvalidKeySpecException {


            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);


            //specifing key generator
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC,
                    "AndroidKeyStore");
            AlgorithmParameterSpec alspec = new ECGenParameterSpec("secp256r1");
            KeyGenParameterSpec keyGenParameterSpec = new;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                new KeyGenParameterSpec.Builder(alias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        //.setUserAuthenticationRequired(true)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setAlgorithmParameterSpec(alspec)
                        .setKeySize(256)
                        .build();
            }
            //generate key pair
            SecureRandom random = new SecureRandom();
                keyPairGenerator.initialize(keyGenParameterSpec, random);
            

            keyPair = keyPairGenerator.generateKeyPair();
            PublicKey pubKeyMaterial =
                    KeyFactory.getInstance(keyPair.getPublic().getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
            byte[] pubKey = keyPair.getPublic().getEncoded();

            Certificate[] cert = ks.getCertificateChain(alias);
            KeyStore.PrivateKeyEntry mPrivatekey = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), cert);

            String password = "changeit";

            KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(password.toCharArray());
            ks.setEntry("privKeyAlias", mPrivatekey, protectionParam);


        }


    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    public PrivateKey getPrivKey(final String alias, final String password) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException {
        KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection( password.toCharArray());
        final PrivateKey key =  ((KeyStore.PrivateKeyEntry)ks.getEntry(alias, protectionParam)).getPrivateKey();

        return key;
    }

    public  PublicKey getPubKey(String alias) throws KeyStoreException {

        final Certificate cert = ks.getCertificate(alias);
        final PublicKey publicKey = cert.getPublicKey();

        return publicKey;
    }

    }

    class TestDrive {
        @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.O)
        public static void main(String[] args) {

            String exceptionMessage;
            GeneratingKeys gk = new GeneratingKeys();
            try {
                gk.generatePairOfKeys("akey");
                gk.getPubKey("akey");
                gk.getPrivKey("akey", "changeit");

            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
        }
    }



