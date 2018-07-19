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
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.ComplainLog;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ComplainLogService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.SignUtils;

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
	@Autowired
	private PartnerStaffService partnerStaffService;
	@Autowired
	private SysParamUtil sysParamUtil;
	
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
			Order order = orderService.get(log.getOrderId());
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
					!order.getStatus().startsWith("5") && !order.getStatus().startsWith("6") && 
					!order.getStatus().startsWith("CM") ) {
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
				}else if(!old.getOprId().equals(userId)) {
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
			if(old == null || !old.getOprId().equals(userId)) {
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

	
	@RequestMapping("/{oprPId}/partner/save")
	public Object complainPartner(@PathVariable("oprPId")Integer oprPId,
			@Valid ComplainLog log,BindingResult result,
			@RequestParam(value="passwd",required=true)String passwd) {
		
		JSONObject jsonRet = new JSONObject();
		try {
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
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			PartnerBasic partner = this.partnerBasicService.getByID(oprPId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "获取合作伙伴信息失败！");
				return jsonRet.toString();
			}
			if(log.getOprId().equals(partner.getVipId())) { //绑定会员
				VipBasic vip = this.vipBasicService.get(log.getOprId());
				if(vip == null || !"1".equals(vip.getStatus()) ) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员或未激活！");
					return jsonRet.toString();
				}
				if(signPwd.equals(vip.getPasswd())) {
					isPass = true;
				}
			}
			if(isPass != true) {
				PartnerStaff staff = this.partnerStaffService.get(oprPId, log.getOprId());
				if(staff != null && staff.getTagList() != null && 
						staff.getTagList().contains(PartnerStaff.TAG.complain4p.getValue()) && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			//数据检查
			if(log.getPartnerId() == null) {
				jsonRet.put("errmsg", "合作伙伴ID：不可为空！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			PartnerBasic upPartner = this.partnerBasicService.getByID(log.getPartnerId());
			if(upPartner == null) {
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			//上下级关系检查
			if(!log.getPartnerId().equals(partner.getUpPartnerId())) {
				jsonRet.put("errmsg", "您无权对该合作伙伴进行投诉！");
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				return jsonRet.toString();
			}	
			//数据保存
			log.setStatus("0"); //待处理
			if(log.getCplanId() == 0) {//新增
				jsonRet = this.complainLogService.add(log);
			}else {//变更
				ComplainLog old = this.complainLogService.get(log.getCplanId());
				if(old == null) {
					jsonRet.put("errcode", ErrCodes.COMPLAIN_NO_EXISTS);
					jsonRet.put("errmsg", "该待变更的投诉信息不存在！");
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
	@RequestMapping("/{partnerId}/partner/delete/{cplanId}")
	public Object deletePartnerComplain(@PathVariable("partnerId")Integer partnerId,
			@PathVariable(value="cplanId",required=true)Integer cplanId,
			@RequestParam("operator")Integer operator,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "获取合作伙伴信息失败！");
				return jsonRet.toString();
			}
			if(operator.equals(partner.getVipId())) { //绑定会员
				VipBasic vip = this.vipBasicService.get(operator);
				if(vip == null || !"1".equals(vip.getStatus()) ) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员或未激活！");
					return jsonRet.toString();
				}
				if(signPwd.equals(vip.getPasswd())) {
					isPass = true;
				}
			}
			if(isPass != true) {
				PartnerStaff staff = this.partnerStaffService.get(partnerId, operator);
				if(staff != null && staff.getTagList() != null && 
						staff.getTagList().contains(PartnerStaff.TAG.complain4p.getValue()) && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			//数据处理
			ComplainLog old = this.complainLogService.get(cplanId);
			if(old == null || !partner.getPartnerId().equals(old.getOprPid())) {
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
	 * 系统合作伙伴处理投诉
	 * @param operator	处理人ID
	 * @param passwd		处理人密码
	 * @param logId		投诉记录ID
	 * @param content	处理内容
	 * @return
	 */
	@RequestMapping("/deal")
	public Object saveDeal(@RequestParam("operator")Integer operator,
			@RequestParam(value="passwd",required=true)String passwd,
			@RequestParam(value="logId",required=true)Integer logId,
			@RequestParam(value="content",required=true)String content) {
		JSONObject jsonRet = new JSONObject();
		try {
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			PartnerBasic sysPartner = this.partnerBasicService.getByID(this.sysParamUtil.getSysPartnerId());
			if(sysPartner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "获取合作伙伴信息失败！");
				return jsonRet.toString();
			}
			if(operator.equals(sysPartner.getVipId())) { //绑定会员
				VipBasic vip = this.vipBasicService.get(operator);
				if(vip == null || !"1".equals(vip.getStatus()) ) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员或未激活！");
					return jsonRet.toString();
				}
				if(signPwd.equals(vip.getPasswd())) {
					isPass = true;
				}
			}
			if(isPass != true) {
				PartnerStaff staff = this.partnerStaffService.get(this.sysParamUtil.getSysPartnerId(), operator);
				if(staff != null && staff.getTagList() != null && 
						staff.getTagList().contains(PartnerStaff.TAG.ComplainDeal.getValue()) && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			
			//数据处理
			ComplainLog old = this.complainLogService.get(logId);
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.COMPLAIN_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该投诉信息！");
			}else{
				jsonRet = this.complainLogService.updateDeal(old, content, operator);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 投诉处理回访
	 * @param operator
	 * @param passwd
	 * @param logId
	 * @param content
	 * @param result		回访结果：0-须再次处理，2：完成
	 * @return
	 */
	@RequestMapping("/revisit")
	public Object saveRevisit(@RequestParam("operator")Integer operator,
			@RequestParam(value="passwd",required=true)String passwd,
			@RequestParam(value="logId",required=true)Integer logId,
			@RequestParam(value="content",required=true)String content,
			@RequestParam(value="result",required=true)String result) {
		JSONObject jsonRet = new JSONObject();
		try {
			if(!"0".equals(result) && !"2".equals(result)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "系统访问参数不正确！");
				return jsonRet.toString();
			}
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			PartnerBasic sysPartner = this.partnerBasicService.getByID(this.sysParamUtil.getSysPartnerId());
			if(sysPartner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "获取合作伙伴信息失败！");
				return jsonRet.toString();
			}
			if(operator.equals(sysPartner.getVipId())) { //绑定会员
				VipBasic vip = this.vipBasicService.get(operator);
				if(vip == null || !"1".equals(vip.getStatus()) ) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员或未激活！");
					return jsonRet.toString();
				}
				if(signPwd.equals(vip.getPasswd())) {
					isPass = true;
				}
			}
			if(isPass != true) {
				PartnerStaff staff = this.partnerStaffService.get(this.sysParamUtil.getSysPartnerId(), operator);
				if(staff != null && staff.getTagList() != null && 
						staff.getTagList().contains(PartnerStaff.TAG.ComplainRevisit.getValue()) && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
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
				jsonRet = this.complainLogService.updateRevisit(old, content, operator, result);
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
	 * @param jsonSearchParams	查询条件:{cplanId,oprId,oprPid,goodsId, partnerId,status,phone,beginCreateTime,endCreateTime,beginDealTime,endDealTime,beginRevisitTime,endRevisitTime}
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
		if(jsonSearch.containsKey("oprId")) {
			params.put("oprId", jsonSearch.getInteger("oprId"));
		}
		if(jsonSearch.containsKey("oprPid")) {
			params.put("oprPid", jsonSearch.getInteger("oprPid"));
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


