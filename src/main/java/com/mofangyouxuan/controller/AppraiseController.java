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
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.Appraise;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.AppraiseService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.service.impl.AuthSecret;

/**
 * 订单评价管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/appraise")
public class AppraiseController {
	
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private AppraiseService appraiseService;

	@Autowired
	private SysParamUtil sysParamUtil;
	@Autowired
	private AuthSecret authSecret;
	
	/**
	 * 商家对买家的评价
	 * 1、首次评价得分不可为空；
	 * 2、追加评价仅可追加内容，不可修改得分；
	 * @param orderId	订单ID
	 * @param partnerId		合作伙伴ID
	 * @param score		评分
	 * @param content	评价内容
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/appr2user/{orderId}")
	public String appraise2User(@PathVariable(value="orderId",required=true)String orderId,
			@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd,
			Integer score,String content) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			StringBuilder sb = new StringBuilder();
			if(score != null) {
				if(score<0 || score>10) {
					sb.append("得分范围 0-10 分！");
				}
			}
			if(content != null && content.length()>0) {
				content = content.trim();
				if(content.length()<3 || content.length()>600) {
					sb.append("图文评价内容长度3-600字符！");
				}
			}
			if(sb.length()>0) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", sb.toString());
				return jsonRet.toJSONString();
			}
			//数据检查
			PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该合作伙伴的信息！");
				return jsonRet.toString();
			}
			//安全检查
			VipBasic vip = this.vipBasicService.get(currUserId);
			jsonRet 	= this.authSecret.auth(myPartner, vip, passwd,PartnerStaff.TAG.saleorder);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			
			Order order = this.orderService.get(orderId);
			if(!myPartner.getPartnerId().equals(order.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			if(!order.getStatus().equals("41") && !order.getStatus().equals("57") ) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行评价！");
				return jsonRet.toJSONString();
			}
			//时间与次数检查
			Long limitApprDaysGap = 1800l;
			Appraise appraise = this.appraiseService.getByOrderIdAndObj(orderId, "2");
			if(appraise != null) {//已有评价
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Long d = (new Date().getTime() - sdf.parse(appraise.getUpdateTime()).getTime())/1000/3600/24;
				if(d > limitApprDaysGap) {
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					jsonRet.put("errmsg", "该订单当前不可再次进行评价，已经超过期限！");
					return jsonRet.toJSONString();
				}
				String oldCtn = appraise.getContent();
				if(oldCtn != null) {
					if(JSONArray.parseArray(oldCtn).size() >= 3) {//已有三次评价
						jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
						jsonRet.put("errmsg", "该订单当前不可再次进行评价，最多可有三次评价！");
						return jsonRet.toJSONString();
					}
				}
				if(content == null) {
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					jsonRet.put("errmsg", "追加评价的内容不可为空，不可少于3个字符！");
					return jsonRet.toJSONString();
				}
				score = appraise.getScoreUser();
			}else {
				if(score == null) {
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					jsonRet.put("errmsg", "评分不可为空！");
					return jsonRet.toJSONString();
				}
			}
			jsonRet = this.appraiseService.appraise2User(order, score, content,currUserId);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 买家对商家的评价
	 * 1、首次评价得分不可为空；
	 * 2、追加评价仅可追加内容，不可修改得分；
	 * @param orderId	订单ID
	 * @param userId		买家ID
	 * @param scoreLogistics		物流评分
	 * @param scoreMerchangt		商家服务评分
	 * @param scoreGoods		商品描述评分
	 * @param content	评价内容
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/appr2mcht/{orderId}")
	public String appraise2Mcht(@PathVariable(value="orderId",required=true)String orderId,
			@RequestParam(value="userId",required=true)Integer userId,
			Integer scoreLogistics,Integer scoreMerchant,Integer scoreGoods,String content) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			StringBuilder sb = new StringBuilder();
			if(scoreLogistics != null) {
				if(scoreLogistics<0 || scoreLogistics>10) {
					sb.append("物流得分范围 0-10 分！");
				}
			}
			if(scoreMerchant != null) {
				if(scoreMerchant<0 || scoreMerchant>10) {
					sb.append("商家服务得分范围 0-10 分！");
				}
			}
			if(scoreGoods != null) {
				if(scoreGoods<0 || scoreGoods>10) {
					sb.append("商品描述得分范围 0-10 分！");
				}
			}
			if(content != null && content.length()>0) {
				content = content.trim();
				if(content.length()<3 || content.length()>600) {
					sb.append("图文评价内容长度3-600字符！");
				}
			}
			if(sb.length()>0) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", sb.toString());
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
			if(!"30".equals(order.getStatus()) && !"31".equals(order.getStatus()) && !"40".equals(order.getStatus()) && !"41".equals(order.getStatus()) && 
					!"55".equals(order.getStatus()) && !"56".equals(order.getStatus()) && !"57".equals(order.getStatus()) && !"58".equals(order.getStatus())) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行评价！");
				return jsonRet.toJSONString();
			}
			//时间与次数检查(天)
			Long limitApprDaysGap = 1800l;  //超过1800天的订单不可进行评价
			Appraise appraise = this.appraiseService.getByOrderIdAndObj(orderId, "1");
			if(appraise != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Long d = (new Date().getTime() - sdf.parse(appraise.getUpdateTime()).getTime())/1000/3600/24;
				if(d > limitApprDaysGap) {
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					jsonRet.put("errmsg", "该订单当前不可再次进行评价，已经超过期限！");
					return jsonRet.toJSONString();
				}
				String oldCtn = appraise.getContent();
				if(oldCtn != null) {
					if(JSONArray.parseArray(oldCtn).size() >= 3) {//已有三次评价
						jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
						jsonRet.put("errmsg", "该订单当前不可再次进行评价，最多可有三次评价！");
						return jsonRet.toJSONString();
					}
				}
				if(content == null) {
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					jsonRet.put("errmsg", "追加评价的内容不可为空，不可少于3个字符！");
					return jsonRet.toJSONString();
				}
				scoreLogistics = appraise.getScoreLogistics();
				scoreMerchant = appraise.getScoreMerchant();
				scoreGoods = appraise.getScoreGoods();
			}else {
				if(scoreLogistics == null || scoreGoods == null || scoreMerchant == null) {
					jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
					jsonRet.put("errmsg", "评分不可为空！");
					return jsonRet.toJSONString();
				}
			}
			jsonRet = this.appraiseService.appraise2Mcht(order, scoreLogistics, scoreMerchant, scoreGoods, content);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 评价审核
	 * @param orderId	订单ID
	 * @param rewPartnerId	审批者合作伙伴
	 * @param operator	审批人
	 * @param review 	审批意见
	 * @param result 	审批结果：S-通过，R-拒绝
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException
	 */
	@RequestMapping("/review")
	public String reviewAppraise(@RequestParam(value="orderId",required=true)String orderId,
			@RequestParam(value="review",required=true)String review,
			@RequestParam(value="result",required=true)String result,
			@RequestParam(value="rewPartnerId",required=true)Integer rewPartnerId,
			@RequestParam(value="operator",required=true)Integer operator,
			@RequestParam(value="passwd",required=true)String passwd){
		JSONObject jsonRet = new JSONObject();
		try {
			if(!"S".equals(result) && !"R".equals(result)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "审批结果不正确（S-通过，R-拒绝）！");
				return jsonRet.toString();
			}
			if(review == null || review.length()<2 || review.length()>600) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "审批意见：长度2-600字符！");
				return jsonRet.toString();
			}
			//数据检查
			Order order = this.orderService.get(orderId);
			if(order == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "系统中没有该订单信息！");
				return jsonRet.toJSONString();
			}
			PartnerBasic partner = this.partnerBasicService.getByID(order.getPartnerId());
			if(partner == null || (!rewPartnerId.equals(this.sysParamUtil.getSysPartnerId()) && !rewPartnerId.equals(partner.getUpPartnerId()))) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权限执行该操作！");
				return jsonRet.toString();
			}
			
			//操作员与密码验证
			jsonRet 	= this.authSecret.auth(rewPartnerId, operator, passwd,PartnerStaff.TAG.reviewappr);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			
			Appraise appraise = this.appraiseService.getByOrderIdAndObj(orderId, "1");
			if(null == appraise.getStatus()) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行评价审核！");
				return jsonRet.toString();
			}
			jsonRet = this.appraiseService.review(orderId, rewPartnerId, operator, result, review);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 根据订单ID与评价对象获取评价信息
	 * @param orderId
	 * @param object
	 * @return {errcode,errmsg,appraise}
	 */
	@RequestMapping("/get/{orderId}/{object}")
	public Object getByOrder(@PathVariable(value="orderId")String orderId,
			@PathVariable(value="object")String object) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			if(!"1".equals(object) && !"2".equals(object)) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "评价对象：取值不正确！");
				return jsonRet;
			}
			Appraise appraise = this.appraiseService.getByOrderIdAndObj(orderId, object);
			if(appraise == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有指定数据！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("appraise", appraise);
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
	 * @param jsonSearchParams	查询条件:{orderId,object,goodsId, partnerId,status,beginUpdateTime,endUpdateTime}
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
			int cnt = this.appraiseService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "没有获取到满足条件的记录信息！");
			if(cnt>0) {
				List<Appraise> list = this.appraiseService.getAll(params, pageCond);
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
		if(jsonSearch.containsKey("object")) {
			params.put("object", jsonSearch.getString("object"));
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
		if(jsonSearch.containsKey("beginUpdateTime")) {
			params.put("beginUpdateTime", jsonSearch.getString("beginUpdateTime"));
		}
		if(jsonSearch.containsKey("endUpdateTime")) {
			params.put("endUpdateTime", jsonSearch.getString("endUpdateTime"));
		}
		if(jsonSearch.containsKey("status")) {
			params.put("status", jsonSearch.getString("status"));
		}
		return params;
	}
	
}
