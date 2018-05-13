package com.mofangyouxuan.wxapi;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.utils.HttpUtils;
import com.mofangyouxuan.utils.NonceStrUtil;
import com.mofangyouxuan.utils.SignUtils;



/**
 * 订单管理服务
 * @author jeekhan
 *
 */
@Component
public class WXPay {
	private static Logger logger = LoggerFactory.getLogger(WXPay.class);
	@Value("${wxpay.mchtid}")
	public String wxMchtId;	//微信支付商户号
	
	@Value("${wxpay.appid}")
	public String appId;			//微信APPID
	
	@Value("${wxpay.pay-notify-url}")
	public String payNotifyUrl;		//微信支付回调地址

	@Value("${wxpay.refund-notify-url}")
	public String refundNotifyUrl;		//微信退款回调地址
	
	@Value("${wxpay.KEY}")
	public String KEY;			//微信支付商户密钥
	
	@Value("${wxpay.wx-fee-rate}")
	public Double wxFeeRate;		//微信手续费费率

	
	/**
	 * 向微信申请取消订单
	 * 商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
	 * @param flowId
	 * @param userId
	 * @return {errcode:0,errmsg:"ok"}
	 */
	public JSONObject closeOrder(String flowId,UserBasic user) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,String> params = new HashMap<String,String>();
			String nonceStr = NonceStrUtil.getNonceStr(20);
			params.put("appid", appId);
			params.put("mch_id", wxMchtId);
			params.put("nonce_str", nonceStr);
			params.put("sign_type", "MD5");
			params.put("out_trade_no", flowId);	//用户支付流水号
			
			//获取签名
			String sign = signMap(params);
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信关闭预付单，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/pay/closeorder";
			String strRet = HttpUtils.doPostTextSSL(url, root.asXML());
			logger.info("微信关闭预付单，接口返回：" + strRet);
			
			//解析应答
			Document doc = DocumentHelper.parseText(strRet);
			Element xmlElement = doc.getRootElement();
			Map<String,String> retMap = new HashMap<String,String>();
			Node return_code = xmlElement.selectSingleNode("return_code");
			if(return_code != null) {
				retMap.put("return_code", return_code.getText());
			}
			Node return_msg = xmlElement.selectSingleNode("return_msg");
			if(return_msg != null) {
				retMap.put("return_msg", return_msg.getText());
			}
			Node appid = xmlElement.selectSingleNode("appid");
			if(appid != null) {
				retMap.put("appid", appid.getText());
			}
			Node mch_id = xmlElement.selectSingleNode("mch_id");
			if(mch_id != null) {
				retMap.put("mch_id", mch_id.getText());
			}

			Node nonce_str = xmlElement.selectSingleNode("nonce_str");
			if(nonce_str != null) {
				retMap.put("nonce_str", nonce_str.getText());
			}
			Node signNode = xmlElement.selectSingleNode("sign");
	//		if(signNode != null) {
	//			retMap.put("sign", signNode.getText());
	//		}
			Node result_code = xmlElement.selectSingleNode("result_code");
			if(result_code != null) {
				retMap.put("result_code", result_code.getText());
			}
			Node result_msg = xmlElement.selectSingleNode("result_msg");
			if(result_msg != null) {
				retMap.put("result_msg", result_msg.getText());
			}
			Node err_code = xmlElement.selectSingleNode("err_code");
			if(err_code != null) {
				retMap.put("err_code", err_code.getText());
			}			
			Node err_code_des = xmlElement.selectSingleNode("err_code_des");
			if(err_code_des != null) {
				retMap.put("err_code_des", err_code_des.getText());
			}

			if("SUCCESS".equals(return_code.getText())) {//接口返回成功
				//获取签名
				String sign2 = signMap(retMap);
				if(!sign2.equals(signNode.getText())) {
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证不匹配！"));
					logger.info("微信关闭预付单：签名验证失败！");
					return jsonRet;
				}
				//业务判断
				if("SUCCESS".equals(result_code.getText()) || "ORDERCLOSED".equals(retMap.get("err_code"))) {//业务成功
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
				}else {//业务失败
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg"));
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信关闭预付单，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 微信预付单申请
	 * @param order
	 * @param flowId
	 * @param totalAmount	总金额
	 * @param openId
	 * @param ip
	 * @return {errcode,errmsg,prepay_id,code_url}
	 */
	public JSONObject unifiedOrder(Order order,String flowId,Long totalAmount,UserBasic user,String ip) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,String> params = new HashMap<String,String>();
			String nonceStr = NonceStrUtil.getNonceStr(20);
			params.put("appid", appId);
			params.put("mch_id", wxMchtId);
			params.put("device_info", "WEB");
			params.put("nonce_str", nonceStr);
			params.put("sign_type", "MD5");
			params.put("body", order.getPartnerBusiName() + "-" + order.getGoodsName());
			params.put("detail", "<![CDATA[{ \"goods_detail\":" + order.getGoodsSpec() + "]]>");
			//params.put("attach", "");//附加数据
			params.put("out_trade_no", flowId);	//用户支付流水号
			params.put("fee_type", "CNY");		//标价币种
			params.put("total_fee", "" + totalAmount);	//订单总金额，单位为分
			params.put("spbill_create_ip", ip);	//APP和网页支付提交用户端ip
			params.put("time_start", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));	//订单生成时间，格式为yyyyMMddHHmmss
			//params.put("time_expire", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//订单失效时间，格式为yyyyMMddHHmmss
			//params.put("goods_tag", "");	//订单优惠标记，使用代金券或立减优惠功能时需要的参数
			params.put("notify_url", payNotifyUrl);		//异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数
			params.put("trade_type", "JSAPI");			//交易类型:JSAPI-公众号支付,NATIVE-扫码支付,APP-APP支付
			params.put("product_id", order.getGoodsId()+"");
			//params.put("limit_pay", "");		//上传此参数no_credit--可限制用户不能使用信用卡支付
			params.put("openid", user.getOpenId());	//用户标识
			//params.put("scene_info", "");	//该字段用于上报场景信息，目前支持上报实际门店信息。该字段为JSON对象数据，对象格式为{"store_info":{"id": "门店ID","name": "名称","area_code": "编码","address": "地址" }}
			
			//获取签名
			String sign = signMap(params);;
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信申请预付单，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/pay/unifiedOrder";
			String strRet = HttpUtils.doPostTextSSL(url, root.asXML());
			logger.info("微信申请预付单，接口返回：" + strRet);
			
			//解析应答
			Document doc = DocumentHelper.parseText(strRet);
			Element xmlElement = doc.getRootElement();
			Map<String,String> retMap = new HashMap<String,String>();
			for(Object ele : xmlElement.elements()){
				String name = ((Node)ele).getName();
				String value = ((Node)ele).getText();
				retMap.put(name, value);
			}
//			Node return_code = xmlElement.selectSingleNode("return_code");
//			if(return_code != null) {
//				retMap.put("return_code", return_code.getText());
//			}
//			Node return_msg = xmlElement.selectSingleNode("return_msg");
//			if(return_msg != null) {
//				retMap.put("return_msg", return_msg.getText());
//			}
//			Node appid = xmlElement.selectSingleNode("appid");
//			if(appid != null) {
//				retMap.put("appid", appid.getText());
//			}
//			Node mch_id = xmlElement.selectSingleNode("mch_id");
//			if(mch_id != null) {
//				retMap.put("mch_id", mch_id.getText());
//			}
//			Node device_info = xmlElement.selectSingleNode("device_info");
//			if(device_info != null) {
//				retMap.put("device_info", device_info.getText());
//			}
//			Node nonce_str = xmlElement.selectSingleNode("nonce_str");
//			if(nonce_str != null) {
//				retMap.put("nonce_str", nonce_str.getText());
//			}
//			Node signNode = xmlElement.selectSingleNode("sign");
//	//		if(signNode != null) {
//	//			retMap.put("sign", signNode.getText());
//	//		}
//			Node result_code = xmlElement.selectSingleNode("result_code");
//			if(result_code != null) {
//				retMap.put("result_code", result_code.getText());
//			}
//			Node err_code = xmlElement.selectSingleNode("err_code");
//			if(err_code != null) {
//				retMap.put("err_code", err_code.getText());
//			}
//			Node err_code_des = xmlElement.selectSingleNode("err_code_des");
//			if(err_code_des != null) {
//				retMap.put("err_code_des", err_code_des.getText());
//			}
//			Node trade_type = xmlElement.selectSingleNode("trade_type");
//			if(trade_type != null) {
//				retMap.put("trade_type", trade_type.getText());
//			}
//			Node prepay_id = xmlElement.selectSingleNode("prepay_id");
//			if(prepay_id != null) {
//				retMap.put("prepay_id", prepay_id.getText());
//			}
//			Node code_url = xmlElement.selectSingleNode("code_url");
//			if(code_url != null) {
//				retMap.put("code_url", code_url.getText());
//			}
//			 
			String retSign = retMap.remove("sign");
			if("SUCCESS".equals(retMap.get("return_code"))) {//接口返回成功
				//获取签名
				String sign2 = signMap(retMap);
				if(!sign2.equals(retSign) ){
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证比匹配！"));
					logger.info("微信申请预付单：签名验证失败！");
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("prepay_id", retMap.get("prepay_id"));
					jsonRet.put("code_url", retMap.get("code_url"));
				}else {//业务失败
					jsonRet.put("errcode", retMap.get("err_code"));
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg"));
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信申请预付单，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 微信申请退款
	 * 同一退款单号多次请求只退一笔
	 * @param flowId
	 * @param totalAmount
	 * @param wxFinishOrderNo
	 * @return {errcode,errmsg,refund_id,refund_fee}
	 */
	public JSONObject applyRefund(String flowId,Long totalAmount,String wxFinishOrderNo) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,String> params = new HashMap<String,String>();
			String nonceStr = NonceStrUtil.getNonceStr(20);
			params.put("appid", appId);
			params.put("mch_id", wxMchtId);
			params.put("nonce_str", nonceStr);
			params.put("sign_type", "MD5");
			params.put("transaction_id", wxFinishOrderNo);	//微信生成的订单号，在支付通知中有返回
			params.put("out_refund_no", flowId);				//商户系统内部的退款单号
			params.put("total_fee", totalAmount + "");		//订单总金额，单位为分，只能为整数
			params.put("refund_fee", totalAmount + "");	//退款总金额，订单总金额，单位为分，只能为整数
			params.put("refund_fee_type", "CNY" );	//退款货币类型，需与支付一致，或者不填。
			//params.put("refund_desc", "");		//若商户传入，会在下发给用户的退款消息中体现退款原因
			//params.put("refund_account","");	//退款资金来源
			params.put("notify_url", refundNotifyUrl);	//异步接收微信支付退款结果通知的回调地址
			
			//获取签名
			String sign = signMap(params);;
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信申请退款，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
			String strRet = HttpUtils.doPostTextSSL(url, root.asXML());
			logger.info("微信申请退款，接口返回：" + strRet);
			
			//解析应答
			Document doc = DocumentHelper.parseText(strRet);
			Element xmlElement = doc.getRootElement();
			Map<String,String> retMap = new HashMap<String,String>();
			for(Object ele : xmlElement.elements()){
				String name = ((Node)ele).getName();
				String value = ((Node)ele).getText();
				retMap.put(name, value);
			}
		 
			String retSign = retMap.remove("sign");
			if("SUCCESS".equals(retMap.get("return_code"))) {//接口返回成功
				//获取签名
				String sign2 = signMap(retMap);
				if(!sign2.equals(retSign) ){
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证比匹配！"));
					logger.info("微信申请退款：签名验证失败！");
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("refund_id", retMap.get("refund_id"));
					jsonRet.put("refund_fee", retMap.get("refund_fee"));
				}else {//业务失败
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg"));
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信申请退款，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 支付结果查询
	 * @param flowId
	 * @param wxFinishOrderNo
	 * @return
	 */
	public JSONObject queryOrder(String flowId,String wxFinishOrderNo) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,String> params = new HashMap<String,String>();
			String nonceStr = NonceStrUtil.getNonceStr(20);
			params.put("appid", appId);
			params.put("mch_id", wxMchtId);
			params.put("nonce_str", nonceStr);
			params.put("sign_type", "MD5");
			params.put("transaction_id", wxFinishOrderNo);	//微信生成的订单号，在支付通知中有返回
			params.put("out_trade_no",flowId);	//商户系统内部订单号
			
			//获取签名
			String sign = signMap(params);
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信订单查询，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/pay/orderquery";
			String strRet = HttpUtils.doPostTextSSL(url, root.asXML());
			logger.info("微信订单查询，接口返回：" + strRet);
			
			//解析应答
			Document doc = DocumentHelper.parseText(strRet);
			Element xmlElement = doc.getRootElement();
			Map<String,String> retMap = new HashMap<String,String>();
			for(Object ele : xmlElement.elements()){
				String name = ((Node)ele).getName();
				String value = ((Node)ele).getText();
				retMap.put(name, value);
			}
		 
			String retSign = retMap.remove("sign");
			if("SUCCESS".equals(retMap.get("return_code"))) {//接口返回成功
				String strings2 = signMap(retMap);
				//获取签名
				String sign2 = SignUtils.encodeMD5Hex(strings2).toUpperCase();
				if(!sign2.equals(retSign) ){
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证比匹配！"));
					logger.info("微信订单查询：签名验证失败！");
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					if("SUCCESS".equals(retMap.get("trade_state"))) {//已支付成功
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
						jsonRet.put("total_fee", retMap.get("total_fee"));
						jsonRet.put("cash_fee", retMap.get("cash_fee"));
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", retMap.get("trade_state"));
					}
				}else {//业务失败
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg"));
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信订单查询，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 微信退款结果查询
	 * 退款有一定延时，用零钱支付的退款20分钟内到账，银行卡支付的退款3个工作日后重新查询退款状态。
	 * @param flowId
	 * @return
	 */
	public JSONObject queryRefund(String flowId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,String> params = new HashMap<String,String>();
			String nonceStr = NonceStrUtil.getNonceStr(20);
			params.put("appid", appId);
			params.put("mch_id", wxMchtId);
			params.put("nonce_str", nonceStr);
			params.put("sign_type", "MD5");
			params.put("out_refund_no", flowId);		//商户系统内部的退款单号
			//params.put("refund_id",wxFinishRefundNo);		//微信生成的退款单号
			
			//获取签名
			String sign = signMap(params);
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信退款查询，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/pay/refundquery";
			String strRet = HttpUtils.doPostTextSSL(url, root.asXML());
			logger.info("微信退款查询，接口返回：" + strRet);
			
			//解析应答
			Document doc = DocumentHelper.parseText(strRet);
			Element xmlElement = doc.getRootElement();
			Map<String,String> retMap = new HashMap<String,String>();
			for(Object ele : xmlElement.elements()){
				String name = ((Node)ele).getName();
				String value = ((Node)ele).getText();
				retMap.put(name, value);
			}
		 
			String retSign = retMap.remove("sign");
			if("SUCCESS".equals(retMap.get("return_code"))) {//接口返回成功
				//获取签名
				String sign2 = signMap(retMap);
				if(!sign2.equals(retSign) ){
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证比匹配！"));
					logger.info("微信退款查询：签名验证失败！");
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					if("SUCCESS".equals(retMap.get("refund_status_$0"))) {//已退款成功
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
						jsonRet.put("settlement_refund_fee_$0", retMap.get("settlement_refund_fee_$0"));
						jsonRet.put("refund_fee_$0", retMap.get("refund_fee_$0"));
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", retMap.get("refund_status_$0"));
					}
				}else {//业务失败
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg"));
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信退款查询，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	public String signMap(Map<String,String> map) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Set<String> paramKeySet = new TreeSet<String>(map.keySet());
		StringBuilder sb = new StringBuilder();
		for(String key:paramKeySet) {
			sb.append("&" + key + "=" + map.get(key));
		}
		String strings = sb.substring(1) + "&key=" + KEY;
		//获取签名
		String sign = SignUtils.encodeMD5Hex(strings).toUpperCase();
		return sign;
	}
}



