package com.mofangyouxuan.pay;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.utils.HttpUtils;

@Component
public class AliPay {
	
	private static Logger logger = LoggerFactory.getLogger(AliPay.class);
	@Value("${alipay.notify-server-url}")
	public String notifyServerUrl; //本地接收通知的服务器：给外网使用
	
	@Value("${alipay.return-server-url}")
	public String returnServerUrl; //本地接收通知的服务器：给外网使用
	
	@Value("${alipay.appid}")
	public String APP_ID="2018061260389188";		//支付宝分配给开发者的应用ID
	
	@Value("${alipay.pay-notify-url}")
	public String payNotifyUrl;		//支付宝支付回调地址
	
	@Value("${alipay.pay-return-url}")
	public String payReturnUrl;		//支付返回URL
	
	@Value("${alipay.cert-key-dir}")
	public String certKeyDir;
	@Value("${alipay.seller-email}")
	public String SELLER_EMAIL = "zhaofachun@mofangyouxuan.com";		//收款支付宝账户email
	public String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEI8O4DtgTkKjcf4BBJzpnG9c52xNnOlHu5wK8Qyjs0RImXSD7aXChxrxDyl0KRWhGorPkFZEcCPa5oZyQ4RsfcdVDrIoukHo57XgpUbH+T18fT0I2+bxr+mogZoAMuKzHumf9fuMFcZEnTHUdVwgzKwW0fHuAClopoYmCGhgDvJ6p1YhJTijOPiQlYntWsWNkWFlJ3YSdn6U1UDrzTvd7MQ8VjxWcFBSKZGTOkXSw7+Sqma/VCDq1oAgU26/0gj+4zpXFgBVldntIZ5nTumrfFILp+xzaA+t9Ok3CF2V0EGoNqzgkb/dsoJQGDc1LlaEW3k1HipY2LpqpGYnP907bAgMBAAECggEAOx9xc8oYffCMzVOzA/PUWswHKZjO/rIGdNkhzTBTgFovP8ENf8/2mDu+gqpppK3XcTtdN+E6cwvDsN0Rm3VM2G3rWQM7NIhqV77bs8kc1ceXrP+ehdCJsNpzX5ndE1QR4q+R1cdPNmFQ6/92qzEEtzg5rj7AV1LKcrQXPYIFWq4Gr9xOG/ek9hhuo5tiQI4PvuAXB4BvMzMTBEkKx0qOaCgnHVSyqDdlhjnbyq6KAGZEhghXEnzj5n44dAZhIhDn2YXGme9s/TzzHhLYzPI92tzC6Q8Pk3na0ymRFXX27/wevbK/V/yaudawQGCT/Ah3Vd8ouNjG0fmMvNsgGIoqSQKBgQC+9H9wYmaRPj0uDLDYmLEGLj8h5LKl+XR7NiGKLBA2vMeQkoKNYf6f/MwgII13R2yn/Zi1dEOZVwbrP4ATNe84m1YF/02JXS88AoqMeWPlINT2JHu/7GfLcke0yN+rjBE/ewmw046x5hoPL8/ta/e+OHY19BEt4i7uxuX7yAXLzwKBgQCxJn/H8nLFmZPXz3+acurtsL5uKgqfVP2drfRn+1PUolAc2ZpvcTKyC1NwJLggPPEeK1nBi4DjYFfWFtq5ir3t8RkM71xdtAxmyUgfS6lx7KLMaJbiwEB/XomNRkwpFDTr4/VozkHHhDnLugtOH60VIF5ppzhaFRWoLarvG1BTNQKBgEZ0xhS8aeXLVh2IlzPD2wVRyP+Dd5sf2KehiGyH54+axfOE62CpOJ7lUpfECw5oryGow1CoTkzkvGvOaT4tV0/GmM3rrjsxw7zbny1HmOEw5QLQ4UwmOQHq76Q3vbd5HeATULcKyArBwPm7hXevr6BjCtLLdA8+9lwpzS/CVMknAoGAUWkl9BeBkzm+7cDYHXyOZmmBOlNryklevCYDWf3wSpnFQ1zlUi2tZJE76R+W1onrLTgy6XVY6CWQeDzMi9Qs8LqKDp25zv49bMc3s9orpsVfE51FKjO78Ezb3ebefUph/74lO+L969jiTrPTGjYIbtsPSHKmvQ9PgphqI7Rjt7kCgYEArwJVNJ8aGHC9eQY3yTJRCUKPVttgxZmsytgx1tdfzZrDBuPYx6LKCT2MH1PqaQJKJGRt0IWOqGMHSYPXjsi1DhOGmWMTq4CjNIpQSw8TW9/XwhAPztQgJXgebUG8OquUyZ8w+bp5tbWHBsjzOTQP0YeOhFRtQeS4RXevFiNmt5c=";
	public String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqrFdUy1gA3RN/qwDW1LbDky+EnnV+caQFidn1WwIND/etxDJWzlofJPw8S/MFvHLMpXyBKpGStM83dTVCMzXyhvavn5HzOVfPPtcVQTh8oAjn5N5V5usE04rdpsl96QkyUsUfUO62EsppF+LVBKHgY6hgOwQic89hwR4svA04kGEZtz0bsMiXck9l1tykz0F6yrZ731GtzRVTxWJPvFO40WrnchVH52gjjD7o9rRJ9lWTSK5T8vFNeIgMsNzl7MXTdfW5Ma0r7IE0h32d/nmKDK493PTTC6Yg/jR4+XxlKo0nA1PlGGerFVp7brkbzLyBBVmzSk9EPYpFBIhYURjvwIDAQAB";	
	public String CHARSET = "UTF-8";
	public String SIGN_TYPE = "RSA2";
	
	@Value("${sys.pay-bills-dir}")
	public String payBillsDir="/Users/jeekhan/mfyx/paybills/";	//支付账单保存路径

	@Autowired
	private OrderService orderService;
	
	/**
	 * 向支付宝申请取消订单
	 * 商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
	 * @param flowId
	 * @return {errcode:0,errmsg:"ok"}
	 */
	public JSONObject closeOrder(String flowId) {
		JSONObject jsonRet = new JSONObject();
		try {
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",APP_ID,this.APP_PRIVATE_KEY,"json",this.CHARSET,this.ALIPAY_PUBLIC_KEY,this.SIGN_TYPE);
			AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
			JSONObject bizContent = new JSONObject();
			bizContent.put("out_trade_no", flowId);
			request.setBizContent(bizContent.toJSONString());
			AlipayTradeCloseResponse response = alipayClient.execute(request);
			if(response.isSuccess()){
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			} else {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "支付宝关闭订单：接口调用失败！");
			}
		}catch (AlipayApiException e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常：" + e.getErrCode() + "-" +e.getErrMsg());
		}
		return jsonRet;
	}
	
	/**
	 * 创建阿里支付申请
	 * @param payType	支付方式（31-手机WAP，32-WEB）
	 * @param order		订单信息
	 * @param payFlowId		支付流水号
	 * @param totalAmount	总金额，单位分
	 * @param userIP			支付用户的IP
	 * @return {errcode,errmsg,tradeNo,AliPayForm}
	 */
	public JSONObject createPayApply(String payType,Order order,String payFlowId,BigDecimal totalAmount) {
		JSONObject jsonRet = new JSONObject();
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
		if("31".equals(payType)) {
			AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
			alipayRequest.setReturnUrl(returnServerUrl + payReturnUrl.replace("{orderId}", order.getOrderId()));
			alipayRequest.setNotifyUrl(notifyServerUrl + payNotifyUrl);//在公共参数中设置回跳和通知地址
			JSONObject bizContent = new JSONObject();
			bizContent.put("out_trade_no", payFlowId);
			bizContent.put("total_amount", totalAmount);
			bizContent.put("subject", order.getPartnerBusiName() + "-" + order.getGoodsName());
			//bizContent.put("body", order.getGoodsSpec());
			bizContent.put("product_code", "QUICK_WAP_PAY");
			((AlipayTradeWapPayRequest) alipayRequest).setBizContent(bizContent.toJSONString());//填充业务参数
			try {
				AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);
				if(response.isSuccess()) {
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("AliPayForm", response.getBody());
					jsonRet.put("tradeNo", response.getTradeNo());
				}
			} catch (AlipayApiException e) {
				e.printStackTrace();
				jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "系统异常：" + e.getErrCode() + "-" +e.getErrMsg());
			}
			return jsonRet;
		}else {
			AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
			alipayRequest.setReturnUrl(returnServerUrl +payReturnUrl.replace("{orderId}", order.getOrderId()));
			alipayRequest.setNotifyUrl(notifyServerUrl + payNotifyUrl);//在公共参数中设置回跳和通知地址
			JSONObject bizContent = new JSONObject();
			bizContent.put("out_trade_no", payFlowId);
			bizContent.put("total_amount", totalAmount);
			bizContent.put("subject", order.getPartnerBusiName() + "-" + order.getGoodsName());
			//bizContent.put("body", order.getGoodsSpec());
			bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
		    alipayRequest.setBizContent(bizContent.toJSONString());//填充业务参数
		    try {
		    		AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
				if(response.isSuccess()) {
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("AliPayForm", response.getBody());
					jsonRet.put("tradeNo", response.getTradeNo());
				}
			} catch (AlipayApiException e) {
				e.printStackTrace();
				jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "系统异常：" + e.getErrCode() + "-" +e.getErrMsg());
			}
			return jsonRet;
		}
	}
	
	
	/**
	 * 支付宝申请退款
	 * 同一退款单号多次请求只退一笔
	 * @param flowId
	 * @param totalAmount
	 * @param outPayNo		支付宝支付单号
	 * @return {errcode,errmsg,refund_id,refund_fee}
	 */
	public JSONObject applyRefund(String flowId,BigDecimal totalAmount,String outPayNo) {
		JSONObject jsonRet = new JSONObject();
		try {
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
			AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();//创建API对应的request类
			JSONObject bizContent = new JSONObject();
			bizContent.put("out_request_no", flowId);
			bizContent.put("refund_amount", totalAmount);
			bizContent.put("trade_no", outPayNo);
			request.setBizContent(bizContent.toJSONString());//设置业务参数
			AlipayTradeRefundResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
			logger.info("支付宝申请退款，接口返回：" + response.getBody());
			String refund_fee = response.getRefundFee();
			Long refundFee = new BigDecimal(refund_fee).multiply(new BigDecimal(100)).longValue();
			//根据response中的结果继续业务逻辑处理
			if(response.isSuccess()){
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("refund_id", response.getOutTradeNo());
				jsonRet.put("refund_fee", refundFee);
			}else {//业务失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", response.getMsg());
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("支付宝申请退款，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 退款结果查询
	 * @param flowId		系统退款单号
	 * @param outTradeNo	支付宝支付单号
	 * @return {errcode,errmsg}
	 */
	public JSONObject queryRefund(String flowId,String outTradeNo) {
		JSONObject jsonRet = new JSONObject();
		try {
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
			AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
			JSONObject bizContent = new JSONObject();
			bizContent.put("out_trade_no", flowId);
			bizContent.put("trade_no", outTradeNo);
			request.setBizContent(bizContent.toJSONString());//设置业务参数
			AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
			if(response.isSuccess()){
				String refund_amount = response.getRefundAmount();
				String status = response.getRefundStatus();
				if("REFUND_SUCCESS".equals(status)) {
					Long refundAmount = new BigDecimal(refund_amount).multiply(new BigDecimal(100)).longValue();
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("refund_fee", refundAmount);
				}else {
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", "还未退款从成功！");
				}
			} else {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "支付宝退款查询：接口调用失败！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("支付宝订单查询，出现异常：" + e.getMessage());
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
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
			AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();//创建API对应的request类
			JSONObject bizContent = new JSONObject();
			bizContent.put("out_trade_no", flowId);
			request.setBizContent(bizContent.toJSONString());//设置业务参数
			AlipayTradeQueryResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
			logger.info("支付宝支付查询：" + response.getBody());
			//根据response中的结果继续业务逻辑处理
			String tradeStatus = response.getTradeStatus();
			String total_amount = response.getTotalAmount(); //订单总金额，单位为元
			String transactionId = response.getTradeNo();		//支付宝支付订单号
			Long totalAmount = new BigDecimal(total_amount).multiply(new BigDecimal(100)).longValue();
			//业务判断
			if("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {//已支付成功
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("total_fee", totalAmount);
				jsonRet.put("transaction_id", transactionId);
			}else if("TRADE_CLOSED".equals(tradeStatus)){
				jsonRet.put("errcode", 1);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("total_fee", totalAmount);
				jsonRet.put("transaction_id", transactionId);
			}else {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", response.getMsg());
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("支付宝订单查询，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	
	/**
	 * 支付通知
	 * 支付完成后，支付宝会把相关支付结果和用户信息发送给商户，商户需要接收处理，并返回应答。
	 * 同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。
	 * @param xml	支付宝传送过来的通知信息
	 * @return
	 */
	public String payNotify(Map<String, String> paramsMap) {
		try {
			boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE); //调用SDK验证签名
			if(!signVerified){
				logger.info("支付宝支付通知：签名验证失败！");
				return "failure";
			}
			//按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
			//业务判断
			String appId = paramsMap.get("app_id"); 		//应用appId
			String sellerEmail =  paramsMap.get("seller_email");		//支付宝收款账户
			if(!this.APP_ID.equals(appId) || this.SELLER_EMAIL.equals(sellerEmail)) {
				logger.info("支付宝支付通知：APPID 与 Seller_Emial 验证失败！");
				return "failure";
			}
			String tradeStatus = paramsMap.get("trade_status");
			String payFlowId = paramsMap.get("out_trade_no"); 	//支付订单号
			String total_amount = paramsMap.get("total_amount"); //订单总金额，单位为元
			String transactionId = paramsMap.get("trade_no");		//支付宝支付订单号
			Long totalAmount = new BigDecimal(total_amount).multiply(new BigDecimal(100)).longValue();
			//更新订单支付
			String ret = "";
			if("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {//业务成功	
				ret = orderService.outPaySucc(payFlowId, totalAmount, transactionId);
			}else if("TRADE_CLOSED".equals(tradeStatus)) {//交易关闭
				ret = orderService.closePay(payFlowId, totalAmount, transactionId);
			}else {
				return "failure";
			}
			if("00".equals(ret)) {//处理成功
				return "success";
			}else {
				logger.info("支付宝支付通知：更新订单信息失败！");
				return "failure";
			}
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("支付宝支付通知，出现异常：" + e.getMessage());
		}
		return "failure";
	}
	
	
	/**
	 * 下载支付账单
	 * @param billDate	账单日期
	 * @return {errcode,errmsg}
	 */
	public JSONObject downloadBill(Date billDate) {
		JSONObject jsonRet = new JSONObject();
		try {
			if(billDate == null) {
				billDate = new Date();
			}
			String strBDate = new SimpleDateFormat("yyyy-MM-dd").format(billDate);
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",APP_ID,APP_PRIVATE_KEY,"json",CHARSET,ALIPAY_PUBLIC_KEY,"RSA2");
			AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
			JSONObject bizContent = new JSONObject();
			bizContent.put("bill_type", "trade");
			bizContent.put("bill_date", strBDate);
			request.setBizContent(bizContent.toJSONString());
			AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);
			if(response.isSuccess()){
				String url = response.getBillDownloadUrl();
				int findex = url.indexOf("fileType=") + "fileType=".length();
				int lindex = url.indexOf("&", findex);
				String fileType = url.substring(findex, lindex);
				File file = HttpUtils.downloadFileSSL(this.payBillsDir, url);
				file.renameTo(new File(this.payBillsDir,"alipay"+ new SimpleDateFormat("yyyyMMdd").format(billDate) + "_1." + fileType));
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			} else {
				logger.info("支付宝账单下载，调用失败，失败信息：" + response.getMsg());
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg","支付宝账单下载，调用失败，失败信息：" + response.getMsg());
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			logger.info("支付宝账单下载，出现异常：" + e.getMessage());
		}
		return jsonRet;
	}
	
	public static void main(String[] args) throws Exception {
		AliPay alipay = new AliPay();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, -2);
		alipay.downloadBill(new SimpleDateFormat("yyyy-MM-dd").parse("2018-06-12"));
		
	}
	
}


