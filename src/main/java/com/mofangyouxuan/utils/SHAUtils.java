package com.mofangyouxuan.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class SHAUtils {
	private static final String CHARSET = "utf-8";
	public static String encodeSHAHex(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		byte[] buf = data.getBytes(CHARSET);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] retBuf = md.digest(buf);
		return Hex.encodeHexString(retBuf);
	}
	
	public static String encodeSHA256Hex(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		byte[] buf = data.getBytes(CHARSET);
		MessageDigest md = MessageDigest.getInstance("SHA256");
		byte[] retBuf = md.digest(buf);
		return Hex.encodeHexString(retBuf);
	}

	public static String encodeSHA384Hex(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		byte[] buf = data.getBytes(CHARSET);
		MessageDigest md = MessageDigest.getInstance("SHA-384");
		byte[] retBuf = md.digest(buf);
		return Hex.encodeHexString(retBuf);
	}

	public static String encodeSHA512Hex(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		byte[] buf = data.getBytes(CHARSET);
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] retBuf = md.digest(buf);
		return Hex.encodeHexString(retBuf);
	}

}
