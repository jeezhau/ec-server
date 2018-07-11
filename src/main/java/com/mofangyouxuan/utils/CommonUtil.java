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
	
	/**
	 * 获取指定IP的二进制字符串序列
	 * @param address
	 * @return
	 */
	public static String getIpBinarySeq(String address){
		String[] networkAddressArray = address.split("\\."); 
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < networkAddressArray.length; i++) {
			String str = networkAddressArray[i];
			if(Integer.toBinaryString(Integer.parseInt(str)).length()<8){
				int num = 8-Integer.toBinaryString(Integer.parseInt(str)).length();
				String ipStr1 = "";
				for (int j = 0; j < num; j++) {
					ipStr1 += "0";
				}
				sb.append(ipStr1+(String)Integer.toBinaryString(Integer.parseInt(str)));
			}else{
				sb.append((String)Integer.toBinaryString(Integer.parseInt(str)));
			}
		}
		return sb.toString();
	}
	
	/**
	 * 验证Ip是否在白名单中
	 * @param remoteIp	待验证IP
	 * @param whiteIpSegment	IP段：101.226.103.0/25
	 * @return
	 */
	public static boolean isValidIp(String remoteIp,String whiteIpSegment) {
		String[] whiteIpAndSeg = whiteIpSegment.split("/");
		String whiteIp = whiteIpAndSeg[0];
		Integer maskCnt = new Integer(whiteIpAndSeg[1]);
		String ripseq = getIpBinarySeq(remoteIp);
		String wipseq = getIpBinarySeq(whiteIp);
		if(ripseq.substring(0, maskCnt).equals(wipseq.substring(0, maskCnt))) {
			return true;
		}
		return false;
	}	
	
	/**
	 * 获取用户IP地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		//System.out.println(genOrderId(100002));
		System.out.println(SignUtils.encodeSHA256Hex("123456"));
	}

}
