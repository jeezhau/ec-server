package com.mofangyouxuan.utils;

import java.util.Random;

/**
 * 生成随机字符串
 * @author jeekhan
 *
 */
public class NonceStrUtil {
	
	private static String[] allCodes = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"
			,"Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9",
			"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p"
			,"q","r","s","t","u","v","w","x","y","z","_","-"};
	
	private static String[] numCodes = {"0","1","2","3","4","5","6","7","8","9"};
	
	private static String[] upperChars = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"
			,"Q","R","S","T","U","V","W","X","Y","Z"};
	
	private static String[] lowwerChars = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p"
			,"q","r","s","t","u","v","w","x","y","z"};
	
	
	/**
	 * 生成指定长度的随机字符串
	 * @param length
	 * @return
	 */
	public static String getNonceStr(int length) {
		if(length<1) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int len = allCodes.length;
		Random rand = new Random();
		for(int i=0;i<length;i++) {
			sb.append(allCodes[rand.nextInt(len)]);
		}
		return sb.toString();
	}
	
	/**
	 * 生成指定长度的随机数字字符串
	 * @param length
	 * @return
	 */
	public static String getNonceNum(int length) {
		if(length<1) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int len = numCodes.length;
		Random rand = new Random();
		for(int i=0;i<length;i++) {
			sb.append(numCodes[rand.nextInt(len)]);
		}
		return sb.toString();
	}
	
	/**
	 * 身材随机密码
	 * @param length
	 * @return
	 */
	public static String getNoncePwd(int length) {
		if(length<1) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		Random rand2 = new Random();
		for(int i=0;i<length;i++) {
			int next = rand.nextInt(3);
			if( next == 0) {
				sb.append(lowwerChars[rand2.nextInt(lowwerChars.length)]);
			}else if(next == 1) {
				sb.append(upperChars[rand2.nextInt(upperChars.length)]);
			}else if(next == 2) {
				sb.append(numCodes[rand2.nextInt(numCodes.length)]);
			}else {
				sb.append("#");
			}
		}
		return sb.toString();
	}

}
