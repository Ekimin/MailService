package com.wentuotuo.mail;

import com.wentuotuo.wtt.security.DESEncrypt;

public class DESTools {

	public static String Encrypt(String aStr) {
		if (null == aStr || aStr.equals("")) {
			return "";
		}
		return DESEncrypt.encrypt(aStr);
	}

	public static String Decrypt(String aStr) {
		if (null == aStr || aStr.equals("")) {
			return "";
		}
		return DESEncrypt.decrypt(aStr);
	}

	public static void main(String[] args) {

		String passWd = "heyimin2016";
		String desWd = DESTools.Encrypt(passWd);
		String desWd2 = DESTools.Decrypt(desWd);
		System.out.print(desWd);
		System.out.print("\n");
		System.out.print(desWd2);
	}

}
