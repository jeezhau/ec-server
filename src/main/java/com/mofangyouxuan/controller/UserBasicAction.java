package com.mofangyouxuan.controller;

import java.util.List;

import javax.validation.Valid;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.utils.SHAUtils;

/**
 * 摩放优选用户管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/user")
public class UserBasicAction {
	
	@Autowired
	private UserBasicService userBasicService;
	
	/**
	 * 创建用户
	 * @return
	 * @throws JSONException
	 */
	@RequestMapping("/create")
	public String create(@Valid UserBasic userBasic,BindingResult result) throws JSONException {
		JSONObject jsonRet = new JSONObject();
		try {
			//用户信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				return jsonRet.toString();
			}
			String registType = userBasic.getRegistType();
			if("1".equals(registType)) {//官网注册
				String email = userBasic.getEmail();
				String passwd = userBasic.getPasswd();
				if(email == null || email.length()<6 || email.length()>100) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", " email: not null and length range is 6-100. ");
					return jsonRet.toString();
				}
				if(passwd == null || passwd.length()<6 || passwd.length()>20) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", " passwd: not null and length range is 6-20. ");
					return jsonRet.toString();
				}else {
					userBasic.setPasswd(SHAUtils.encodeSHAHex(passwd));//加密明文密码
				}
			}else if("2".equals(registType)) {
				String openid = userBasic.getOpenId();
				if(openid == null || openid.length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", " openid: not null and length range is 6-100. ");
					return jsonRet.toString();
				}
			}
			//数据重复性检查
			String unique = "";
			if("2".equals(registType)) {
				unique = userBasic.getOpenId();
			}else if("1".equals(registType)) {
				unique = userBasic.getEmail();
			}
			if(unique != null && unique.length()>0) {//已有该用户，直接返回成功
				UserBasic old = this.userBasicService.get(unique);
				if( old != null) {
					if("1".equals(old.getStatus())) {
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "系统中已有该用户，如果需要修改信息请使用修改功能；若果不是您自己请换一个名称注册！");
						return jsonRet.toString();
					}else {
						userBasic.setId(old.getId());
						userBasic.setStatus("0"); //用户正常
						this.userBasicService.update(userBasic);
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "ok");
					}
				}
			}
			//数据处理
			userBasic.setStatus("0"); //用户正常
			Integer id = this.userBasicService.add(userBasic);
			if(id == null) {
				jsonRet.put("errcode", ErrCodes.USER_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//数据处理
			jsonRet.put("errcode", ErrCodes.USER_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	
}
