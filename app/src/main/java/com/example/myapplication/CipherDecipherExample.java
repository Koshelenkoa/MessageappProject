package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import
import java.util.Base64;

public class CipherDecipherExample {


	public static void main(String[] args) {
		MsgCipherDecipher m = new MsgCipherDecipher();
		try {
			System.out.println(m.returnFinalMessage("Hello"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}

