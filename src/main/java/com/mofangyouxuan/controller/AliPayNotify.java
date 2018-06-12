package com.mofangyouxuan.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mofangyouxuan.pay.AliPay;

@RestController
@RequestMapping("/alipay")
public class AliPayNotify {
	private static Logger log = LoggerFactory.getLogger(AliPayNotify.class);
	
	@Autowired
	private AliPay aliPay;
	
	/**
	 * 接收支付宝支付通知
	 * @param is
	 * @return
	 */
	@RequestMapping(value="/notify",method=RequestMethod.POST)
	public String payNotify(HttpServletRequest request){
		Map<String,String> paramsMap = new HashMap<String,String>();
		try {
			Map<String,String[]> params = request.getParameterMap();
			for(Map.Entry<String, String[]> entry:params.entrySet()) {
				paramsMap.put(entry.getKey(), entry.getValue()[0]);
			}
			String ret = this.aliPay.payNotify(paramsMap);
			return ret;
		} catch (Exception e) {
			log.info("支付宝支付通知，系统异常：" + e.getMessage());
			e.printStackTrace();
			return "failure";
		}
	}
	
}
