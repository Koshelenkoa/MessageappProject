package com.example.myapplication;



import java.io.IOException;
import java.security.SecureRandom;
import java.security.*;
import java.security.cert.CertificateException;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;

import java.nio.ByteBuffer;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.*;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;



public class MsgCipherDecipher{

	 private SecureRandom random;
	 public static final int AES_KEY_SIZE = 256;
	    public static final int GCM_IV_LENGTH = 12;
	    public static final int GCM_TAG_LENGTH = 16;
	    private KeyPair akey;
	    private KeyPair bkey;
	    private byte[] apubKey;
	    private byte[] bpubKey;
	    
	    public KeyPair getAkey() {
	    	return akey;
	    }
	    public KeyPair getBkey() {
	    	return bkey;
	    }
	    public byte[] getPubkeyB() {
	    	return bpubKey;
	    }
	    public byte[] getPubkeyA() {
	    	return apubKey;
	    }
	
	
	
	public static byte[] establishKeyAgreement(byte[] publicKey, String privKeyAlies, String password)
			throws InvalidKeyException, NoSuchAlgorithmException, KeyStoreException,
			CertificateException, IOException, UnrecoverableEntryException,
			InvalidKeySpecException {
			KeyGeneratorForKeyStore gk = new KeyGeneratorForKeyStore();
		PrivateKey privateKey = gk.getPrivateKey(privKeyAlies, password);
		KeyAgreement keyAgree = KeyAgreement.getInstance("ECDH");
		keyAgree.init(privateKey);
		KeyFactory keyFactory = KeyFactory.getInstance("ECDH");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
		keyAgree.doPhase(keyFactory.generatePublic(keySpec), true);
		byte[] key = keyAgree.generateSecret();
		
		return key;
	}
	
	public String getPubKeyAString() {

			return Base64.getEncoder().encodeToString(apubKey).substring(36);
	}
	
	public String getPubKeyBString() {
			return Base64.getEncoder().encodeToString(bpubKey).substring(36);

	}
	

	public byte[] encrypt(byte[] plaintext, String AkeyPairAlias, byte[] encodedPubKeyB, String password)
			throws KeyStoreException, CertificateException, IOException,
			NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableEntryException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException, InvalidKeySpecException {

		byte[] iv = new byte[GCM_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
		
		byte[] key = establishKeyAgreement(encodedPubKeyB, AkeyPairAlias, password);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		
		
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
		byte[] ciphertext = cipher.doFinal(plaintext);
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        return byteBuffer.array();
	}
	
	public byte[] decrypt(byte[] ciphertext, String alies, byte[] encodedAKey, String password)
			throws Exception{
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, ciphertext, 0, GCM_IV_LENGTH);
		
		byte[] key = establishKeyAgreement(encodedAKey, alies, password);
		
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
		
		
		byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);
		
		return plaintext;

		}
	public String returnFinalMessage(String stringtext) throws Exception{
			final String password = null;
		KeyGeneratorForKeyStore gk = new KeyGeneratorForKeyStore();
		gk.generatePairOfKeys("akey");
		gk.generatePairOfKeys("bkey");
		byte[] text = stringtext.getBytes();

		byte[] ciphertext = encrypt(text,"akey", gk.getPublicKey("bkey"), password);
		System.out.println(new String("Ciphertext: " + Base64.getEncoder().encodeToString(ciphertext)));
		byte[] plaintext = decrypt(ciphertext, "bkey", gk.getPublicKey("akey"), password);
		String decryptedText = new String(plaintext);
		String output = "Decrypted text: " + decryptedText.toString() + "\n";
		output += "Public key A: " + Base64.getEncoder().encodeToString(gk.getPublicKey("akey")) + "\n";
		output += "Public key B: " + Base64.getEncoder().encodeToString(gk.getPublicKey("bkey")) + "\n";

		return output;

	}
}
