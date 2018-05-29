package com.mofangyouxuan.wxapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.utils.CommonUtil;
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
	@Value("${wxpay.notify-server-url}")
	public String notifyServerUrl; //本地接收通知的服务器：给外网使用
	
	@Value("${wxpay.mchtid}")
	public String wxMchtId;	//微信支付商户号
	
	@Value("${wxpay.appid}")
	public String appId;			//微信公众号APPID
	
	@Value("${wxpay.pay-notify-url}")
	public String payNotifyUrl;		//微信支付回调地址

	@Value("${wxpay.refund-notify-url}")
	public String refundNotifyUrl;		//微信退款回调地址
	
	public String SecretKEY;			//微信支付商户密钥,保存于服务器本地
	@Value("${wxpay.cert-key-dir}")
	public String certKeyDir;
	
	
	@Autowired
	private OrderService orderService;
	
	public String  getSecretKey() {
		if(SecretKEY != null && SecretKEY.length()>0) {
			return SecretKEY;
		}
		File  file = new File(this.certKeyDir + "SecretKey.data");
		List<String> list = null;
		try {
			list = FileUtils.readLines(file, "utf8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//获取密钥
		if(list != null && list.size()>0) {
			SecretKEY = list.get(0).trim();
		}
		return SecretKEY;
	}
	
	/**
	 * 向微信申请取消订单
	 * 商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
	 * @param flowId
	 * @param userId
	 * @return {errcode:0,errmsg:"ok"}
	 */
	public JSONObject closeOrder(String flowId) {
		
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
	 * 创建微信预先支付信息
	 * @param payType	支付方式（21-公众号，22-H5）
	 * @param order		订单信息
	 * @param payFlowId		支付流水号
	 * @param totalAmount	总金额
	 * @param openId			公众号粉丝用户的OPNEID
	 * @param userIP			支付用户的IP
	 * @return {errcode,errmsg,payFlowId,trade_type,prepay_id,code_url,mweb_url}
	 */
	public JSONObject createPrePay(String payType,Order order,String payFlowId,Long totalAmount,String openId,String userIP) {
		JSONObject jsonRet = new JSONObject();
		String tradeType;
		if("21".equals(payType)) {
			tradeType = "JSAPI";
		}else {
			tradeType = "MWEB";	//H5支付
		}
		JSONObject wxRet = unifiedOrder(tradeType,order,payFlowId, totalAmount, openId, userIP);
		if(wxRet.containsKey("prepay_id")) {//成功
			wxRet.put("payFlowId", payFlowId);
			return wxRet;
		}else {//失败
			if("ORDERCLOSED".equals(jsonRet.getString("errcode"))) {//已关闭
				//再次申请
				payFlowId = CommonUtil.genPayFlowId(order.getOrderId(), payFlowId); //生成新流水单号
				wxRet = unifiedOrder(tradeType,order, payFlowId,totalAmount, openId, userIP);
				if(wxRet.containsKey("prepay_id")) {//成功
					wxRet.put("payFlowId", payFlowId);
					return wxRet;
				}else {
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
					return jsonRet;
				}
			}else if("OUT_TRADE_NO_USED".equals(jsonRet.getString("errcode"))) {//微信有，系统无
				wxRet = closeOrder(payFlowId);
				if(wxRet.getIntValue("errcode") == 0) { //关单成功
					//再次申请
					payFlowId = CommonUtil.genPayFlowId(order.getOrderId(), payFlowId);
					wxRet = unifiedOrder(tradeType,order, payFlowId,totalAmount, openId, userIP);
					if(wxRet.containsKey("prepay_id")) {//成功
						wxRet.put("payFlowId", payFlowId);
						return wxRet;
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
						return jsonRet;
					}
				}else {
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
					return jsonRet;
				}
			}else {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
				return jsonRet;
			}
		}
	}
	
	/**
	 * 微信支付预付单申请
	 * @param tradeType	支付类型：JSAPI-公众号支付,NATIVE-扫码支付,APP-APP支付，MWEB-H5支付
	 * @param order		订单信息
	 * @param payFlowId		支付流水号
	 * @param totalAmount	总金额
	 * @param openId			公众号粉丝用户的OPNEID
	 * @param ip				支付用户的IP
	 * @return {errcode,errmsg,prepay_id,code_url,mweb_url}
	 */
	private JSONObject unifiedOrder(String tradeType,Order order,String payFlowId,Long totalAmount,String openId,String ip) {
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
			params.put("out_trade_no", payFlowId);	//用户支付流水号
			params.put("fee_type", "CNY");		//标价币种
			params.put("total_fee", "" + totalAmount);	//订单总金额，单位为分
			params.put("spbill_create_ip", ip);	//APP和网页支付提交用户端ip
			params.put("time_start", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));	//订单生成时间，格式为yyyyMMddHHmmss
			//params.put("time_expire", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//订单失效时间，格式为yyyyMMddHHmmss
			//params.put("goods_tag", "");	//订单优惠标记，使用代金券或立减优惠功能时需要的参数
			params.put("notify_url", notifyServerUrl + payNotifyUrl);		//异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数
			params.put("trade_type", tradeType);			//交易类型:JSAPI-公众号支付,NATIVE-扫码支付,APP-APP支付，MWEB-H5支付
			params.put("product_id", order.getGoodsId()+"");
			//params.put("limit_pay", "");		//上传此参数no_credit--可限制用户不能使用信用卡支付
			if("JSAPI".equals(tradeType)) {
				params.put("openid", openId);	//用户标识
			}
			if("MWEB".equals(tradeType))	{	//H5支付
				params.put("scene_info", "{\"_info\": {\"type\":\"Wap\",\"wap_url\": \"https://.mofangyouxuan.com\",\"wap_name\": \"摩放优选\"}}");	//该字段用于上报场景信息，目前支持上报实际门店信息。该字段为JSON对象数据，对象格式为{"store_info":{"id": "门店ID","name": "名称","area_code": "编码","address": "地址" }}
			}
			//获取签名
			String sign = signMap(params);;
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信统一下单，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			String strRet = HttpUtils.doPostTextSSL(url, root.asXML());
			logger.info("微信统一下单，接口返回：" + strRet);
			
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
					logger.info("微信统一下单：签名验证失败！");
					return jsonRet;
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("trade_type", retMap.get("trade_type")); //调用接口提交的交易类型，取值如下：JSAPI，NATIVE，APP，,H5支付固定传MWEB
					jsonRet.put("prepay_id", retMap.get("prepay_id"));	//微信生成的预支付回话标识，用于后续接口调用中使用，该值有效期为2小时,针对H5支付此参数无特殊用途
					jsonRet.put("code_url", retMap.get("code_url"));	 //trade_type为NATIVE时有返回，用于生成二维码，展示给用户进行扫码支付
					jsonRet.put("mweb_url", retMap.get("mweb_url")); 	//mweb_url为拉起微信支付收银台的中间页面，可通过访问该url来拉起微信客户端，完成支付,mweb_url的有效期为5分钟
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
			logger.info("微信统一下单，出现异常：" + e.getMessage());
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
			params.put("notify_url", notifyServerUrl + refundNotifyUrl);	//异步接收微信支付退款结果通知的回调地址
			
			//获取签名
			String sign = signMap(params);;
			params.put("sign", sign);
			
			Element root = DocumentHelper.createElement("xml");
			for(Map.Entry<String, String> entry:params.entrySet()) {
				root.addElement(entry.getKey()).addText(entry.getValue());
			}
			
			logger.info("微信申请退款，发送请求：" + root.asXML());
			String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
			String strRet = HttpUtils.doPostTextSSL(this.getWXSSLClient(),url, root.asXML());
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
					return jsonRet;
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
	 * @param flowId		系统支付单号
	 * @return {errcode,errmsg,transaction_id,total_fee,cash_fee}
	 */
	public JSONObject queryOrder(String flowId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,String> params = new HashMap<String,String>();
			String nonceStr = NonceStrUtil.getNonceStr(20);
			params.put("appid", appId);
			params.put("mch_id", wxMchtId);
			params.put("nonce_str", nonceStr);
			params.put("sign_type", "MD5");
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
				String genSign = signMap(retMap);//生成签名
				if(!genSign.equals(retSign) ){
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证比匹配！"));
					logger.info("微信订单查询：签名验证失败！");
					return jsonRet;
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					if("SUCCESS".equals(retMap.get("trade_state"))) {//已支付成功
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
						jsonRet.put("total_fee", retMap.get("total_fee"));
						jsonRet.put("transaction_id", retMap.get("transaction_id"));
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
	 * @param flowId		系统退款单号
	 * @return {errcode,errmsg,refund_id,refund_fee}
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
					return jsonRet;
				}
				//业务判断
				else if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					if("SUCCESS".equals(retMap.get("refund_status_0"))) {//已退款成功
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
						jsonRet.put("refund_fee", retMap.get("refund_fee_0"));
						jsonRet.put("refund_id", retMap.get("refund_id_0"));
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", retMap.get("refund_status_0"));
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
	
	/**
	 * 支付通知
	 * 支付完成后，微信会把相关支付结果和用户信息发送给商户，商户需要接收处理，并返回应答。
	 * 同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。
	 * @param xml	微信传送过来的通知信息
	 * @return
	 */
	public JSONObject payNotice(Element xml) {
		JSONObject jsonRet = new JSONObject();
		try {
			//解析通知
			Map<String,String> retMap = new HashMap<String,String>();
			for(Object ele : xml.elements()){
				String name = ((Node)ele).getName();
				String value = ((Node)ele).getText();
				retMap.put(name, value);
			}
			String retSign = retMap.remove("sign");
			if("SUCCESS".equals(retMap.get("return_code"))) {// 通信标识：SUCCESS/FAIL
				//获取签名
				String sign2 = signMap(retMap);
				if(!sign2.equals(retSign) ){
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("签名验证失败！"));
					logger.info("微信支付通知：签名验证失败！");
					return jsonRet;
				}else {
					logger.info("微信支付通知：签名验证成功！");
				}
				//业务判断
				if("SUCCESS".equals(retMap.get("result_code"))) {//业务成功
					String payFlowId = retMap.get("out_trade_no"); //支付订单号
					String total_fee = retMap.get("total_fee"); //订单总金额，单位为分
					String transactionId = retMap.get("transaction_id");		//微信支付订单号
					Long totalAmount = Long.parseLong(total_fee);
					//更新订单支付
					String ret = orderService.outPaySucc(payFlowId, totalAmount, transactionId);
					if("00".equals(ret)) {//处理成功
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", ret);
					}
				}else {//业务失败
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg")); //返回信息，如非空，为错误原因
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信支付通知，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 退款通知
	 * 当商户申请的退款有结果后，微信会把相关结果发送给商户，商户需要接收处理，并返回应答。 
	 * 同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。
	 * 退款结果对重要的数据进行了加密，商户需要用商户秘钥进行解密后才能获得结果通知的内容
	 * @param xml	微信传送过来的通知信息
	 * @return
	 */
	public JSONObject refundNotice(Element xml) {
		JSONObject jsonRet = new JSONObject();
		try {
			//解析通知
			Map<String,String> retMap = new HashMap<String,String>();
			for(Object ele : xml.elements()){
				String name = ((Node)ele).getName();
				String value = ((Node)ele).getText();
				retMap.put(name, value);
			}
			String retSign = retMap.remove("sign");
			if("SUCCESS".equals(retMap.get("return_code"))) {// 通信标识：SUCCESS/FAIL
				//获取签名并验证
				if(retSign != null) {
					String sign2 = signMap(retMap);
					if(!sign2.equals(retSign) ){
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", retMap.get("签名验证失败！"));
						logger.info("微信退款通知：签名验证失败！");
						return jsonRet;
					}
				}
				//业务判断
				String secrectReqInfo = retMap.remove("req_info");  //加密信息
				//（1）对加密串A做base64解码，得到加密串B
				//（2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
				//（3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
				//解密
				byte[] desecInfo = Base64.decodeBase64(secrectReqInfo.getBytes("utf8"));
				String signKey = SignUtils.encodeMD5Hex(this.getSecretKey()).toLowerCase();
				byte[] byteDesecInfo = decryptAES(signKey.getBytes("utf8"), desecInfo);
				String retStr = new String(byteDesecInfo,"utf8");
				logger.info("退款通知解析结果：" + retStr);
				Document doc = DocumentHelper.parseText(retStr);
				Element element = doc.getRootElement();
				retMap = new HashMap<String,String>();
				for(Object ele : element.elements()){
					String name = ((Node)ele).getName();
					String value = ((Node)ele).getText();
					retMap.put(name, value);
				}
				if("SUCCESS".equals(retMap.get("refund_status"))) {//退款成功
					String payFlowId = retMap.get("out_refund_no"); //支付订单号
					String refund_fee = retMap.get("settlement_refund_fee"); //退款金额
					String refundId = retMap.get("refund_id");		//微信退款单号
					Long totalAmount = Long.parseLong(refund_fee);
					//更新订单支付
					String ret = orderService.outRefundSucc(payFlowId, totalAmount, refundId);
					if("00".equals(ret)) {//处理成功
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", ret);
					}
				}else {//业务失败
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", retMap.get("err_code_des"));
				}
			}else {//接口返回失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", retMap.get("return_msg")); //返回信息，如非空，为错误原因
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("微信退款通知，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 生成签名
	 * @param map	待签名数据
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public String signMap(Map<String,String> map) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Set<String> paramKeySet = new TreeSet<String>(map.keySet());
		StringBuilder sb = new StringBuilder();
		for(String key:paramKeySet) {
			String val = map.get(key);
			if(val != null && val.length()>0) {
				sb.append("&" + key + "=" + val);
			}
		}
		String strings = sb.substring(1) + "&key=" + getSecretKey();
		//获取签名
		String sign = SignUtils.encodeMD5Hex(strings).toUpperCase();
		return sign;
	}
	
    
	private CloseableHttpClient wxSSLHttpClient = null;
    /**
     * 获取微信访问的SSL链接客户端，附带证书
     * @return
     * @throws Exception 
     */
	public CloseableHttpClient getWXSSLClient() throws Exception{
		if(wxSSLHttpClient != null) {
			return wxSSLHttpClient;
		}
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File(this.certKeyDir + "/apiclient_cert.p12")), this.wxMchtId.toCharArray());
        SSLContext sslcontext = SSLContexts.custom()
                //加载服务端提供的truststore(如果服务器提供truststore的话就不用忽略对服务器端证书的校验了)
                //.loadTrustMaterial(new File("D:\\truststore.jks"), "123456".toCharArray(),
                //        new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, wxMchtId.toCharArray())
                .build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1"}, 
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient wxSSLHttpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();

        return wxSSLHttpClient;
	}
	

    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";
	/**
	 * 解密
	 * @param content  待解密内容
	 * @return
	 */
	public static byte[] decryptAES(byte[] key,byte[] data) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, "AES");
	    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM,"BC");
	    cipher.init(Cipher.DECRYPT_MODE, secretKey);
	    return cipher.doFinal(data);
	}
	static {  
		Security.addProvider(new BouncyCastleProvider()); 
	}
	
	public static void main(String[] args) throws Exception {
		String str = "<xml><return_code>SUCCESS</return_code><appid><![CDATA[wx34a58dad7a6542ca]]></appid><mch_id><![CDATA[1503812541]]></mch_id><nonce_str><![CDATA[b57912754356f8a562c2f550b5a3a195]]></nonce_str><req_info><![CDATA[L6QuVIicbZ8WmxteS6/ITA3a7jUzchfHeT8reXQov+8NrG6kxC4AUi1edErJOzwbqSE8T8QMY8Ua8KqkHKzIBimWvLKT3/dbum1UaicL9IJW+xRQ0Ut5PsJR0Iki8j+5LA5zusOfxQFMl4WkeZSseUG4WuhBCXACTLE8R2BXUufyrpoaFGkTQt7hXJD1I08JfaxliN3h9TbpT7f59dbrVQHDHXh1DPZVGzJdbUaTTa+6k8MRHeaT2nDz7gqPUkHqkcy86GEdzYcUift4SMRoh2cuX3T1oaczFsY9wHPKr3NOfdK7KVmDgv9T1S4r6TyCuZscyq/PNwAc0VQuJnesZrbHIsFuF5h6T/qcTMfTxuRjchp1axOmkur8WlpilLsmPp2r6+ZjyvTCkSJJXCxbiNFoz6R40qXxeC/ReYxrIqLZHuuoTruDGdXcXq+SQ4gFhuaKudeslhHX+Sf9WmEec233AiVKPDMq/3JxmHMHj7QR8nm/EX0NVo8bIQkTpYQ0cF8ukvPzV1/TmVEyvrPoYgRFVDOW2G9oNuAmKW+jzX7A9l9ky0OvQOSHT6IkhO8hyP2jAer4oK/+KbCah6RJFslsjJJap2mqI3fFUxsFdI30HF5txxYoCLGfF53wz2e1rQaC7QVKSYbK+/Keinfj5DOyUlZqbuTdvyy/eAjUsiX/aNK2FyfeajZvFjQ5JZYz+tzbzp8B31h+1xImB5kJgHUa6qpAHrAUmU8kEm73LQ34dwkYciQfv3etU/mHABYw2G9iF3CcH93AtvHnI/m3dG79Y92FKDbHVqMpMH9Hjmg4S4l9XaZDflRfGo3Mc+gEAGE0zNEiybV2rCVzdU4KQvXLbRNTYSXy7kdneACZKmJqh2QcMaKorEOx3+Yfw1BgiU89dLXrBtLSlWJlRD22+XuJHL/ZwaM0wRpgoxySmBeoR4b5qFjvV7fJXH/tjov8x6BllsMDNpIfNrvS0YjUliieKmgUQgan8n4ZbSOzO+uPcpu3TrLRnPRl6+DUbE9k0od50PmOpwXWx84XUa6CX4EVUtf5pFs7FLOvcUgPU/JVVqu8igMnXH8SBrNqNuQ7iwcXkhG1Ls13qyuwwMUUJg==]]></req_info></xml>";
		Document doc = DocumentHelper.parseText(str);
		Element xml = doc.getRootElement();
		Map<String,String> retMap = new HashMap<String,String>();
		for(Object ele : xml.elements()){
			String name = ((Node)ele).getName();
			String value = ((Node)ele).getText();
			retMap.put(name, value);
		}
		String secrectReqInfo = retMap.remove("req_info");  //加密信息
		byte[] desecInfo = Base64.decodeBase64(secrectReqInfo.getBytes("utf8"));
		String signKey = SignUtils.encodeMD5Hex("qerwseqsDRTdg455656564cxgxgggddd").toLowerCase();
		byte[] byteDesecInfo = decryptAES(signKey.getBytes("utf8"), desecInfo);
		String retStr = new String(byteDesecInfo,"utf8");
		System.out.println("退款通知解析结果：" + retStr);
	}
}



