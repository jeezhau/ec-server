package com.mofangyouxuan.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.SignUtils;

@Service
public class AuthSecret {
	
	@Autowired
	private VipBasicService vipBasicService ;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	
	public JSONObject auth(Integer partnerId,Integer userId,String passwd,PartnerStaff.TAG tag) throws Exception {
		JSONObject jsonRet = new JSONObject();
		//数据检查
		PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
		if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
			jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
			jsonRet.put("errmsg", "系统中没有该合作伙伴的信息！");
			return jsonRet;
		}
		VipBasic vip = this.vipBasicService.get(userId);
		//操作员与密码验证
		Boolean isPass = false;
		String signPwd = SignUtils.encodeSHA256Hex(passwd);
		if(vip != null && myPartner.getVipId().equals(vip.getVipId())) { //绑定会员
			if(signPwd.equals(vip.getPasswd())) { //会员密码验证
				isPass = true;
			}
		}
		if(isPass != true ) {
			PartnerStaff operator = this.partnerStaffService.get(partnerId, userId); //员工&& operator != null) {
			if(operator != null && operator.getTagList() != null && operator.getTagList().contains(tag.getValue()) && signPwd.equals(operator.getPasswd())) { //员工密码验证
				isPass = true;
			}
		}
		if(!isPass) {
			jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
			jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
			return jsonRet;
		}
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		return jsonRet;
	}
	
	/**
	 * 登录验证
	 * @param myPartner	合作伙伴信息
	 * @param vip		用户VIP信息
	 * @param passwd		验证密码
	 * @return
	 * @throws Exception
	 */
	public JSONObject auth(PartnerBasic myPartner,VipBasic vip,String passwd,PartnerStaff.TAG tag) throws Exception {
		JSONObject jsonRet = new JSONObject();
		//数据检查
		if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
			jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
			jsonRet.put("errmsg", "系统中没有该合作伙伴的信息！");
			return jsonRet;
		}
		//操作员与密码验证
		Boolean isPass = false;
		String signPwd = SignUtils.encodeSHA256Hex(passwd);
		if(vip != null && myPartner.getVipId().equals(vip.getVipId())) { //绑定会员
			if(signPwd.equals(vip.getPasswd())) { //会员密码验证
				isPass = true;
			}
		}
		if(isPass != true ) {
			PartnerStaff operator = this.partnerStaffService.get(myPartner.getPartnerId(), vip.getVipId()); //员工&& operator != null) {
			if(operator != null && operator.getTagList() != null && operator.getTagList().contains(tag.getValue()) && signPwd.equals(operator.getPasswd())) { //员工密码验证
				isPass = true;
			}
		}
		if(!isPass) {
			jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
			jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
			return jsonRet;
		}
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		return jsonRet;
	}

}
