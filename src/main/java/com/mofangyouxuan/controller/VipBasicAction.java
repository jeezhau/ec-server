package com.mofangyouxuan.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;

/**
 * 会员信息管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/vip")
public class VipBasicAction {
	
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
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

}
