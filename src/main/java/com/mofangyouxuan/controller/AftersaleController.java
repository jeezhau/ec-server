package com.mofangyouxuan.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.Aftersale;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.AftersaleService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.SignUtils;

/**
 * 售后服务管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/aftersale")
public class AftersaleController {
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private AftersaleService aftersaleService;

	@Autowired
	private SysParamUtil sysParamUtil;
	
	/**
	 * 买家申请退款(退货)
	 * 1、申请退货，先提交等待卖家处理；
	 * 2、卖家同意退货，则发送物流信息；
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @param type		退款类型(1-未收到货，3-签收退款：品质与描述问题或无理由退货)
	 * @param reason		退款理由，签收退货包含快递信息{reason,dispatchMode,logisticsComp,logisticsNo}
	 * @param passwd		会员操作密码，会员需要密码
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{userId}/refund/{orderId}")
	public Object applyRefund(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId,
			@RequestParam(value="type",required=true)String type,
			@RequestParam(value="content",required=true)String content,
			@RequestParam(value="passwd")String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			if(!"1".equals(type) && !"3".equals(type)) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "退款类型：取值不正确！");
				return jsonRet.toJSONString();
			}
			content = content.trim();
			JSONObject asCtn = JSONObject.parseObject(content);
			if(asCtn.getString("reason") != null && asCtn.getString("reason").length()>1000) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "退款理由：最长1000字符！");
				return jsonRet.toJSONString();
			}
			
			VipBasic userVip = this.vipBasicService.get(userId);
			Order order = this.orderService.get(orderId);
			PartnerBasic partner = this.partnerBasicService.getByID(order.getPartnerId());
			if(userVip == null || order == null || partner == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!userVip.getVipId().equals(order.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			
			//密码检查
			if("1".equals(userVip.getStatus())) {
				if(passwd == null || passwd.length()<6) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您的会员操作密码不可为空！");
					return jsonRet.toJSONString();
				}
				//密码验证
				if(userVip.getPasswd() == null || userVip.getPasswd().length()<10) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您还未设置会员操作密码，请先到会员中心完成设置！");
					return jsonRet.toJSONString();
				}
				if(!SignUtils.encodeSHA256Hex(passwd).equals(userVip.getPasswd())) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您的会员操作密码输入不正确！");
					return jsonRet.toJSONString();
				}
			}
			//支付流水检查
			PayFlow payFlow = this.orderService.getLastedFlow(orderId, "1");
			if(payFlow == null || !"11".equals(payFlow.getStatus())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的订单没有支付成功信息（未支付到账、支付失败）！");
				return jsonRet.toJSONString();
			}
			payFlow = this.orderService.getLastedFlow(orderId, null);
			if(payFlow == null || (!"11".equals(payFlow.getStatus()) && !"F2".equals(payFlow.getStatus()))) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg","系统没有您的支付流水信息！");
				return jsonRet;
			}
			
			//订单与退款类型检查
			if("1".equals(type)) {//买家未收到货
				//订单状态检查
				if(!order.getStatus().startsWith("2") && !"30".equals(order.getStatus()) && !"55".equals(order.getStatus()) && !"67".equals(order.getStatus())) {
					jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
					jsonRet.put("errmsg", "您当前不可执行未收到货退款申请操作！");
					return jsonRet.toJSONString();
				}
				if("20".equals(order.getStatus()) || "21".equals(order.getStatus())) {//未发货
					Date payTime = payFlow.getIncomeTime();
					Long d = (new Date().getTime()-payTime.getTime())/1000/3600/24;
					if(d < sysParamUtil.getNoDeliveryDates4Refund()) {
						jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
						jsonRet.put("errmsg", "还未到最后发货时间限制，您可与卖家联系，然后执行订单取消！");
						return jsonRet.toJSONString();
					}
				}else if("22".equals(order.getStatus()) || "30".equals(order.getStatus()) || "55".equals(order.getStatus())) {//发货未收到
					Long d = null;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Aftersale aftersale = this.aftersaleService.getByID(orderId);
					if("22".equals(order.getStatus()) || "30".equals(order.getStatus())) {
						d = (new Date().getTime() - sdf.parse(order.getSendTime()).getTime())/1000/3600/24;
					}else {
						d = (new Date().getTime() - sdf.parse(aftersale.getDealTime()).getTime())/1000/3600/24;
					}
					if(d < sysParamUtil.getNoSignDates4Refund()) {
						jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
						jsonRet.put("errmsg", "还未到最后收货时间限制，您不可申请退款，您可与卖家联系要求作出处理！");
						return jsonRet.toJSONString();
					}
					type = "2";  //发货未收到
				}
			}else {//签收后退货
				//订单状态检查
				if(!"31".equals(order.getStatus()) && !order.getStatus().startsWith("4") &&
						!"56".equals(order.getStatus()) && !"57".equals(order.getStatus()) && !"58".equals(order.getStatus()) &&
						!"61".equals(order.getStatus()) && !"67".equals(order.getStatus())) {
					jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
					jsonRet.put("errmsg", "您当前不可执行退货退款申请操作！");
					return jsonRet.toJSONString();
				}
			}
			if("61".equals(order.getStatus())) { //卖家同意退货
				if(null == asCtn.getInteger("dispatchMode") || asCtn.getInteger("dispatchMode") < 1 || asCtn.getInteger("dispatchMode") > 4) {
					jsonRet.put("errmsg", "配送类型不正确(1-官方统一配送、2-商家自行配送、3-快递配送、4-自取)！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
				if(asCtn.getString("logisticsComp") == null || asCtn.getString("logisticsComp").length()<1) {
					jsonRet.put("errmsg", "配送方名称不可为空！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
				if(asCtn.getString("logisticsNo") == null || asCtn.getString("logisticsNo").length()<1) {
					jsonRet.put("errmsg", "物流单号不可为空！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else {
				if(asCtn.getString("reason") == null || asCtn.getString("reason").length()<3) {
					jsonRet.put("errmsg", "退款理由：不可少于3个字符！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}
			//卖家账户检查
			VipBasic mchtVip = this.vipBasicService.getVipBal(partner.getVipId());
			if(mchtVip == null || 
					mchtVip.getBalance() < payFlow.getPayAmount() ) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "您当前不可执行退货退款申请操作(卖家账户可用余额不足，请与卖家联系)！");
				return jsonRet.toJSONString();
			}
			
			String typeStr = "";
			if("1".equals(type)) {
				typeStr = "卖家未发货，买家申请退款";
			}else if("2".equals(type)) {
				typeStr = "卖家已发货，买家超时未收到货申请退款";
			}else {
				typeStr = "买家已签收，申请退货退款";
			}
			Date currTime = new Date();
			Order updOdr = new Order();
			if(!"61".equals(order.getStatus())) {
				updOdr.setStatus("60"); //60:提出申请
			}else {
				updOdr.setStatus("62"); //62:提交退货物流
			}
			updOdr.setOrderId(order.getOrderId());
			Aftersale aftersale = this.aftersaleService.getByID(orderId);
			if(aftersale == null) {
				aftersale = new Aftersale();
			}
			aftersale.setGoodsId(order.getGoodsId());
			aftersale.setOrderId(orderId);
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currTime));
			asr.put("type", typeStr);
			asr.put("content", asCtn);
			String oldAsr = aftersale.getApplyReason()==null ? "[]" : aftersale.getApplyReason();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(asr);
			aftersale.setApplyReason(asrArr.toJSONString());
			int cnt = this.orderService.update(updOdr);
			if(cnt >0) {
				jsonRet = this.aftersaleService.saveAF(aftersale);
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库保存数据出错！");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
		
	}
	
	
	/**
	 * 买家申请换货
	 * 
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @param content		换货理由，包含快递信息{reason,dispatchMode,logisticsComp,logisticsNo}
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{userId}/exchange/{orderId}")
	public Object exchange(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId,
			@RequestParam(value="content",required=true)String content) {
		JSONObject jsonRet = new JSONObject();
		try {
			content = content.trim();
			JSONObject asCtn = JSONObject.parseObject(content);
			if(asCtn.getString("reason") != null && asCtn.getString("reason").length()>1000) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "换货理由：最长1000字符！");
				return jsonRet.toJSONString();
			}
			UserBasic user = this.userBasicService.get(userId);
			Order order = this.orderService.get(orderId);
			if(user == null || order == null ) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!user.getUserId().equals(order.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			if(!order.getStatus().equals("40") && !order.getStatus().equals("41") && !order.getStatus().equals("51")) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "该订单当前不可申请换货（未签收）！");
				return jsonRet.toJSONString();
			}
			if(order.getStatus().equals("51")) {
				if(null == asCtn.getInteger("dispatchMode") || asCtn.getInteger("dispatchMode") < 1 || asCtn.getInteger("dispatchMode") > 4) {
					jsonRet.put("errmsg", "配送类型不正确(1-官方统一配送、2-商家自行配送、3-快递配送、4-自取)！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
				if(asCtn.getString("logisticsComp") == null || asCtn.getString("logisticsComp").length()<1) {
					jsonRet.put("errmsg", "配送方名称不可为空！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
				if(asCtn.getString("logisticsNo") == null || asCtn.getString("logisticsNo").length()<1) {
					jsonRet.put("errmsg", "物流单号不可为空！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else {
				if(asCtn.getString("reason") == null || asCtn.getString("reason").length()<3) {
					jsonRet.put("errmsg", "退款理由不可少于3个字符！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}
			//更新订单信息
			Date currTime = new Date();
			Order updOdr = new Order();
			if(!order.getStatus().equals("51")) {
				updOdr.setStatus("50"); //50:提出申请
			}else {
				updOdr.setStatus("52"); //52:提交发货物流
			}
			updOdr.setOrderId(order.getOrderId());
			Aftersale aftersale = this.aftersaleService.getByID(orderId);
			if(aftersale == null) {
				aftersale = new Aftersale();
			}
			aftersale.setGoodsId(order.getGoodsId());
			aftersale.setOrderId(orderId);
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currTime));
			asr.put("type", "申请换货");
			asr.put("content", asCtn);
			String oldAsr = aftersale.getApplyReason()==null ? "[]" : aftersale.getApplyReason();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(asr);
			aftersale.setApplyReason(asrArr.toJSONString());
			int cnt = this.orderService.update(updOdr);
			if(cnt >0) {
				jsonRet = this.aftersaleService.saveAF(aftersale);
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库保存数据出错！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	
	
	
	/**
	 * 商家更新售后信息
	 * 1、首次评价得分不可为空；
	 * 2、追加评价仅可追加内容，不可修改得分；
	 * @param orderId	订单ID
	 * @param partnerId	合作伙伴ID
	 * @param nextStat	下一个状态（处理结果）
	 * @param content	评价内容，json格式{reason,dispatchMode,logisticsComp,logisticsNo}
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{partnerId}/deal/{orderId}")
	public String updAftersales(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="nextStat",required=true)String nextStat,
			@RequestParam(value="content",required=true)String content,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd")String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数合规据检查
			StringBuilder sb = new StringBuilder();
			if(!"51".equals(nextStat) && "53".equals(nextStat) && "54".equals(nextStat) && "55".equals(nextStat) && "58".equals(nextStat) &&
					!"61".equals(nextStat) && "63".equals(nextStat) && "64".equals(nextStat) && "65".equals(nextStat) && "68".equals(nextStat)) {
				sb.append("处理结果：取值不正确！");
			}
			content = content.trim();
			JSONObject asCtn = JSONObject.parseObject(content);
			if("55".equals(nextStat)) {
				if(null == asCtn.getInteger("dispatchMode") || asCtn.getInteger("dispatchMode") < 1 || asCtn.getInteger("dispatchMode") > 4) {
					jsonRet.put("errmsg", "配送类型不正确(1-官方统一配送、2-商家自行配送、3-快递配送、4-自取)！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
				if(asCtn.getString("logisticsComp") == null || asCtn.getString("logisticsComp").length()<1) {
					jsonRet.put("errmsg", "配送方名称不可为空！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
				if(asCtn.getString("logisticsNo") == null || asCtn.getString("logisticsNo").length()<1) {
					jsonRet.put("errmsg", "物流单号不可为空！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}
			if(asCtn.getString("reason") == null || asCtn.getString("reason").length()<3) {
				jsonRet.put("errmsg", "处理明细：不可少于3个字符！");
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				return jsonRet.toString();
			}
			//数据检查
			PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该合作伙伴的信息！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(currUserId);
			//操作员与密码验证
			Integer updateOpr = null;
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(vip != null && myPartner.getUpdateOpr().equals(vip.getVipId())) { //绑定会员
				if(signPwd.equals(vip.getPasswd())) { //会员密码验证
					isPass = true;
					updateOpr = vip.getVipId();
				}
			}
			if(isPass != true ) {
				PartnerStaff operator = this.partnerStaffService.get(partnerId, currUserId); //员工&& operator != null) {
				if(operator != null && operator.getTagList() != null && operator.getTagList().contains("aftersale") && signPwd.equals(operator.getPasswd())) { //员工密码验证
					isPass = true;
					updateOpr = operator.getUserId();
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			Order order = this.orderService.get(orderId);
			if(order == null || !myPartner.getPartnerId().equals(order.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			//当前状态与处理结果检查
			if(!order.getStatus().equals("50") && !order.getStatus().equals("52") && !order.getStatus().equals("53") &&  
					!order.getStatus().equals("60") && !order.getStatus().equals("62") && !order.getStatus().equals("63") ) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行售后处理！");
				return jsonRet.toJSONString();
			}
			
			if("50".equals(order.getStatus())) {
				if(!"51".equals(nextStat) && !"58".equals(nextStat)) {
					jsonRet.put("errmsg", "处理结果：取值不正确！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else if("52".equals(order.getStatus())) {
				if(!"53".equals(nextStat) && !"54".equals(nextStat) && !"55".equals(nextStat)) {
					jsonRet.put("errmsg", "处理结果：取值不正确！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else if("53".equals(order.getStatus())) {
				if(!"54".equals(nextStat) && !"55".equals(nextStat)) {
					jsonRet.put("errmsg", "处理结果：取值不正确！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else if("60".equals(order.getStatus())) {
				if(!"61".equals(nextStat) && !"65".equals(nextStat) && !"68".equals(nextStat)) {
					jsonRet.put("errmsg", "处理结果：取值不正确！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else if("62".equals(order.getStatus())) {
				if(!"63".equals(nextStat) && !"64".equals(nextStat) && !"65".equals(nextStat)) {
					jsonRet.put("errmsg", "处理结果：取值不正确！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}else if("63".equals(order.getStatus())) {
				if(!"64".equals(nextStat) && !"65".equals(nextStat)) {
					jsonRet.put("errmsg", "处理结果：取值不正确！");
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					return jsonRet.toString();
				}
			}
			//数据处理保存
			if("65".equals(nextStat)) {	//执行退款
				//支付流水检查
				PayFlow payFlow = this.orderService.getLastedFlow(orderId, "1");
				if(payFlow == null || !"11".equals(payFlow.getStatus())) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您的订单没有支付成功信息（未支付到账、支付失败）！");
					return jsonRet.toJSONString();
				}
				payFlow = this.orderService.getLastedFlow(orderId, null);
				if(payFlow == null || (!"11".equals(payFlow.getStatus()) && !"F2".equals(payFlow.getStatus()))) {
					jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
					jsonRet.put("errmsg","系统没有您的支付流水信息！");
					return jsonRet.toJSONString();
				}
				//卖家账户检查
				VipBasic mchtVip = this.vipBasicService.getVipBal(myPartner.getVipId());
				if(mchtVip == null || 
						mchtVip.getBalance() < payFlow.getPayAmount() ) {
					jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
					jsonRet.put("errmsg", "您当前不可执行退货退款申请操作，账户可用余额不足！");
					return jsonRet.toJSONString();
				}
				jsonRet = this.orderService.applyRefund(true,order, payFlow, order.getUserId(), myPartner.getVipId(), asCtn);
			}else {
				//更新订单信息
				Date currTime = new Date();
				Order updOdr = new Order();
				updOdr.setStatus(nextStat); 
				updOdr.setOrderId(order.getOrderId());
				Aftersale aftersale = this.aftersaleService.getByID(orderId);
				JSONObject asr = new JSONObject();
				asr.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currTime));
				asr.put("operator", updateOpr);
				asr.put("type", nextStat.startsWith("5") ? "换货处理":"退款(货)处理");
				asr.put("content", asCtn);
				String oldAsr = aftersale.getDealResult()==null ? "[]" : aftersale.getDealResult();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(0,asr);
				aftersale.setDealResult(asrArr.toJSONString());
				aftersale.setDealTime(currTime);
				int cnt = this.orderService.update(updOdr);
				if(cnt >0) {
					jsonRet = this.aftersaleService.updateAF(aftersale);
				}else {
					jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
					jsonRet.put("errmsg", "数据库保存数据出错！");
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 根据订单ID获取售后信息
	 * @param orderId
	 * @param object
	 * @return {errcode,errmsg,aftersale}
	 */
	@RequestMapping("/get/{orderId}")
	public Object getById(@PathVariable(value="orderId")String orderId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Aftersale aftersale = this.aftersaleService.getByID(orderId);
			if(aftersale == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有指定数据！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("aftersale", aftersale);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 查询指定查询条件、分页条件的信息；
	 * @param jsonSearchParams	查询条件:{orderId,goodsId, partnerId,beginApplyTime,endApplyTime,beginDealTime,endDealTime}
	 * @param jsonPageCond		分页信息:{begin:, pageSize:}
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/getall")
	public Object getAll(@RequestParam(value="jsonSearchParams",required=true)String jsonSearchParams,
			String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,Object> params = this.getSearchMap(jsonSearchParams);
			
			PageCond pageCond = null;
			if(jsonPageCond == null || jsonPageCond.length()<1) {
				pageCond = new PageCond(0,100);
			}else {
				pageCond = JSONObject.toJavaObject(JSONObject.parseObject(jsonPageCond), PageCond.class);
				if(pageCond == null) {
					pageCond = new PageCond(0,100);
				}
				if( pageCond.getBegin()<=0) {
					pageCond.setBegin(0);
				}
				if(pageCond.getPageSize()<2) {
					pageCond.setPageSize(100);
				}
			}
			int cnt = this.aftersaleService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "没有获取到满足条件的记录信息！");
			if(cnt>0) {
				List<Aftersale> list = this.aftersaleService.getAll(params, pageCond);
				if(list != null && list.size()>0) {
					jsonRet.put("datas", list);
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
				}
			}
			return jsonRet.toString();
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	private Map<String,Object> getSearchMap(String jsonSearchParams) {
		Map<String,Object> params = new HashMap<String,Object>();
		if(jsonSearchParams == null || jsonSearchParams.length()<=0) {
			return params;
		}
		JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
		if(jsonSearch.containsKey("orderId")) {
			params.put("orderId", jsonSearch.getString("orderId"));
		}
		if(jsonSearch.containsKey("userId")) {
			params.put("userId", jsonSearch.getInteger("userId"));
		}
		if(jsonSearch.containsKey("partnerId")) {
			params.put("partnerId", jsonSearch.getInteger("partnerId"));
		}
		if(jsonSearch.containsKey("goodsId")) {
			params.put("goodsId", jsonSearch.getLong("goodsId"));
		}
		if(jsonSearch.containsKey("upPartnerId")) {//查询指定上级合作伙伴
			params.put("upPartnerId", jsonSearch.getInteger("upPartnerId"));
		}
		if(jsonSearch.containsKey("beginApplyTime")) {
			params.put("beginApplyTime", jsonSearch.getString("beginApplyTime"));
		}
		if(jsonSearch.containsKey("endApplyTime")) {
			params.put("endApplyTime", jsonSearch.getString("endApplyTime"));
		}
		if(jsonSearch.containsKey("beginDealTime")) {
			params.put("beginDealTime", jsonSearch.getString("beginDealTime"));
		}
		if(jsonSearch.containsKey("endDealTime")) {
			params.put("endDealTime", jsonSearch.getString("endDealTime"));
		}
		return params;
	}

}
