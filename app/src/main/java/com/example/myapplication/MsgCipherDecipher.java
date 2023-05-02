package com.example.myapplication;
import android.os.Build;

import java.security.SecureRandom;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.ECPoint;
import java.util.Base64;
import java.util.Base64.Encoder;
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

	public void establishKeys(int keysize) throws Exception {
		ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256r1");
	      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
	      
	      SecureRandom random = new SecureRandom();
	      keyGen.initialize(ecGenSpec, random);
	      keyGen.initialize(keysize);

	      this.akey = keyGen.generateKeyPair();
	      this.bkey = keyGen.generateKeyPair();
	
	      this.apubKey = akey.getPublic().getEncoded();
	      this.bpubKey = bkey.getPublic().getEncoded();
	      
	}
	
	
	
	public static byte[] genKey(byte[] pubKeyEnc, PrivateKey privkey) throws Exception{
		KeyAgreement keyAgree = KeyAgreement.getInstance("ECDH");
		keyAgree.init(privkey);
		
		//deencoding public key
		X509EncodedKeySpec x509KeySpec  = new X509EncodedKeySpec(pubKeyEnc);
		KeyFactory keyFac = KeyFactory.getInstance("EC");
        PublicKey pubkey = keyFac.generatePublic(x509KeySpec);
        
		keyAgree.doPhase(pubkey, true);
		byte[] key = keyAgree.generateSecret();
		
		return key;
	}
	
	public String getPubKeyAString() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return Base64.getEncoder().encodeToString(apubKey).substring(36);
		}
	}
	
	public String getPubKeyBString() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return Base64.getEncoder().encodeToString(bpubKey).substring(36);
		}
	}
	
	public byte[] encrypt(byte[] plaintext, PrivateKey a, byte[] b) throws Exception {
		
		byte[] iv = new byte[GCM_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
		
		byte[] key = genKey(b, a);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		
		
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
		byte[] ciphertext = cipher.doFinal(plaintext);
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        return byteBuffer.array();
	}
	
	public byte[] decrypt(byte[] ciphertext, PrivateKey b, byte[] a) throws Exception{
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, ciphertext, 0, GCM_IV_LENGTH);
		
		byte[] key = genKey(a, b);
		
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
		
		
		byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);
		
		return plaintext;
	}
}
