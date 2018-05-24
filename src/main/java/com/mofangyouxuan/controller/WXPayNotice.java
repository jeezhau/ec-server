package com.mofangyouxuan.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.wxapi.WXPay;

@RestController
@RequestMapping("/wxpay/notify")
public class WXPayNotice {
	private static Logger log = LoggerFactory.getLogger(WXPayNotice.class);
	
	@Autowired
	private WXPay wXPay;
	
	/**
	 * 接收微信支付通知
	 * @param is
	 * @return
	 */
	@RequestMapping(value="/pay",method=RequestMethod.POST)
	public String payNotify(InputStream is){
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		Element retXml = DocumentHelper.createElement("xml");
		try {
			String line = "";
			br = new BufferedReader(new InputStreamReader(is,"utf-8"));
			while((line=br.readLine())!=null){
				sb.append(line);
			}
			String recvMsg = sb.toString();
			Document doc = DocumentHelper.parseText(recvMsg);
			Element xmlElement = doc.getRootElement();
            log.info("收到微信支付通知：" + recvMsg);
            JSONObject jsonRet = this.wXPay.payNotice(xmlElement);
            if(jsonRet.getIntValue("errcode") == 0) {
            		retXml.addElement("return_code").add(DocumentHelper.createCDATA("SUCCESS"));
            		retXml.addElement("return_msg").add(DocumentHelper.createCDATA("OK"));
            }else {
            		retXml.addElement("return_code").add(DocumentHelper.createCDATA("FAIL"));
            		retXml.addElement("return_msg").add(DocumentHelper.createCDATA(jsonRet.getString("errmsg")));
            }
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
			retXml.addElement("return_code").add(DocumentHelper.createCDATA("FAIL"));
    			retXml.addElement("return_msg").add(DocumentHelper.createCDATA("系统异常"));
		}
		return retXml.asXML();
	}
	
	/**
	 * 接收微信退款通知
	 * @param is
	 * @return
	 */
	@RequestMapping(value="/refund",method=RequestMethod.POST)
	public String refundNotify(InputStream is){
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		Element retXml = DocumentHelper.createElement("xml");
		try {
			String line = "";
			br = new BufferedReader(new InputStreamReader(is,"utf-8"));
			while((line=br.readLine())!=null){
				sb.append(line);
			}
			String recvMsg = sb.toString();
			Document doc = DocumentHelper.parseText(recvMsg);
			Element xmlElement = doc.getRootElement();
            log.info("收到微信支付通知：" + recvMsg);
            JSONObject jsonRet = this.wXPay.refundNotice(xmlElement);
            if(jsonRet.getIntValue("errcode") == 0) {
            		retXml.addElement("return_code").add(DocumentHelper.createCDATA("SUCCESS"));
            		retXml.addElement("return_msg").add(DocumentHelper.createCDATA("OK"));
            }else {
            		retXml.addElement("return_code").add(DocumentHelper.createCDATA("FAIL"));
            		retXml.addElement("return_msg").add(DocumentHelper.createCDATA(jsonRet.getString("errmsg")));
            }
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
			retXml.addElement("return_code").add(DocumentHelper.createCDATA("FAIL"));
    			retXml.addElement("return_msg").add(DocumentHelper.createCDATA("系统异常"));
		}
		return retXml.asXML();
	}
}
