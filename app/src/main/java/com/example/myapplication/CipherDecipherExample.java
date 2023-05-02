package com.example.myapplication;

import java.util.Base64;

public class CipherDecipherExample {
	public static void main(String[] args) throws Exception {
		MsgCipherDecipher msg = new MsgCipherDecipher();
		String stringtext = "Hello";
		System.out.println("Imput text: " + stringtext);
		byte[] text = stringtext.getBytes();

		byte[] ciphertext = msg.encrypt(text, msg.getAkey().getPrivate(), msg.getPubkeyB());
		System.out.println(new String("Ciphertext: " + Base64.getEncoder().encodeToString(ciphertext)));
		byte[] plaintext = msg.decrypt(ciphertext, msg.getBkey().getPrivate(), msg.getPubkeyA());
		String decryptedText = new String(plaintext);
		System.out.println("Decrypted text: " + decryptedText);
		System.out.println(msg.getPubKeyAString().length());
		System.out.println("Public key A: " + msg.getPubKeyAString());
		System.out.println("Public key B: " + msg.getPubKeyBString());
	}
}
