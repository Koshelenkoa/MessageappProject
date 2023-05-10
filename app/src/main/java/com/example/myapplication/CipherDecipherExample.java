package com.example.myapplication;

import android.os.Build;

import java.util.Base64;

public class CipherDecipherExample {


	@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.O)
	@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.M)
	public static void main(String[] args)  {
		try{MsgCipherDecipher msg = new MsgCipherDecipher();
		GeneratingKeys gk = new GeneratingKeys();
		gk.generatePairOfKeys("akey");
		gk.generatePairOfKeys("bkey");
		gk.getPubKey("akey");
		gk.getPrivKey("akey", "changeit");
			gk.getPubKey("akey");
			gk.getPrivKey("akey", "changeit");
		String stringtext = "Hello";
		System.out.println("Imput text: " + stringtext);
		byte[] text = stringtext.getBytes();

		byte[] ciphertext = msg.encrypt(text, msg.getAkey().getPrivate(), msg.getPubkeyB());
		System.out.println(new String("Ciphertext: " + Base64.getEncoder().encodeToString(ciphertext)));
		byte[] plaintext = msg.decrypt(ciphertext, msg.getBkey().getPrivate(), gk.getPubKey("akey"));
		String decryptedText = new String(plaintext);
		System.out.println("Decrypted text: " + decryptedText);
		System.out.println(msg.getPubKeyAString().length());
		System.out.println("Public key A: " + gk.getPubKey("akey"));
		System.out.println("Public key B: " + gk.getPubKey("bkey"));}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
}
