package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.utils.FileFilter;
import com.mofangyouxuan.utils.HttpUtils;
import com.mofangyouxuan.utils.NonceStrUtil;
import com.mofangyouxuan.utils.SignUtils;

/**
 * 摩放优选用户管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/user")
public class UserBasicAction {
	
	@Value("${sys.user-img-path}")
	private String userImgDir ;
	
	private String regexpPhone = "1[3-9]\\d{9}";
	private String regexpEmail = "^[A-Za-z0-9_\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
	
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
	
	@Autowired
	private UserBasicService userBasicService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
	/**
	 * 创建用户
	 * @param userBasic 用户基本信息
	 * @param veriCode	使用官网注册时的手机号或邮箱的验证码
	 * 
	 * @return {errcode:0,errmsg:"ok",userId:1333}
	 * @throws JSONException
	 */
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public String create(@Valid UserBasic userBasic,BindingResult result,String veriCode) {
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
			String unique = ""; //唯一值
			if("1".equals(registType)) {//官网注册
				String email = userBasic.getEmail();
				String phone = userBasic.getPhone();
				String passwd = userBasic.getPasswd();
				if(email == null || phone == null) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", "邮箱或手机号不可都为空！ ");
					return jsonRet.toString();
				}
				if(veriCode == null || veriCode.length() != 6) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", "验证码：格式不正确！");
					return jsonRet.toString();
				}
				if(email != null && email.length()>1) {
					if(!email.matches(this.regexpEmail)) {
						jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
						jsonRet.put("errmsg", "邮箱：格式不正确！");
						return jsonRet.toString();
					}
					//邮箱验证
					JSONObject verRet = this.verifyEmailVeriCode(email, veriCode);
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
					unique = email;
				}
				if(phone != null && phone.length()>0) {
					if(!phone.matches(this.regexpPhone)) {
						jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
						jsonRet.put("errmsg", "手机号：格式不正确！");
						return jsonRet.toString();
					}
					//手机号验证
					JSONObject verRet = this.verifyPhoneVeriCode(phone, veriCode);
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
					unique = phone;
				}
				if(passwd == null || passwd.length()<6 || passwd.length()>20) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", " 密码: 长度 6-20 个字符. ");
					return jsonRet.toString();
				}else {
					userBasic.setPasswd(SignUtils.encodeSHA256Hex(passwd));//加密明文密码
				}
				
				
			}else if("2".equals(registType)) {//微信注册
				String openid = userBasic.getOpenId();
				if(openid == null || openid.length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", " openid: 长度范围 6-100字符. ");
					return jsonRet.toString();
				}
			}
			//数据重复性检查
			
			if("2".equals(registType)) {
				unique = userBasic.getOpenId();
			}
			if(unique == null || unique.length()<=0) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "用户唯一值不可为空！");
				return jsonRet.toString();
			}
			UserBasic old = this.userBasicService.get(unique);
			if( old != null) {	//已有该用户，直接返回成功
				if("2".equals(registType)) {//微信再次关注
					return this.update(userBasic, result);
				}
				jsonRet.put("errcode", 0);
				jsonRet.put("userId", old.getUserId());
				jsonRet.put("errmsg", "系统中已有该用户，如果需要修改信息请使用修改功能！");
				return jsonRet.toString();
			}
			//数据处理
			userBasic.setStatus("1"); //用户正常
			Integer id = this.userBasicService.add(userBasic);
			if(id == null) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("userId", id);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//数据处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	/**
	 * 更新用户
	 * 
	 * @param userBasic
	 * @param result
	 * @param isReCreate 是否再次注册
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/update",method=RequestMethod.POST)
	public String update(@Valid UserBasic userBasic,BindingResult result) {
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
			
			//数据检查
			UserBasic old = null;
			if(userBasic.getUserId() != null) {
				old = this.userBasicService.get(userBasic.getUserId());
				if( old == null || !"1".equals(old.getStatus())) {//不存在或不正常
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "系统中没有该用户或已注销！如果是注销用户则请先激活！");
					return jsonRet.toString();
				}
			}else {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", " 用户ID不可为空！");
				return jsonRet.toString();
			}
			//数据处理
			userBasic.setStatus("1"); //用户正常
			userBasic.setPasswd(old.getPasswd());
			userBasic.setPhone(old.getPhone());
			userBasic.setEmail(old.getEmail());
			userBasic.setRegistTime(null);
			userBasic.setRegistType(old.getRegistType());
			userBasic.setSenceId(old.getSenceId());
			int cnt = this.userBasicService.update(userBasic);
			if(cnt < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//数据处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 根据UserId获取用户的基本信息
	 * @param userId
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {用户所有字段}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/getbyid/{userId}")
	public Object getUser(@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(userId);
			if(user == null || !"1".equals(user.getStatus())){
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", " 系统中没有该用户！ ");
				return jsonRet.toString();
			}
			return user;
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	/**
	 * 根据OpenId或UnionId、Email或Phone获取用户的基本信息
	 * @param openId
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {用户所有字段}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/get",method=RequestMethod.POST)
	public Object getUser(String openId) {
		JSONObject jsonRet = new JSONObject();
		if(openId == null || openId.trim().length()<6) {
			jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
			jsonRet.put("errmsg", " OpenId或UnionId或Email长度至少为6个字符！ ");
			return jsonRet.toString();
		}
		try {
			UserBasic user = this.userBasicService.get(openId);
			
			if(user == null || !"1".equals(user.getStatus())){
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", " 系统中没有该用户！ ");
				return jsonRet.toString();
			}
			return user;
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	/**
	 * 头像上传
	 * 保存名称：headimg.xxx
	 * @param image		照片
	 * @param userId	当前操作用户
	 * @return {errcode:0,errmsg:"",filename:''}
	 */
	@RequestMapping("/headimg/upload/{userId}")
	public String uploadHeadImg(@RequestParam(value="image")MultipartFile image,
			@PathVariable(value="userId",required=true)Integer currUserId) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "头像照片信息不可为空！");
				return jsonRet.toString();
			}
			//文件类型判断
			String imgType = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf('.')+1);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "头像图片文件必须是jpg,jpeg,png格式！");
				return jsonRet.toString();
			}
			UserBasic user = this.userBasicService.get(currUserId);
			if(user == null || !"1".equals(user.getStatus())){
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", " 系统中没有该用户！ ");
				return jsonRet.toString();
			}
			//照片保存，删除旧的
			File userDir = new File(this.userImgDir,"USERID_" + currUserId);
			if(!userDir.exists()) {
				userDir.mkdirs();
			}else {
				File[] oldFiles = userDir.listFiles(new FileFilter("headimg"));
				if(oldFiles != null && oldFiles.length>0) {
					for(File oldFile:oldFiles) {
						oldFile.delete();
					}
				}
			}
			UserBasic newU = new UserBasic();
			newU.setUserId(user.getUserId());
			newU.setHeadimgurl("headimg");
			this.userBasicService.update(newU);
			File newFile = new File(userDir, "headimg." + imgType.toLowerCase());
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("filename", newU.getHeadimgurl());
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 显示头像照
	 * @param currUserId
	 * @param out
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/headimg/show/{currUserId}")
	public void showHeadImg(@PathVariable(value="currUserId",required=true)Integer currUserId,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) throws IOException {
//		UserBasic user = this.userBasicService.get(currUserId);
//		if(user == null || !"1".equals(user.getStatus())){
//			return;
//		}
		File dir = new File(this.userImgDir + "USERID_" + currUserId);
		File[] files = dir.listFiles(new FileFilter("headimg"));
		if(dir.exists() && dir.isDirectory() && files != null && files.length>0) {
			File file = files[0];
			BufferedImage image = ImageIO.read(file);
			response.setContentType("image/*");
			response.addHeader("filename", file.getName());
			OutputStream os = response.getOutputStream();  
			String type = file.getName().substring(file.getName().lastIndexOf('.')+1);
			ImageIO.write(image, type, os); 
		}
	}
	
	
	/**
	 * 重新绑定手机号
	 * 
	 * @param userId
	 * @param oldVeriCode	旧手机的验证码，初始绑定为空
	 * @param newPhone
	 * @param newVeriCode
	 * @return
	 */
	@RequestMapping(value="/{userId}/updphone",method=RequestMethod.POST)
	public Object updPhone(@PathVariable(value="userId",required=true)Integer userId,
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
			UserBasic user = this.userBasicService.get(userId);
			if(user == null || !"1".equals(user.getStatus())) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			
			//旧手机号验证
			if(user.getPhone()!=null && user.getPhone().length()>=11) {
				if(oldVeriCode == null || oldVeriCode.trim().length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "原手机号的验证码不可为空！");
					return jsonRet.toString();
				}
				
				JSONObject verRet = this.verifyPhoneVeriCode(user.getPhone(), oldVeriCode);
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
			int cnt = this.userBasicService.updPhone(userId, newPhone);
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
	 * @param userId
	 * @param oldVeriCode	旧邮箱的验证码，初始绑定为空
	 * @param newEmail
	 * @param newVeriCode
	 * @return
	 */
	@RequestMapping(value="/{userId}/updemail",method=RequestMethod.POST)
	public Object updEmail(@PathVariable(value="userId",required=true)Integer userId,String oldVeriCode,
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
			UserBasic user = this.userBasicService.get(userId);
			if(user == null || !"1".equals(user.getStatus())) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			//原邮箱验证
			if(user.getEmail()!=null && user.getEmail().length()>=3) {
				if(oldVeriCode == null || oldVeriCode.trim().length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "原邮箱的验证码不可为空！");
					return jsonRet.toString();
				}
				
				JSONObject verRet = this.verifyEmailVeriCode(user.getEmail(), oldVeriCode);
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
			int cnt = this.userBasicService.updEmail(userId, newEmail);
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
	 * 重设密码（用于忘记密码、新设密码操作）
	 * @param userId
	 * @param type	密码的发送媒介：1-phone,2-email
	 * @return {errcode,errmsg}
	 */
	@RequestMapping(value="/{userId}/resetpwd",method=RequestMethod.POST)
	public Object resetPwd(@PathVariable(value="userId",required=true)Integer userId,
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
			UserBasic user = this.userBasicService.get(userId);
			if( user == null || !"1".equals(user.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户！");
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
			int cnt = this.userBasicService.updPwd(userId, newPwdSign);
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
	 * 更新密码
	 * @param userId
	 * @param passwd
	 * @return
	 */
	@RequestMapping(value="/{userId}/updpwd",method=RequestMethod.POST)
	public Object updPasswd(@PathVariable(value="userId",required=true)Integer userId,
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
			UserBasic user = this.userBasicService.get(userId);
			if(user == null || !"1".equals(user.getStatus())) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			//旧密码验证
			String oldPwdSign = SignUtils.encodeSHA256Hex(oldPwd);
			if(!oldPwdSign.equals(user.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "原密码不正确！");
				return jsonRet.toString();
			}
			//数据保存
			String newPwdSign = SignUtils.encodeSHA256Hex(newPwd);
			int cnt = this.userBasicService.updPwd(userId, newPwdSign);
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
