package com.mofangyouxuan.utils;

import java.util.Random;

/**
 * 生成随机字符串
 * @author jeekhan
 *
 */
public class NonceStrUtil {
	
	private static String[] codes = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"
			,"Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9",
			"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p"
			,"q","r","s","t","u","v","w","x","y","z","_","-"};
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
		int len = codes.length;
		Random rand = new Random();
		for(int i=0;i<length;i++) {
			sb.append(codes[rand.nextInt(len)]);
		}
		return sb.toString();
	}

}
