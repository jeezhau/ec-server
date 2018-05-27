package com.mofangyouxuan.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Hex;

public abstract class DesUtils {
	/**
	 * SunJCE 仅支持112和168位的秘钥；填充方式支持PKCS5Padding而不支持PKCS7Padding
	 */
	public static final String KYE_ALGORITHM = "DESede";
	
	public static final String CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding";
	
	public static final String BC_CIPHER_ALGORITHM = "DESede/ECB/PKCS7Padding";
	
	private static String HEX_KEY = "c10b5797a83be0a702753d61f2f41a4a7c7f3ecb804fea49"; //默认密钥(16进制)
	
	public static byte[] initKey() throws Exception{
		KeyGenerator keyGen = KeyGenerator.getInstance(KYE_ALGORITHM, "SunJCE");
		keyGen.init(168);
		SecretKey key = keyGen.generateKey();
		return key.getEncoded();
	}
	
	/**
	 * 生成16进制密钥
	 * @return
	 * @throws Exception
	 */
	public static String initKeyHex() throws Exception{
		KeyGenerator keyGen = KeyGenerator.getInstance(KYE_ALGORITHM, "SunJCE");
		keyGen.init(168);
		SecretKey key = keyGen.generateKey();
		byte[] keyCode = key.getEncoded();
		String str = Hex.encodeHexString(keyCode);
		return str;
	}
	
	
	public static byte[] initBCKey() throws Exception{
		KeyGenerator keyGen = KeyGenerator.getInstance(KYE_ALGORITHM, "BC");
		keyGen.init(192);
		SecretKey key = keyGen.generateKey();
		return key.getEncoded();
	}
	
	
	public static Key toKey(byte[] keyCode) throws Exception{
		DESedeKeySpec spec = new DESedeKeySpec(keyCode);
		SecretKeyFactory fac = SecretKeyFactory.getInstance(KYE_ALGORITHM, "SunJCE");
		return fac.generateSecret(spec);
	}
	
	public static Key toKey(String key) throws Exception{
		byte[] keyCode = Hex.decodeHex(key.toCharArray());
		DESedeKeySpec spec = new DESedeKeySpec(keyCode);
		SecretKeyFactory fac = SecretKeyFactory.getInstance(KYE_ALGORITHM, "SunJCE");
		return fac.generateSecret(spec);
	}
	
	public static Key toBCKey(byte[] keyCode) throws Exception{
		DESedeKeySpec spec = new DESedeKeySpec(keyCode);
		SecretKeyFactory fac = SecretKeyFactory.getInstance(KYE_ALGORITHM, "BC");
		return fac.generateSecret(spec);
	}
	
	public static byte[] encrypt(byte[] input,byte[] keyCode) throws Exception{
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "SunJCE");
		Key key = toKey(keyCode);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(input);
	}
	
	/**
	 * 使用默认密钥进行加密
	 * @param data	待加密数据
	 * @return
	 * @throws Exception
	 */
	public static String encryptHex(String data) throws Exception{
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "SunJCE");
		Key key = toKey(HEX_KEY);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] input = data.getBytes("utf-8");
		byte[] buf = cipher.doFinal(input);
		return Hex.encodeHexString(buf);
	}
	
	public static byte[] decrypt(byte[] input,byte[] keyCode) throws Exception{
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "SunJCE");
		Key key = toKey(keyCode);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(input);
	}
	
	public static byte[] encryptBC(byte[] input,byte[] keyCode) throws Exception{
		Cipher cipher = Cipher.getInstance(BC_CIPHER_ALGORITHM, "BC");
		Key key = toKey(keyCode);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(input);
	}
	
	public static byte[] decryptBC(byte[] input,byte[] keyCode) throws Exception{
		Cipher cipher = Cipher.getInstance(BC_CIPHER_ALGORITHM, "BC");
		Key key = toKey(keyCode);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(input);
	}
	/**
	 * 使用默认密钥解密
	 * @param data 待解密数据(16进制)
	 * @return
	 * @throws Exception
	 */
	public static String decryptHex(String data) throws Exception{
		byte[] input = Hex.decodeHex(data.toCharArray());
		byte[] keyCode = Hex.decodeHex(HEX_KEY.toCharArray());
		byte[] buf = decrypt(input,keyCode);
		String str = new String(buf,"utf-8");
		return str;
	}
	
	public static void main(String[] args) throws Exception{
		String username = "leyi";
		String pwd_test = "leyipwd";
		String db_url = "jdbc:mysql://localhost:3306/leyi";
		
		String sc_db_url;
		String sc_username;
		sc_db_url = "d35866b4fd23238341f0aeab6a35e04000110b95e8598246c8763089e415ae0f13926547afca876c";
		sc_username = "2b73b8408410a8f9";
		db_url = decryptHex(sc_db_url);
		username = decryptHex(sc_username);
		System.out.println(db_url);
		System.out.println(username);
	}
	
}
