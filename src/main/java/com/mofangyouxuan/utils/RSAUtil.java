package com.mofangyouxuan.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.io.FileUtils;
import org.springframework.util.Base64Utils;

public class RSAUtil {
    private static final String KEY_ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String PRIVATE_KEY = "privateKey";
    private static final int KEY_SIZE = 2048;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";  
    private static final String ENCODE_ALGORITHM = "SHA-256";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    
    /**
     * 生成密钥对
     * 
     * @return
     */
    public static Map<String, byte[]> generateKeyBytes() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, byte[]> keyMap = new HashMap<String, byte[]>();
            keyMap.put(PUBLIC_KEY, publicKey.getEncoded());
            keyMap.put(PRIVATE_KEY, privateKey.getEncoded());
            return keyMap;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void storeKeyPair2Files(String fileDir) throws IOException {
    		Map<String, byte[]> keyMap = generateKeyBytes();
    		byte[] privateKey = keyMap.get(PRIVATE_KEY);
    		byte[] publicKey = keyMap.get(PUBLIC_KEY);
    		if(privateKey != null) {
    			File file = new File(fileDir,"privateKey.pem");
    			String data = Base64Utils.encodeToString(privateKey);
    			FileUtils.writeStringToFile(file, data);
    		}
    		if(publicKey != null) {
    			File file = new File(fileDir,"publicKey.pem");
    			String data = Base64Utils.encodeToString(publicKey);
    			FileUtils.writeStringToFile(file, data);
    		}
    }
    
    /** 
     * 从字符串中加载公钥 
     * 
     * @param publicKeyStr 
     *            公钥数据字符串 
     * @throws Exception 
     *             加载公钥时产生的异常 
     */  
    public static PublicKey loadPublicKey(String publicKeyStr) throws Exception  {  
        try{  
            byte[] buffer = Base64Utils.decodeFromString(publicKeyStr);  
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);  
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);  
        } catch (NoSuchAlgorithmException e){  
            throw new Exception("无此算法");  
        } catch (InvalidKeySpecException e){  
            throw new Exception("公钥非法");  
        } catch (NullPointerException e){  
            throw new Exception("公钥数据为空");  
        }
    }  
  
    /** 
     * 从字符串中加载私钥<br> 
     * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）。 
     * 
     * @param privateKeyStr 
     * @return 
     * @throws Exception 
     */  
    public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception  {  
        try {  
            byte[] buffer = Base64Utils.decodeFromString(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);  
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);  
        } catch (NoSuchAlgorithmException e){  
            throw new Exception("无此算法");  
        } catch (InvalidKeySpecException e){  
            throw new Exception("私钥非法");  
        } catch (NullPointerException e){  
            throw new Exception("私钥数据为空");  
        }  
    }  
  
    /** 
     * 从字符文件中加载公钥 
     * 
     * @param file 公钥文件 
     * @throws Exception 加载公钥时产生的异常 
     */  
    public static PublicKey loadPublicKey(File file) throws Exception {  
        try{  
            return loadPublicKey(readKey(file));  
        } catch (IOException e){  
            throw new Exception("公钥数据流读取错误");  
        } catch (NullPointerException e){  
            throw new Exception("公钥输入流为空");  
        }  
    }  
  
    /** 
     * 从字符文件加载私钥 
     * 
     * @param file 私钥文件 
     * @throws Exception 
     */  
    public static PrivateKey loadPrivateKey(File file) throws Exception  {  
        try{  
            return loadPrivateKey(readKey(file));  
        } catch (IOException e)  {  
            throw new Exception("私钥数据读取错误");  
        } catch (NullPointerException e){  
            throw new Exception("私钥输入流为空");  
        }  
    }  
  
    /** 
     * 读取密钥信息 
     * 
     * @param in 
     * @return 
     * @throws IOException 
     */  
    private static String readKey(File file) throws IOException  {  
    		String key = FileUtils.readFileToString(file);
        return key;  
    }
  
    /**
     * 还原公钥
     * 
     * @param keyBytes
     * @return
     */
    public static PublicKey loadPublicKey(byte[] keyBytes) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 还原私钥
     * 
     * @param keyBytes
     * @return
     */
    public static PrivateKey loadPrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = factory
                    .generatePrivate(pkcs8EncodedKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /** 
     * 用公钥加密 <br> 
     * 每次加密的字节数，不能超过密钥的长度值减去11 
     * 
     * @param data 需加密数据的byte数据 
     * @param publicKey 公钥 
     * @return 加密后的byte型数据 
     */  
    public static byte[] encryptData(byte[] data, PublicKey publicKey)  {  
        try {  
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);  
            // 编码前设定编码方式及密钥  
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
            // 传入编码数据并返回编码结果  
            int inputLen = data.length;    
            ByteArrayOutputStream out = new ByteArrayOutputStream();    
            int offSet = 0;    
            byte[] cache;    
            int i = 0;    
            // 对数据分段加密    
            while (inputLen - offSet > 0) {    
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {    
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);    
                } else {    
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);    
                }    
                out.write(cache, 0, cache.length);    
                i++;    
                offSet = i * MAX_ENCRYPT_BLOCK;    
            }
            byte[] encryptedData = out.toByteArray();    
            out.close();    
            return encryptedData;    
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }
    }  
  
    /** 
     * 用私钥解密 
     * 
     * @param encryptedData 经过encryptedData()加密返回的byte数据 
     * @param privateKey 私钥 
     * @return 
     */  
    public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey)  {  
        try{  
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);  
            cipher.init(Cipher.DECRYPT_MODE, privateKey);  
            return cipher.doFinal(encryptedData);  
        } catch (Exception e){
            e.printStackTrace();  
            return null;  
        }  
    }
    
    /** 
     * 签名 
     * @param privateKey 私钥 
     * @param plain_text 明文 
     * @return 
     */  
    public static byte[] sign(PrivateKey privateKey, String plain_text) {  
        MessageDigest messageDigest;  
        byte[] signed = null;  
        try {  
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);  
            messageDigest.update(plain_text.getBytes());  
            byte[] outputDigest_sign = messageDigest.digest();  
            Signature Sign = Signature.getInstance(SIGNATURE_ALGORITHM);  
            Sign.initSign(privateKey);  
            Sign.update(outputDigest_sign);  
            signed = Sign.sign();  
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {  
            e.printStackTrace();  
        }  
        return signed;  
    }  
  
    /** 
     * 验签 
     *  
     * @param publicKey  公钥 
     * @param plain_text  明文 
     * @param signed  签名 
     */  
    public static boolean verifySign(PublicKey publicKey, String plain_text, byte[] signed) {  
        MessageDigest messageDigest;  
        boolean SignedSuccess=false;  
        try {  
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);  
            messageDigest.update(plain_text.getBytes());  
            byte[] outputDigest_verify = messageDigest.digest();  
            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);  
            verifySign.initVerify(publicKey);  
            verifySign.update(outputDigest_verify);  
            SignedSuccess = verifySign.verify(signed);  
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {  
            e.printStackTrace();  
        }  
        return SignedSuccess;  
    }
    
    public static void main(String[] args) throws Exception {
    		String keyDir = "/Users/jeekhan/alipay/";
//    		File dir = new File(keyDir);
//    		if(!dir.exists()) {
//    			dir.mkdirs();
//    		}
//    		storeKeyPair2Files(keyDir);
    		
    		PrivateKey priKey = loadPrivateKey(new File(keyDir,"privateKey.pem"));
    		String data = "a=123";
    		String ret = Base64Utils.encodeToString(sign(priKey,data));
    		System.out.println(ret);
    		
    }
    
    
    
}
