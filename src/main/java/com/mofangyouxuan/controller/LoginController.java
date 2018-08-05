package com.mofangyouxuan.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.LoginLog;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerSettle;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.LoginLogService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.SignUtils;

@RestController
@RequestMapping("/login")
public class LoginController {
	
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	@Autowired
	private LoginLogService loginLogService;
	
	/**
	 * 普通用户登录
	 * @param log
	 * @param result
	 * @param userId
	 * @param passwd		密码登录使用的密码，除微信之外
	 * @return {errcode,errmsg,userBasic,vipBasic}
	 */
	@RequestMapping("/uin")
	public Object userLogin(@Valid LoginLog log,BindingResult result,
			@RequestParam(value="passwd",required=false)String passwd) {
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
			UserBasic user = this.userBasicService.get(log.getUserId());
			if(user == null || !"1".equals(user.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户信息！");
				return jsonRet.toString();
			}
			VipBasic vipBasic = this.vipBasicService.getVipBal(user.getUserId());
			if(vipBasic == null) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息！");
				return jsonRet.toString();
			}
			//密码验证
			boolean isSucc = false;
			if(!"1".equals(log.getSource())) {
				if(passwd == null) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", "登录密码：不可为空！");
					return jsonRet.toString();
				}
				passwd = passwd.trim();
				String pwdSign = SignUtils.encodeSHA256Hex(passwd);
				if(pwdSign.equals(user.getPasswd())) {
					isSucc = true;
				}
			}
			//数据保存
			log.setIsSucc(isSucc?"1":"0");
			jsonRet = this.loginLogService.add(log);
			if(!isSucc) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "登录密码：不正确！" + jsonRet.getString("errmsg"));
			}
			if(jsonRet.getIntValue("errcode") == 0) {
				jsonRet.put("userBasic", user);
				jsonRet.put("vipBasic", vipBasic);
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
	 * 合作伙伴登录
	 * @param log
	 * @param result
	 * @param staffTp	登录用户类型：1-vip，2-员工
	 * @param passwd		密码登录使用的密码
	 * @return {errcode,errmsg,vipBasic,myPartner,mySettle,staff}
	 */
	@RequestMapping("/pin")
	public Object partnerLogin(@Valid LoginLog log,BindingResult result,
			@RequestParam(value="staffTp",required=true)String staffTp,
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
			Integer userId = Integer.parseInt(log.getUserId());
			VipBasic vipBasic = null;
			PartnerBasic myPartner = null;
			PartnerSettle mySettle = null;
			PartnerStaff staff = null;
			//密码验证
			boolean isSucc = false;
			passwd = passwd.trim();
			String pwdSign = SignUtils.encodeSHA256Hex(passwd);
			if("1".equals(staffTp)) {
				vipBasic = this.vipBasicService.getVipBal(userId);
				if(vipBasic == null || !"1".equals(vipBasic.getStatus())) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员信息(或未激活)！");
					return jsonRet.toString();
				}
				if(pwdSign.equals(vipBasic.getPasswd())) {
					isSucc = true;
				}
				myPartner = this.partnerBasicService.getByBindUser(userId);
				if(myPartner != null) {
					mySettle = this.partnerBasicService.getSettle(myPartner.getPartnerId());
				}
			}else {//员工
				if(log.getPartnerId() == null) {
					jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
					jsonRet.put("errmsg", "合作伙伴ID：不可为空！");
					return jsonRet.toString();
				}
				myPartner = this.partnerBasicService.getByID(log.getPartnerId());
				mySettle = this.partnerBasicService.getSettle(log.getPartnerId());
				staff = this.partnerStaffService.get(log.getPartnerId(), userId);
				if(myPartner == null || staff == null) {
					jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
					jsonRet.put("errmsg", "系统中没有该合作伙伴及员工信息！");
					return jsonRet.toString();
				}
				if(pwdSign.equals(staff.getPasswd())) {
					isSucc = true;
				}
			}
			//数据保存
			log.setIsSucc(isSucc?"1":"0");
			jsonRet = this.loginLogService.add(log);
			if(!isSucc) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "登录密码：不正确！" + jsonRet.getString("errmsg"));
			}
			if(jsonRet.getIntValue("errcode") == 0) {
				jsonRet.put("vipBasic", vipBasic);
				jsonRet.put("myPartner", myPartner);
				jsonRet.put("mySettle", mySettle);
				jsonRet.put("staff", staff);
			}
		}catch(Exception e) {
			//数据处理
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
}
