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
import com.mofangyouxuan.model.SettleAccount;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.SettleAccountService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.SignUtils;

@RestController
@RequestMapping("/settle")
public class SettleAccountController {
	@Autowired
	private VipBasicService vipBasicService;
	
	@Autowired
	private SettleAccountService settleAccountService;
	
	/**
	 * 保存会员账户
	 * @param account
	 * @param result
	 * @param vipId
	 * @param passwd
	 * @return
	 */
	@RequestMapping("/{vipId}/saveact")
	public Object saveAccount(@Valid SettleAccount account,BindingResult result,
			@PathVariable("vipId")Integer vipId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.get(vipId);
			if(vipBasic == null || !"1".equals(vipBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息或未激活！");
				return jsonRet.toString();
			}
			if(!account.getVipId().equals(vipId)) {
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
			//密码验证
			passwd = passwd.trim();
			String pwdSign = SignUtils.encodeSHA256Hex(passwd);
			if(!pwdSign.equals(vipBasic.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员操作密码不正确！");
				return jsonRet.toString();
			}
			
			//数据保存
			account.setStatus("1"); //可用
			if(account.getSettleId() == 0) {//新增
				jsonRet = this.settleAccountService.add(account);
			}else {
				SettleAccount old = this.settleAccountService.get(account.getSettleId());
				if(old == null) {
					jsonRet.put("errcode", ErrCodes.VIP_ACCOUNT_NOT_EXISTS);
					jsonRet.put("errmsg", "该会员账户不存在！");
				}else if(!old.getVipId().equals(vipId)) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您无权执行该操作！");
				}else {
					jsonRet = this.settleAccountService.update(account);
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
	 * 删除账户
	 * @param settleId
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{vipId}/delete")
	public Object delete(@PathVariable("vipId")Integer vipId,
			@RequestParam(value="settleId",required=true)Long settleId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.get(vipId);
			if(vipBasic == null || !"1".equals(vipBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息或未激活！");
				return jsonRet.toString();
			}
			//密码验证
			passwd = passwd.trim();
			String pwdSign = SignUtils.encodeSHA256Hex(passwd);
			if(!pwdSign.equals(vipBasic.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员操作密码不正确！");
				return jsonRet.toString();
			}
			//数据处理
			SettleAccount old = this.settleAccountService.get(settleId);
			if(old == null || !old.getVipId().equals(vipId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
			}else {
				jsonRet = this.settleAccountService.delete(old);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	
	/**
	 * 获取会员用户的所有账户
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{vipId}/getall")
	public Object getAll(@PathVariable("vipId")Integer vipId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			List<SettleAccount > list = this.settleAccountService.getAll(params);
			if(list != null && list.size() >0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("datas", list);
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "没有获取到数据！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}

}
