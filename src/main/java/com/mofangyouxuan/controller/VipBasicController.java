package com.mofangyouxuan.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
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
	
	
	/**
	 * 获取用户的VIP信息
	 * @param openId 微信公众号OenID或UnionID或Email
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {用户所有VIP字段}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/get",method=RequestMethod.POST)
	public Object getVipBasic(String openId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(openId);
			if(userBasic == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			VipBasic vipBasic = this.vipBasicService.get(userBasic.getUserId());
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
	public Object searchOrders(@RequestParam(value="jsonSearchParams",required=true)String jsonSearchParams,String jsonPageCond) {
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
	 * 更新会员资金操作密码
	 * @param vipId
	 * @param passwd
	 * @return
	 */
	@RequestMapping(value="/updpwd",method=RequestMethod.POST)
	public Object updPasswd(@RequestParam(value="vipId",required=true)Integer vipId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			passwd = passwd.trim();
			if(passwd.length()<6 || passwd.length()>20) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "密码长度为6-20位字符！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(vipId);
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该会员用户！");
				return jsonRet.toString();
			}
			String passwdSign = SignUtils.encodeSHA256Hex(passwd);
			int cnt = this.vipBasicService.updPwd(vipId, passwdSign);
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
	@RequestMapping(value="/updact",method=RequestMethod.POST)
	public Object updAccount(@RequestParam(value="vipId",required=true)Integer vipId,
			@RequestParam(value="actNm",required=true)String accountName,
			@RequestParam(value="actNo",required=true)String accountNo,
			@RequestParam(value="actBlk",required=true)String accountBank) {
		JSONObject jsonRet = new JSONObject();
		try {
			accountName = accountName.trim();
			accountNo = accountNo.trim();
			accountBank = accountBank.trim();
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
			int cnt = this.vipBasicService.updAccount(vipId, accountName, accountNo, accountBank);
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
	
}
