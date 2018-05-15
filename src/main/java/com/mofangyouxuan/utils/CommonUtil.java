package com.mofangyouxuan.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {
	
	/**java获取客户端*/
	public static String getPlatform(HttpServletRequest request){
	    /**User Agent中文名为用户代理，简称 UA，它是一个特殊字符串头，使得服务器
	    能够识别客户使用的操作系统及版本、CPU 类型、浏览器及版本、浏览器渲染引擎、浏览器语言、浏览器插件等*/  
	    String agent= request.getHeader("user-agent");
	    //客户端类型常量
	    String type = "";
	    if(agent.contains("iPhone")||agent.contains("iPod")||agent.contains("iPad")){  
	        type = "ios";
	    } else if(agent.contains("Android") || agent.contains("Linux")) { 
	        type = "apk";
	    } else if(agent.indexOf("micromessenger") > 0){ 
	        type = "wx";
	    }else {
	        type = "pc";
	    }
	    return type;
	}
	
	/**
	 * 生成30位的订单ID
	 * @param userId
	 * @return
	 */
	public static String genOrderId(Integer userId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String currTime = sdf.format(new Date());
		String uId = userId + "";
		for(int i=0;i<16-uId.length();i++) {
			uId = "0" + uId;
		}
		return currTime + uId;
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
		if(next<10) {
			return orderId + "0" +next;
		}
		return orderId + next;
	}
	

	

}
