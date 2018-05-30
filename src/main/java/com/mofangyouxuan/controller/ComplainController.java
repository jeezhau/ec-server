package com.mofangyouxuan.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ComplainLog;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ComplainLogService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;

/**
 * 用户投诉管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/complain")
public class ComplainController {
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private ComplainLogService complainLogService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	
	/**
	 * 用户对购买的订单进行投诉或商家对上级合作伙伴的投诉
	 * 1、新增投诉；
	 * 2、对未处理的投诉进行修改；
	 * 3、订单投诉为用户对购买的商品进行投诉，投诉内容主要为对商家的服务进行投诉；
	 * 4、商家投诉上级合作伙伴主要为对上级合作伙伴提供的服务不满意；
	 * 
	 * @param userId
	 * @param log
	 * @param result
	 * @return
	 */
	@RequestMapping("/{userId}/order/save")
	public Object complainOrder(@PathVariable("userId")Integer userId,
			@Valid ComplainLog log,BindingResult result) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户信息！");
				return jsonRet.toString();
			}
			if(!userBasic.getUserId().equals(userId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			//数据检查
			if(log.getOrderId() == null) {
				jsonRet.put("errmsg", "订单ID：不可为空！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			Order order = orderService.get(false, false, false, false, true, log.getOrderId());
			if(order == null) {
				jsonRet.put("errmsg", "系统中没有该订单信息！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			if(!order.getUserId().equals(userId)) {
				jsonRet.put("errmsg", "您无权对该商品订单进行投诉！");
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				return jsonRet.toString();
			}
			if(!order.getStatus().startsWith("2") && !order.getStatus().startsWith("3")  && !order.getStatus().startsWith("4") && 
					!order.getStatus().startsWith("5") && !order.getStatus().startsWith("6") ) {
				jsonRet.put("errmsg", "该商品订单当前不可进行投诉！");
				jsonRet.put("errcode", ErrCodes.COMPLAIN_STATUS_ERROR);
				return jsonRet.toString();
			}
			//数据保存
			log.setPartnerId(order.getPartnerId());
			log.setGoodsId(order.getGoodsId());
			log.setStatus("0"); //待处理
			if(log.getCplanId() == 0) {//新增
				jsonRet = this.complainLogService.add(log);
			}else {//变更
				ComplainLog old = this.complainLogService.get(log.getCplanId());
				if(old == null) {
					jsonRet.put("errcode", ErrCodes.COMPLAIN_NO_EXISTS);
					jsonRet.put("errmsg", "该待变更的投诉信息不存在！");
				}else if(!old.getUserId().equals(userId)) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您无权执行该操作！");
				}else if(!"0".equals(old.getStatus())){ //待处理
					jsonRet.put("errcode", ErrCodes.COMPLAIN_STATUS_ERROR);
					jsonRet.put("errmsg", "该投诉信息已在处理中，不可变更！");
				}else {
					jsonRet = this.complainLogService.updateContent(log);
				}
			}
		}catch(Exception e) {
			//数据处理
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	@RequestMapping("/{vipId}/partner/save")
	public Object complainPartner(@PathVariable("vipId")Integer vipId,
			@Valid ComplainLog log,BindingResult result) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.get(vipId);
			if(vipBasic == null || !"1".equals(vipBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息或未激活！");
				return jsonRet.toString();
			}
			if(!vipBasic.getVipId().equals(vipId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			//数据检查
			if(log.getPartnerId() == null) {
				jsonRet.put("errmsg", "合作伙伴ID：不可为空！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			PartnerBasic partner = this.partnerBasicService.getByID(log.getPartnerId());
			if(partner == null) {
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			//上下级关系检查
//			if(!order.getUserId().equals(userId)) {
//				jsonRet.put("errmsg", "您无权对该商品订单进行投诉！");
//				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
//				return jsonRet.toString();
//			}				
			//数据保存
			log.setStatus("0"); //待处理
			if(log.getCplanId() == 0) {//新增
				jsonRet = this.complainLogService.add(log);
			}else {//变更
				ComplainLog old = this.complainLogService.get(log.getCplanId());
				if(old == null) {
					jsonRet.put("errcode", ErrCodes.COMPLAIN_NO_EXISTS);
					jsonRet.put("errmsg", "该待变更的投诉信息不存在！");
				}else if(!old.getUserId().equals(vipId)) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您无权执行该操作！");
				}else if(!"0".equals(old.getStatus())){ //待处理
					jsonRet.put("errcode", ErrCodes.COMPLAIN_STATUS_ERROR);
					jsonRet.put("errmsg", "该投诉信息已在处理中，不可变更！");
				}else {
					jsonRet = this.complainLogService.updateContent(log);
				}
			}
		}catch(Exception e) {
			//数据处理
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
		
	}
	
	/**
	 * 删除投诉
	 * @param logId
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{userId}/order/delete/{logId}")
	public Object deleteOrderComplain(@PathVariable("userId")Integer userId,
			@PathVariable(value="logId",required=true)Integer logId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户信息！");
				return jsonRet.toString();
			}
			if(!userBasic.getUserId().equals(userId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//数据处理
			ComplainLog old = this.complainLogService.get(logId);
			if(old == null || !old.getUserId().equals(userId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
			}else {
				jsonRet = this.complainLogService.delete(old);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 删除投诉
	 * @param logId
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{vipId}/partner/delete/{logId}")
	public Object deletePartnerComplain(@PathVariable("vipId")Integer vipId,
			@PathVariable(value="logId",required=true)Integer logId) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.get(vipId);
			if(vipBasic == null || !"1".equals(vipBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息或未激活！");
				return jsonRet.toString();
			}
			if(!vipBasic.getVipId().equals(vipId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//数据处理
			ComplainLog old = this.complainLogService.get(logId);
			if(old == null || !old.getUserId().equals(vipId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
			}else {
				jsonRet = this.complainLogService.delete(old);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	
	@RequestMapping("/{oprId}/deal")
	public Object saveDeal(@PathVariable("oprId")Integer oprId,
			@RequestParam(value="logId",required=true)Integer logId,
			@RequestParam(value="content",required=true)String content) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(oprId);
			if(userBasic == null || !"1".equals(userBasic.getStatus())||
					oprId < 10000) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该操作员信息！");
				return jsonRet.toString();
			}
			//数据处理
			ComplainLog old = this.complainLogService.get(logId);
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.COMPLAIN_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该投诉信息！");
			}else{
				jsonRet = this.complainLogService.updateDeal(old, content, oprId);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	@RequestMapping("/{oprId}/revisit")
	public Object saveRevisit(@PathVariable("oprId")Integer oprId,
			@RequestParam(value="logId",required=true)Integer logId,
			@RequestParam(value="content",required=true)String content,
			@RequestParam(value="content",required=true)String result) {
		JSONObject jsonRet = new JSONObject();
		try {
			if(!"1".equals(result) && !"2".equals(result)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "系统访问参数不正确！");
				return jsonRet.toString();
			}
			UserBasic userBasic = this.userBasicService.get(oprId);
			if(userBasic == null || !"1".equals(userBasic.getStatus())||
					oprId < 10000) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该操作员信息！");
				return jsonRet.toString();
			}
			//数据处理
			ComplainLog old = this.complainLogService.get(logId);
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.COMPLAIN_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该投诉信息！");
			}else if(!"1".equals(old.getStatus())){ //待回访
				jsonRet.put("errcode", ErrCodes.COMPLAIN_STATUS_ERROR);
				jsonRet.put("errmsg", "该投诉信息正在处理中，不可记录回访！");
			}else{
				jsonRet = this.complainLogService.updateRevisit(old, content, oprId, result);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 查询指定查询条件、排序条件、分页条件的信息；
	 * @param jsonSearchParams	查询条件:{userId,goodsId, partnerId,status,phone,beginCreateTime,endCreateTime,beginDealTime,endDealTime,beginRevisitTime,endRevisitTime}
	 * @param jsonSortParams		排序条件:{createTime:"N#0/1",dealTime:"N#0/1",revisitTime:"N#0/1"}
	 * @param jsonPageCond		分页信息:{begin:, pageSize:}
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/getall")
	public Object getAll(@RequestParam(value="jsonSearchParams",required=true)String jsonSearchParams,
			String jsonSortParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			String sorts = this.getSortsStr(jsonSortParams);
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
			int cnt = this.complainLogService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "没有获取到满足条件的记录信息！");
			if(cnt>0) {
				List<ComplainLog> list = this.complainLogService.getAll(params, sorts, pageCond);
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
	
	private String getSortsStr(String jsonSortParams) {
		String strSorts = null;
		if(jsonSortParams == null || jsonSortParams.length() <= 0) {
			return strSorts;
		}
		JSONObject jsonSort = JSONObject.parseObject(jsonSortParams);
		Map<Integer,String> sortMap = new HashMap<Integer,String>();
		if(jsonSort.containsKey("createTime")) {
			String value = jsonSort.getString("createTime");
			if(value != null && value.length()>0) {
				String[] arr = value.split("#");
				sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " create_time asc " : " create_time desc " );
			}
		}
		if(jsonSort.containsKey("dealTime")) {
			String value = jsonSort.getString("dealTime");
			if(value != null && value.length()>0) {
				String[] arr = value.split("#");
				sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " deal_time asc " : " deal_time desc " );
			}
		}
		if(jsonSort.containsKey("revisitTime")) {
			String value = jsonSort.getString("revisitTime");
			if(value != null && value.length()>0) {
				String[] arr = value.split("#");
				sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " revisit_time asc " : " revisit_time desc " );
			}
		}
		return strSorts;
	}
	
	private Map<String,Object> getSearchMap(String jsonSearchParams) {
		Map<String,Object> params = new HashMap<String,Object>();
		if(jsonSearchParams == null || jsonSearchParams.length()<=0) {
			return params;
		}
		JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
		
		if(jsonSearch.containsKey("cplanId")) {
			params.put("cplanId", jsonSearch.getInteger("cplanId"));
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
		if(jsonSearch.containsKey("orderId")) {
			params.put("orderId", jsonSearch.getString("orderId"));
		}
		if(jsonSearch.containsKey("phone")) {
			params.put("phone", jsonSearch.getInteger("phone"));
		}
		if(jsonSearch.containsKey("beginCreateTime")) {
			params.put("beginCreateTime", jsonSearch.getString("beginCreateTime"));
		}
		if(jsonSearch.containsKey("endCreateTime")) {
			params.put("endCreateTime", jsonSearch.getString("endCreateTime"));
		}
		if(jsonSearch.containsKey("beginDealTime")) {
			params.put("beginDealTime", jsonSearch.getString("beginDealTime"));
		}
		if(jsonSearch.containsKey("endDealTime")) {
			params.put("endDealTime", jsonSearch.getString("endDealTime"));
		}
		if(jsonSearch.containsKey("beginRevisitTime")) {
			params.put("beginRevisitTime", jsonSearch.getString("beginRevisitTime"));
		}
		if(jsonSearch.containsKey("endRevisitTime")) {
			params.put("endRevisitTime", jsonSearch.getString("endRevisitTime"));
		}
		if(jsonSearch.containsKey("status")) {
			params.put("status", jsonSearch.getString("status"));
		}
		return params;
	}

}


