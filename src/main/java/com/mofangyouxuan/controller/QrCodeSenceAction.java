package com.mofangyouxuan.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.QrCodeSence;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.QrCodeSenceService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.utils.HttpUtils;

/**
 * 用户推广二维码信息管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/qrcode")
public class QrCodeSenceAction {
	
	@Value("${wxmp.wxmp-server-url}")
	private String wxmpServerUrl;//微信管理平台服务器路径
	
	@Value("${wxmp.qrcode-apply-url}")
	private String qrCodeApplyUrl;
	
	@Value("${wxmp.qrcode-get-url}")
	private String qrCodeGetUrl;
	
	@Value("${wxmp.qrcode-show-url}")
	private String qrCodeShowUrl;
	
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private QrCodeSenceService qrCodeSenceService;
	
	/**
	 * 获取指定用户的推广信息
	 * 1、已推广用户数量；
	 * 2、当前推广二维码：系统已有则直接向微信管理平台申请下载，如果没有则向微信管理平台申请重新生成；
	 * @param openId		要生成推广二维码的用户
	 * @param create		是否强制重新生成(1)
	 * @return {"errcode":0,"errmsg":"ok","showurl":"","count":0}
	 * @throws JSONException 
	 */
	@RequestMapping("/spread")
	public Object getSpreadInfo(String openId,String create) {
		JSONObject jsonRet = new JSONObject();
		UserBasic user = this.userBasicService.get(openId);
		if(user == null || !"1".equals(user.getStatus())) {
			jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
			jsonRet.put("errmsg", "系统中没有该用户！");
			return jsonRet.toString();
		}
		try {
			int cnt = this.userBasicService.countSpreadUsers(user.getUserId());
			QrCodeSence qrcode = this.qrCodeSenceService.get(user.getUserId());
			if(qrcode == null 
					|| (qrcode.getCreateTime().getTime() + qrcode.getExpireSeconds()*1000L)<=new Date().getTime()
					|| (create != null && "1".equals(create.trim()))){
				//重新申请
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("sceneId", user.getUserId());
				String strRet = HttpUtils.doPost(this.wxmpServerUrl + this.qrCodeApplyUrl, params);
				JSONObject ret = JSONObject.parseObject(strRet);
				if(ret.containsKey("errcode") && ret.getIntValue("errcode") == 0) {
					String ticket = ret.getString("ticket");
					int expire_seconds = ret.getIntValue("expire_seconds");
					String url = ret.getString("url");
					if(qrcode != null) {
						this.qrCodeSenceService.delete(user.getUserId());
					}
					//申请生成二维码
					params = new HashMap<String,Object>();
					params.put("ticket", ticket);
					strRet = HttpUtils.doPost(this.wxmpServerUrl + this.qrCodeGetUrl, params);
					ret = JSONObject.parseObject(strRet);
					if(ret.containsKey("errcode") && ret.getIntValue("errcode") == 0) {
						qrcode = new QrCodeSence();
						qrcode.setSenceId(user.getUserId());
						qrcode.setUserId(user.getUserId());
						qrcode.setCreateTime(new Date());
						qrcode.setExpireSeconds(expire_seconds);
						qrcode.setTicket(ticket);
						qrcode.setUrl(url);
						qrcode.setWxmpPicnane(ret.getString("filename"));
						this.qrCodeSenceService.add(qrcode);
						//返回显示路径
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
						jsonRet.put("showurl", this.wxmpServerUrl + this.qrCodeShowUrl + qrcode.getWxmpPicnane());
						jsonRet.put("count", cnt);
						return jsonRet.toString();
					}
				}
				jsonRet.put("errcode", ErrCodes.QRCODE_ERROR);
				jsonRet.put("errmsg", "生成推广二维码失败！");
				return jsonRet.toString();
			}else {
				//返回显示路径
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("showurl", this.wxmpServerUrl + this.qrCodeShowUrl + qrcode.getWxmpPicnane());
				jsonRet.put("count", cnt);
				return jsonRet.toString();
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
}
