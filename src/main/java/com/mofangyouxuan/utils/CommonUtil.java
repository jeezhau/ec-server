package com.mofangyouxuan.utils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {
	
	/**
	 * 生成30位的订单ID
	 * @param userId
	 * @return
	 */
	public static String genOrderId(Integer userId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS"); //17位时间
		String currTime = sdf.format(new Date());
		String uId = userId + "";
		int len = 11 - uId.length();
		for(int i=0;i<len;i++) {	//11位用户
			uId = "0" + uId;
		}
		return currTime + uId + NonceStrUtil.getNonceNum(2);
	}
	
	/**
	 * 生成32位的支付流水ID
	 * @param orderId
	 * @return
	 */
	public static String genPayFlowId(String orderId,String oldId) {
		int next = 0;
		if(oldId != null && oldId.length() == 32) {
			next = Integer.parseInt(oldId.substring(30));
		}
		next += 1;
		if(next<99) {
			return orderId + "0" +next;
		}
		return orderId + next;
	}
	

	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		//System.out.println(genOrderId(100002));
		System.out.println(SignUtils.encodeSHA256Hex("123456"));
	}

}
