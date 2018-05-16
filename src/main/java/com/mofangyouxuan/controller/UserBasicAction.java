package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
	 * @return {errcode:0,errmsg:"ok",userId:1333}
	 * @throws JSONException
	 */
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public String create(@Valid UserBasic userBasic,BindingResult result) {
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
					userBasic.setPasswd(SignUtils.encodeSHAHex(passwd));//加密明文密码
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
			if(unique != null && unique.length()>0) {
				UserBasic old = this.userBasicService.get(unique);
				if( old != null) {	//已有该用户，直接返回成功
					jsonRet.put("errcode", 0);
					jsonRet.put("userId", old.getUserId());
					jsonRet.put("errmsg", "系统中已有该用户，如果需要修改信息请使用修改功能！");
					return jsonRet.toString();
				}
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
	 * 
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
					userBasic.setPasswd(SignUtils.encodeSHAHex(passwd));//加密明文密码
				}
			}else if("2".equals(registType)) {
				String openId = userBasic.getOpenId();
				if(openId == null || openId.length()<6) {
					jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
					jsonRet.put("errmsg", " openid: not null and length range is 6-100. ");
					return jsonRet.toString();
				}
			}
			//数据检查
			String unique = "";
			if("2".equals(registType)) {
				unique = userBasic.getOpenId();
			}else if("1".equals(registType)) {
				unique = userBasic.getEmail();
			}
			UserBasic old = null;
			if(unique != null && unique.length()>0) {
				old = this.userBasicService.get(unique);
				if( old == null || !"1".equals(old.getStatus())) {//不存在或不正常
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "系统中没有该用户或已注销！如果是注销用户则请先激活！");
					return jsonRet.toString();
				}
			}else {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", " 用户的公众号OPENID和Email不可都为空！");
				return jsonRet.toString();
			}
			//数据处理
			userBasic.setStatus("1"); //用户正常
			userBasic.setPasswd(old.getPasswd());
			userBasic.setUserId(old.getUserId());
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
	 * 根据OpenId或UnionId或Email获取用户的基本信息
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
	
}
