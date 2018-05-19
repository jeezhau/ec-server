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
import com.mofangyouxuan.model.VipBasic;
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
	
	private String regexpPhone = "1[3-9]\\d{9}";
	private String regexpEmail = "^[A-Za-z0-9_\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
	
	@Autowired
	private VipBasicService vipBasicService;
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
	 * @param vipId 会员ID
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {用户所有VIP字段}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/get/{vipId}")
	public Object getVipBasic(@PathVariable("vipId")Integer vipId) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.get(vipId);
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

			int cnt = this.vipBasicService.countAll(jsonSearch);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", ErrCodes.GOODS_NO_GOODS);
			jsonRet.put("errmsg", "没有获取到订单信息！");
			if(cnt>0) {
				List<ChangeFlow> list = this.vipBasicService.getAll(jsonSearch, pageCond);
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
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			if("1".equals(type) && (vip.getPhone() == null || vip.getPhone().length()<11)){
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "您还未绑定手机号，请先完成手机号的绑定！");
				return jsonRet.toString();
			}
			if("2".equals(type) && (vip.getEmail() == null || vip.getEmail().length()<3)){
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "您还未绑定邮箱，请先完成邮箱的绑定！");
				return jsonRet.toString();
			}
			//发送密码至媒介
			String newPwd = NonceStrUtil.getNoncePwd(9);
			if("1".equals(type)){//手机发送
				JSONObject verRet = this.sendPhoneResetPwd(vip.getPhone(), newPwd);
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
				JSONObject verRet = this.sendEmailResetPwd(vip.getEmail(), newPwd);
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
			if(vip == null) {
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
	 * 更新会员银行提现账户信息
	 * @param vipId
	 * @param accountName
	 * @param accountNo
	 * @param accountBank
	 * @return
	 */
	@RequestMapping(value="/{vipId}/updact",method=RequestMethod.POST)
	public Object updAccount(@PathVariable(value="vipId",required=true)Integer vipId,
			@RequestParam(value="cashTp",required=true)String cashType,
			@RequestParam(value="actTp",required=true)String accountType,
			@RequestParam(value="idNo",required=true)String idNo,
			@RequestParam(value="actNm",required=true)String accountName,
			@RequestParam(value="actNo",required=true)String accountNo,
			@RequestParam(value="actBlk",required=true)String accountBank,
			@RequestParam(value="pwd",required=true)String pwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			cashType = cashType.trim();
			accountType =accountType.trim();
			idNo = idNo.trim();
			accountName = accountName.trim();
			accountNo = accountNo.trim();
			accountBank = accountBank.trim();
			if(!cashType.matches("[123]")) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "提现方式格式不正确！");
				return jsonRet.toString();
			}
			if(!accountType.matches("[12]")) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "账户类型格式不正确！");
				return jsonRet.toString();
			}
			if(!idNo.matches("[1-9]\\d{16}[0-9Xx]")) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "身份证号码格式不正确！");
				return jsonRet.toString();
			}
			if(accountName.length()<2 || accountName.length()>100) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "账户名长度为2-100位字符！");
				return jsonRet.toString();
			}
			if(accountNo.length()<3 || accountNo.length()>30) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "账户号长度为3-30位字符！");
				return jsonRet.toString();
			}
			if(accountBank.length()<2 || accountBank.length()>100) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "开户行名称长度为2-100位字符！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(vipId);
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			//密码验证
			pwd = pwd.trim();
			String pwdSign = SignUtils.encodeSHA256Hex(pwd);
			if(!pwdSign.equals(vip.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员操作密码不正确！");
				return jsonRet.toString();
			}
			//数据保存
			int cnt = this.vipBasicService.updAccount(vipId, cashType, accountType, idNo,accountName, accountNo, accountBank);
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
	 * 重新绑定手机号
	 * 
	 * @param vipId
	 * @param oldVeriCode	旧手机的验证码，初始绑定为空
	 * @param newPhone
	 * @param newVeriCode
	 * @return
	 */
	@RequestMapping(value="/{vipId}/updphone",method=RequestMethod.POST)
	public Object updPhone(@PathVariable(value="vipId",required=true)Integer vipId,
			String oldVeriCode,
			@RequestParam(value="newPhone",required=true)String newPhone,
			@RequestParam(value="newVeriCode",required=true)String newVeriCode) {
		JSONObject jsonRet = new JSONObject();
		try {
			newPhone = newPhone.trim();
			if(!newPhone.matches(this.regexpPhone)) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "新手机号格式不正确！");
				return jsonRet.toString();
			}
			newVeriCode = newVeriCode.trim();
			if(newVeriCode.length() != 6) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "新手机号验证码格式不正确！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(vipId);
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			
			//旧手机号验证
			if(vip.getPhone()!=null && vip.getPhone().length()>=11) {
				if(oldVeriCode == null || oldVeriCode.trim().length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "原手机号的验证码不可为空！");
					return jsonRet.toString();
				}
				
				JSONObject verRet = this.verifyPhoneVeriCode(vip.getPhone(), oldVeriCode);
				if(verRet == null || !verRet.containsKey("errcode") ) {
					jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
					jsonRet.put("errmsg", "出现系统错误，请稍后再试！");
					return jsonRet.toString();
				}
				if(verRet.getIntValue("errcode") == 0) {
					;
				}else {
					jsonRet.put("errcode", ErrCodes.USER_VIP_VERICODE_ERROR);
					jsonRet.put("errmsg", verRet.getString("errmsg"));
					return jsonRet.toString();
				}
			}
			//新手机号验证
			JSONObject verRet = this.verifyPhoneVeriCode(newPhone, newVeriCode);
			if(verRet == null || !verRet.containsKey("errcode") ) {
				jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "出现系统错误，请稍后再试！");
				return jsonRet.toString();
			}
			if(verRet.getIntValue("errcode") == 0) {
				;
			}else {
				jsonRet.put("errcode", ErrCodes.USER_VIP_VERICODE_ERROR);
				jsonRet.put("errmsg", verRet.getString("errmsg"));
				return jsonRet.toString();
			}
			//数据保存
			int cnt = this.vipBasicService.updPhone(vipId, newPhone);
			if(cnt>0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("newpwd", "newpwd");
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
	 * 重新绑定邮箱
	 * 
	 * @param vipId
	 * @param oldVeriCode	旧邮箱的验证码，初始绑定为空
	 * @param newEmail
	 * @param newVeriCode
	 * @return
	 */
	@RequestMapping(value="/{vipId}/updemail",method=RequestMethod.POST)
	public Object updEmail(@PathVariable(value="vipId",required=true)Integer vipId,
			String oldVeriCode,
			@RequestParam(value="newEmail",required=true)String newEmail,
			@RequestParam(value="newVeriCode",required=true)String newVeriCode) {
		JSONObject jsonRet = new JSONObject();
		try {
			newEmail = newEmail.trim();
			if(!newEmail.matches(this.regexpEmail)) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "新邮箱格式不正确！");
				return jsonRet.toString();
			}
			newVeriCode = newVeriCode.trim();
			if(newVeriCode.length() != 6) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "新邮箱验证码格式不正确！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(vipId);
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			//原邮箱验证
			if(vip.getEmail()!=null && vip.getEmail().length()>=3) {
				if(oldVeriCode == null || oldVeriCode.trim().length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "原邮箱的验证码不可为空！");
					return jsonRet.toString();
				}
				
				JSONObject verRet = this.verifyEmailVeriCode(vip.getEmail(), oldVeriCode);
				if(verRet == null || !verRet.containsKey("errcode") ) {
					jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
					jsonRet.put("errmsg", "出现系统错误，请稍后再试！");
					return jsonRet.toString();
				}
				if(verRet.getIntValue("errcode") == 0) {
					;
				}else {
					jsonRet.put("errcode", ErrCodes.USER_VIP_VERICODE_ERROR);
					jsonRet.put("errmsg", verRet.getString("errmsg"));
					return jsonRet.toString();
				}
			}
			//新邮箱验证
			JSONObject verRet = this.verifyEmailVeriCode(newEmail, newVeriCode);
			if(verRet == null || !verRet.containsKey("errcode") ) {
				jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "出现系统错误，请稍后再试！");
				return jsonRet.toString();
			}
			if(verRet.getIntValue("errcode") == 0) {
				;
			}else {
				jsonRet.put("errcode", ErrCodes.USER_VIP_VERICODE_ERROR);
				jsonRet.put("errmsg", verRet.getString("errmsg"));
				return jsonRet.toString();
			}
			//数据保存
			int cnt = this.vipBasicService.updEmail(vipId, newEmail);
			if(cnt>0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("newpwd", "newpwd");
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
	 * 验证手机验证码
	 * @param phone		手机号码
	 * @param veriCode
	 * @return {errcode:0,errmsg:"ok"} 
	 */
	private JSONObject verifyPhoneVeriCode(String phone,String veriCode) {
		String url = messmpServerUrl + verifyPhoneVeriCode;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("phone", phone);
		params.put("veriCode", veriCode);
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
	 * 验证邮箱验证码
	 * @param email	
	 * @param veriCode
	 * @return {errcode:0,errmsg:"ok"} 
	 */
	private JSONObject verifyEmailVeriCode(String email,String veriCode) {
		String url = messmpServerUrl + verifyEmailVeriCode;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("email", email);
		params.put("veriCode", veriCode);
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
}

