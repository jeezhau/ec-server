package com.mofangyouxuan.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.HttpUtils;
import com.mofangyouxuan.utils.NonceStrUtil;
import com.mofangyouxuan.utils.SignUtils;

/**
 * 会员信息管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/vip")
public class VipBasicController {
	

	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private ChangeFlowService changFlowService;
	
	@Value("${messmp.messmp-server-url}")
	private String messmpServerUrl;
	@Value("${messmp.verify-phone-vericode}")
	private String verifyPhoneVeriCode;
	@Value("${messmp.verify-email-vericode}")
	private String verifyEmailVeriCode;
	@Value("${messmp.send-phone-resetpwd}")
	private String sendPhoneResetPwd;
	@Value("${messmp.send-email-resetpwd}")
	private String sendEmailResetPwd;
	
	/**
	 * 获取用户的VIP信息
	 * 更新会员开通与余额信息
	 * @param vipId 会员ID
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {用户所有VIP字段}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/get/{vipId}")
	public Object getVipBasic(@PathVariable("vipId")Integer vipId) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.getVipBal(vipId);
			if(vipBasic == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			return vipBasic;
		}catch(Exception e) {
			//数据处理
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 查询指定查询条件、分页条件的资金流水信息；
	 * 会员ID不可为空
	 * @param jsonSearchParams	查询条件:{vipId,changeType, amountDown,amountUp,beginCrtTime,endCrtTime,beginSumTime,endSumTime,sumFlag,reason,createOpr}
	 * @param jsonPageCond		分页信息:{begin:, pageSize:}
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/flow/getall")
	public Object searchFlows(@RequestParam(value="jsonSearchParams",required=true)String jsonSearchParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
			if(!jsonSearch.containsKey("vipId") && !jsonSearch.containsKey("createOpr") ) {
				jsonRet.put("errcode", ErrCodes.ORDER_SEARCH_PARAM);
				jsonRet.put("errmsg", "会员ID、创建人会员ID不可都为空！");
				return jsonRet.toString();
			}
			
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
			Map<String,Object> search = this.getSearchParamsMap(jsonSearch);
			int cnt = this.changFlowService.countAll(search);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", ErrCodes.GOODS_NO_GOODS);
			jsonRet.put("errmsg", "没有获取到订单信息！");
			if(cnt>0) {
				List<ChangeFlow> list = this.changFlowService.getAll(search, pageCond);
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

	/**
	 * 重设密码（用于忘记密码、新设密码操作）
	 * @param vipId
	 * @param type	密码的发送媒介：1-phone,2-email
	 * @return {errcode,errmsg}
	 */
	@RequestMapping(value="/{vipId}/resetpwd",method=RequestMethod.POST)
	public Object resetPwd(@PathVariable(value="vipId",required=true)Integer vipId,
			@RequestParam(value="type",required=true)String type) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据验证
			type = type.trim();
			if(!type.matches("[12]")) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "密码的重置媒介类型不正确（1-手机，2-邮箱）！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(vipId);
			UserBasic user = this.userBasicService.get(vipId);
			if(vip == null || user == null || !"1".equals(user.getStatus()) || !"1".equals(vip.getStatus())) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			if("1".equals(type) && (user.getPhone() == null || user.getPhone().length()<11)){
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "您还未绑定手机号，请先完成手机号的绑定！");
				return jsonRet.toString();
			}
			if("2".equals(type) && (user.getEmail() == null || user.getEmail().length()<3)){
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "您还未绑定邮箱，请先完成邮箱的绑定！");
				return jsonRet.toString();
			}
			//发送密码至媒介
			String newPwd = NonceStrUtil.getNoncePwd(9);
			if("1".equals(type)){//手机发送
				JSONObject verRet = this.sendPhoneResetPwd(user.getPhone(), newPwd);
				if(verRet == null || !verRet.containsKey("errcode") ) {
					jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
					jsonRet.put("errmsg", "出现系统错误，请稍后再试！");
					return jsonRet.toString();
				}
				if(verRet.getIntValue("errcode") == 0) {
					;
				}else if(verRet.getIntValue("errcode") == 1){//旧的可用
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					return jsonRet.toString();
				}else {
					jsonRet.put("errcode", ErrCodes.USER_VIP_VERICODE_ERROR);
					jsonRet.put("errmsg", verRet.getString("errmsg"));
					return jsonRet.toString();
				}
			}else {
				JSONObject verRet = this.sendEmailResetPwd(user.getEmail(), newPwd);
				if(verRet == null || !verRet.containsKey("errcode") ) {
					jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
					jsonRet.put("errmsg", "出现系统错误，请稍后再试！");
					return jsonRet.toString();
				}
				if(verRet.getIntValue("errcode") == 0) {
					;
				}else if(verRet.getIntValue("errcode") == 1){//旧的可用
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					return jsonRet.toString();
				}else {
					jsonRet.put("errcode", ErrCodes.USER_VIP_VERICODE_ERROR);
					jsonRet.put("errmsg", verRet.getString("errmsg"));
					return jsonRet.toString();
				}
			}
			//数据保存
			String newPwdSign = SignUtils.encodeSHA256Hex(newPwd);
			int cnt = this.vipBasicService.updPwd(vipId, newPwdSign);
			if(cnt>0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库数据保存出错！");
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
	 * 更新会员资金操作密码
	 * @param vipId
	 * @param passwd
	 * @return
	 */
	@RequestMapping(value="/{vipId}/updpwd",method=RequestMethod.POST)
	public Object updPasswd(@PathVariable(value="vipId",required=true)Integer vipId,
			@RequestParam(value="oldPwd",required=true)String oldPwd,
			@RequestParam(value="newPwd",required=true)String newPwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			oldPwd = oldPwd.trim();
			if(oldPwd.length()<6 || oldPwd.length()>20) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "原密码长度为6-20位字符！");
				return jsonRet.toString();
			}
			newPwd = newPwd.trim();
			if(newPwd.length()<6 || newPwd.length()>20) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "新密码长度为6-20位字符！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(vipId);
			if(vip == null || !"1".equals(vip.getStatus())) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			//旧密码验证
			String oldPwdSign = SignUtils.encodeSHA256Hex(oldPwd);
			if(!oldPwdSign.equals(vip.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "原密码不正确！");
				return jsonRet.toString();
			}
			//数据保存
			String newPwdSign = SignUtils.encodeSHA256Hex(newPwd);
			int cnt = this.vipBasicService.updPwd(vipId, newPwdSign);
			if(cnt>0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库数据保存出错！");
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
	 * 发送邮箱重置密码
	 * @param email	
	 * @param pwd
	 * @return {errcode:0,errmsg:"ok"} 
	 */
	private JSONObject sendEmailResetPwd(String email,String pwd) {
		String url = messmpServerUrl + sendEmailResetPwd;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("email", email);
		params.put("pwd", pwd);
		String strRet = HttpUtils.doPostSSL(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 发送手机重置密码
	 * @param phone	
	 * @param pwd
	 * @return {errcode:0,errmsg:"ok"} 
	 */
	private JSONObject sendPhoneResetPwd(String phone,String pwd) {
		String url = messmpServerUrl + sendPhoneResetPwd;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("phone", phone);
		params.put("pwd", pwd);
		String strRet = HttpUtils.doPostSSL(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解析查询条件
	 * @param jsonParams
	 * @return
	 */
	private Map<String,Object> getSearchParamsMap(JSONObject jsonParams){
		Map<String,Object> params = new HashMap<String,Object>();
 
		if(jsonParams.containsKey("vipId") && jsonParams.getInteger("vipId") != null) { //会员ID
			params.put("vipId", jsonParams.getInteger("vipId"));
		}
		if(jsonParams.containsKey("changeType") && jsonParams.getString("changeType").trim().length()>0) {//变更类型
			params.put("changeType", jsonParams.getString("changeType").trim());
		}
		if(jsonParams.containsKey("amountDown") && jsonParams.getDouble("amountDown") != null) { //金额下限
			params.put("amountDown", jsonParams.getDouble("amountDown"));
		}
		if(jsonParams.containsKey("amountUp") && jsonParams.getDouble("amountUp") != null) {//金额上限
			params.put("amountUp", jsonParams.getDouble("amountUp"));
		}
		if(jsonParams.containsKey("beginCrtTime") && jsonParams.getString("beginCrtTime").trim().length()>0) { //创建开始时间
			params.put("beginCrtTime", jsonParams.getString("beginCrtTime").trim());
		}
		if(jsonParams.containsKey("endCrtTime") && jsonParams.getString("endCrtTime").trim().length()>0) { //创建结束时间
			params.put("endCrtTime", jsonParams.getString("endCrtTime").trim());
		}
		if(jsonParams.containsKey("beginSumTime") && jsonParams.getString("beginSumTime").trim().length()>0) { //累积开始时间
			params.put("beginSumTime", jsonParams.getString("beginSumTime").trim());
		}
		if(jsonParams.containsKey("endSumTime") && jsonParams.getString("endSumTime").trim().length()>0) { //累积结束时间
			params.put("endSumTime", jsonParams.getString("endSumTime").trim());
		}
		if(jsonParams.containsKey("createOpr") && jsonParams.getInteger("createOpr") != null) { //创建人
			params.put("createOpr", jsonParams.getInteger("createOpr"));
		}
		if(jsonParams.containsKey("reason") && jsonParams.getString("reason").trim().length()>0) { //理由
			params.put("reason", jsonParams.getString("reason").trim());
		}
		if(jsonParams.containsKey("sumFlag") && jsonParams.getString("sumFlag").trim().length()>0) { //累积标志
			params.put("sumFlag", jsonParams.getString("sumFlag").trim());
		}
		if(jsonParams.containsKey("orderId") && jsonParams.getString("orderId").trim().length()>0) { //订单ID
			params.put("orderId", jsonParams.getString("orderId").trim());
		}
		return params;
	}
	
	
}

