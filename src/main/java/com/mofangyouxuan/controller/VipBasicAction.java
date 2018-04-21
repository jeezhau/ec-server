package com.mofangyouxuan.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
	 * @return
	 * @throws JSONException 
	 */
	@RequestMapping(value="/get",method=RequestMethod.POST)
	public Object getVipBasic(String openId) throws JSONException {
		JSONObject jsonRet = new JSONObject();
		UserBasic userBasic = this.userBasicService.get(openId);
		if(userBasic == null) {
			jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
			jsonRet.put("errmsg", "系统中没有该会员用户！");
			return jsonRet.toString();
		}
		VipBasic vipBasic = this.vipBasicService.get(userBasic.getId());
		if(vipBasic == null) {
			jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
			jsonRet.put("errmsg", "系统中没有该会员用户！");
			return jsonRet.toString();
		}
		return vipBasic;
	}

}
