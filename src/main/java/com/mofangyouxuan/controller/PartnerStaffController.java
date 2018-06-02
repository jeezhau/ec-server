package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.FileFilter;
import com.mofangyouxuan.utils.SignUtils;

/**
 * 合作伙伴员工管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/pstaff")
public class PartnerStaffController {
	
	@Value("${sys.partner-img-dir}")
	private String partnerImgDir;	//合作伙伴照片保存目录

	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	
	/**
	 * 合作伙伴员工基本信息保存
	 * 1、新增时redId为0，新员工的初始密码不可为空；
	 * 2、修改时不修改原来的密码、头像、客服二维码；
	 * @param partnerId
	 * @param staff
	 * @param result
	 * @param passwd		员工密码或合作伙伴绑定的会员的密码
	 * @return
	 */
	@RequestMapping("/{partnerId}/saveStaff")
	public Object saveStaff(@PathVariable("partnerId")Integer partnerId,
			@Valid PartnerStaff staff,BindingResult result,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据验证
			String checkRet = this.dataCheck(partnerId, staff, result, passwd);
			if(checkRet != null) {
				return checkRet;
			}
			//数据处理保存
			staff.setStatus("1");
			if(staff.getRecId() == 0) { //新增
				String defaultPwd = staff.getPasswd();
				if(defaultPwd == null || defaultPwd.length()<6 || defaultPwd.length()>20) {
					jsonRet.put("errmsg", "新员工的初始操作密码：长度范围【6-20字符】！");
					jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
					return jsonRet.toString();
				}
				String signPwd = SignUtils.encodeSHA256Hex(defaultPwd);
				staff.setPasswd(signPwd);
				jsonRet = this.partnerStaffService.addStaff(staff);
			}else { //修改
				PartnerStaff old = this.partnerStaffService.get(staff.getRecId());
				if(old == null) {
					jsonRet.put("errmsg", "系统中没有该员工信息！");
					jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
					return jsonRet.toString();
				}
				staff.setHeadimgurl(old.getHeadimgurl());
				staff.setKfQrcodeUrl(old.getKfQrcodeUrl());
				staff.setPasswd(null); 	//使用旧的密码
				staff.setRecId(old.getRecId());
				jsonRet = this.partnerStaffService.updateStaffBasic(staff);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 超级管理人员删除员工
	 * @param partnerId
	 * @param rootPasswd		超级管理人员的密码：合作伙伴绑定会员的密码
	 * @param userId			被删除的员工
	 * @return
	 */
	@RequestMapping("/{partnerId}/deleteStaff")
	public String deleteStaff(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="rootPasswd",required=true)String rootPasswd,
			@RequestParam(value="userId",required=true)Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			PartnerStaff staff = this.partnerStaffService.get(partnerId, userId);
			if(staff == null){
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", " 系统中没有该员工信息！ ");
				return jsonRet.toString();
			}
			//超级密码检查
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null ){
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", " 系统中没有该合作伙伴信息！ ");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(partner.getVipId());
			String signPwd = SignUtils.encodeSHA256Hex(rootPasswd);
			if(vip == null || !signPwd.equals(vip.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的会员密码不正确)！");
				return jsonRet.toString();
			}
			//数据保存
			jsonRet = this.partnerStaffService.deleteStaff(staff);
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 超级管理人员重置密码
	 * @param partnerId
	 * @param rootPasswd		超级管理人员的密码：合作伙伴绑定会员的密码
	 * @param userId			被重设密码的员工
	 * @param newPasswd		新密码
	 * @return
	 */
	@RequestMapping("/{partnerId}/resetpwd")
	public String resetPwd(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="rootPasswd",required=true)String rootPasswd,
			@RequestParam(value="userId",required=true)Integer userId,
			@RequestParam(value="newPasswd",required=true)String newPasswd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			PartnerStaff staff = this.partnerStaffService.get(partnerId, userId);
			if(staff == null){
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", " 系统中没有该员工信息！ ");
				return jsonRet.toString();
			}
			if(newPasswd == null || newPasswd.length()<6 || newPasswd.length()>20) {
				jsonRet.put("errmsg", "新操作密码：长度范围【6-20字符】！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			//超级密码检查
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null ){
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", " 系统中没有该合作伙伴信息！ ");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(partner.getVipId());
			String signPwd = SignUtils.encodeSHA256Hex(rootPasswd);
			if(vip == null || !signPwd.equals(vip.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的会员密码不正确)！");
				return jsonRet.toString();
			}
			//数据保存
			String signNewPwd = SignUtils.encodeSHA256Hex(newPasswd);
			PartnerStaff updStaff = new PartnerStaff();
			updStaff.setRecId(staff.getRecId());
			updStaff.setPasswd(signNewPwd);
			updStaff.setUpdateOpr(vip.getVipId());
			updStaff.setUpdateTime(new Date());
			jsonRet = this.partnerStaffService.updateOther(updStaff);
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 使用原密码修改
	 * @param partnerId
	 * @param userId
	 * @param oldPasswd
	 * @param newPasswd
	 * @return
	 */
	@RequestMapping("/{partnerId}/updpwd")
	public String updatePwd(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="userId",required=true)Integer userId,
			@RequestParam(value="oldPasswd",required=true)String oldPasswd,
			@RequestParam(value="newPasswd",required=true)String newPasswd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			PartnerStaff operator = this.partnerStaffService.get(partnerId, userId);
			if(operator == null){
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", " 系统中没有该员工信息！ ");
				return jsonRet.toString();
			}
			if(newPasswd == null || newPasswd.length()<6 || newPasswd.length()>20) {
				jsonRet.put("errmsg", "新操作密码：长度范围【6-20字符】！");
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			//密码检查
			String signPwd = SignUtils.encodeSHA256Hex(oldPasswd);
			if(!signPwd.equals(operator.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的原操作密码不正确)！");
				return jsonRet.toString();
			}
			//数据保存
			String signNewPwd = SignUtils.encodeSHA256Hex(newPasswd);
			PartnerStaff updStaff = new PartnerStaff();
			updStaff.setRecId(operator.getRecId());
			updStaff.setPasswd(signNewPwd);
			updStaff.setUpdateOpr(userId);
			updStaff.setUpdateTime(new Date());
			jsonRet = this.partnerStaffService.updateOther(updStaff);
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	/**
	 * 头像与客服二维码上传：员工自我维护
	 * 保存相对路径：staff
	 * 1、头像保存名称：[userid]_headimg.xxx；
	 * 2、客服二维码保存名称：[userid]_kfqrcode.xxx;
	 * @param image		照片
	 * @param mode	照片类型：headimg、kfqrcode
	 * @param userId		当前操作用户
	 * @param passwd		操作密码
	 * @return {errcode:0,errmsg:"",filename:''}
	 */
	@RequestMapping("/{partnerId}/upload/{mode}")
	public String uploadImg(@PathVariable("partnerId")Integer partnerId,
			@PathVariable("mode")String mode,
			@RequestParam(value="userId",required=true)Integer userId,
			@RequestParam(value="passwd",required=true)String passwd,
			@RequestParam(value="image")MultipartFile image) {
		JSONObject jsonRet = new JSONObject();
		try {
			int limitSize = 3*1024*1024; //3M
			if(!"headimg".equals(mode) && !"kfqrcode".equals(mode)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "系统访问参数不正确！");
				return jsonRet.toString();
			}
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片信息不可为空！");
				return jsonRet.toString();
			}
			//文件判断
			String imgType = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf('.')+1);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件必须是jpg,jpeg,png格式！");
				return jsonRet.toString();
			}
			if(image.getSize() > limitSize) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件不可大于3M！");
				return jsonRet.toString();
			}
			PartnerStaff operator = this.partnerStaffService.get(partnerId, userId);
			if(operator == null){
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", " 系统中没有该员工信息！ ");
				return jsonRet.toString();
			}
			//密码检查
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(!signPwd.equals(operator.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的操作密码不正确)！");
				return jsonRet.toString();
			}
			//照片保存，删除旧的
			File staffDir = new File(this.partnerImgDir +  "PARTNERID_" + partnerId,"staff");
			String filename = userId + "_" + mode; //文件保存名称
			if(!staffDir.exists()) {
				staffDir.mkdirs();
			}else {
				File[] oldFiles = staffDir.listFiles(new FileFilter(filename));
				if(oldFiles != null && oldFiles.length>0) {
					for(File oldFile:oldFiles) {
						oldFile.delete();
					}
				}
			}
			File newFile = new File(staffDir, filename + "." + imgType.toLowerCase());
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("filename", newFile.getName());
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	

	/**
	 * 头像与客服二维码显示
	 * 保存相对路径：staff
	 * 1、头像保存名称：[userid]_headimg.xxx；
	 * 2、客服二维码保存名称：[userid]_qrcode.xxx;
	 * @param partnerId
	 * @param userId
	 * @param mode
	 * @param out
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/{partnerId}/show/{userId}/{mode}")
	public void showImg(@PathVariable("partnerId")Integer partnerId,
			@PathVariable("userId")Integer userId,
			@PathVariable("mode")String mode,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) throws IOException {
		if(!"headimg".equals(mode) && !"kfqrcode".equals(mode)) {
			return;
		}
		File dir = new File(this.partnerImgDir +  "PARTNERID_" + partnerId + "/staff" );
		if(!dir.exists()) {
			return;
		}
		File[] files = dir.listFiles(new FileFilter(mode));
		if(dir.isDirectory() && files != null && files.length>0) {
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
	 * 数据检查
	 * @param partnerId
	 * @param staff
	 * @param result
	 * @param passwd
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private String dataCheck(Integer partnerId,
			 PartnerStaff staff,BindingResult result,String passwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		JSONObject jsonRet = new JSONObject();
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
		PartnerBasic partner = this.partnerBasicService.getByID(staff.getPartnerId());
		if(partner == null) {
			jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
			jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
			return jsonRet.toString();
		}
		if(!partner.getPartnerId().equals(partnerId)) {
			jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
			jsonRet.put("errmsg", "您无权对该合作伙伴进行管理！");
			return jsonRet.toString();
		}
		UserBasic user = this.userBasicService.get(staff.getUserId());
		if(user == null || !"1".equals(user.getStatus())) {
			jsonRet.put("errmsg", "系统中没有该用户！");
			jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
			return jsonRet.toString();
		}
		String oldStatus = partner.getStatus();
		if(!"0".equals(oldStatus) && !"S".equals(oldStatus) && !"C".equals(oldStatus)) {
			jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
			jsonRet.put("errmsg", "您当前的合作伙伴状态不正确，不可进行员工管理！");
			return jsonRet.toString();
		}
		//操作员与密码验证
		Boolean isPass = false;
		String signPwd = SignUtils.encodeSHA256Hex(passwd);
		PartnerStaff operator = this.partnerStaffService.get(partnerId, staff.getUpdateOpr()); //员工
		VipBasic vip = this.vipBasicService.get(staff.getUpdateOpr());  
		if(vip != null && staff.getUpdateOpr().equals(partner.getVipId())) { //绑定会员
			if(signPwd.equals(vip.getPasswd())) { //会员密码验证
				isPass = true;
			}
		}
		if(isPass != true && operator != null) {
			if(signPwd.equals(operator.getPasswd())) { //员工密码验证
				isPass = true;
			}
		}
		if(!isPass) {
			jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
			jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
			return jsonRet.toString();
		}
		return null;
	}
	
	/**
	 * 查询指定查询条件、排序条件、分页条件的信息；
	 * @param jsonSearchParams	查询条件:{recId,userId,staffId,nickname,email,phone,isKf,tagId,updateOpr,status,beginUpdateTime,endUpdateTime}
	 * @param jsonPageCond		分页信息:{begin:, pageSize:}
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/{partnerId}/getall")
	public Object getAllByPartner(@PathVariable("partnerId")Integer partnerId,
			String jsonSearchParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			String sorts = " upate_time desc ";
			Map<String,Object> params = this.getSearchMap(jsonSearchParams);
			params.put("partnerId", partnerId);
			PageCond pageCond = null;
			if(jsonPageCond == null || jsonPageCond.length()<1) {
				pageCond = new PageCond(0,20);
			}else {
				pageCond = JSONObject.toJavaObject(JSONObject.parseObject(jsonPageCond), PageCond.class);
				if(pageCond == null) {
					pageCond = new PageCond(0,20);
				}
				if( pageCond.getBegin()<=0) {
					pageCond.setBegin(0);
				}
				if(pageCond.getPageSize()<2) {
					pageCond.setPageSize(20);
				}
			}
			int cnt = this.partnerStaffService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "没有获取到满足条件的记录信息！");
			if(cnt>0) {
				List<PartnerStaff> list = this.partnerStaffService.getAll(params, sorts, pageCond);
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
	
	private Map<String,Object> getSearchMap(String jsonSearchParams) {
		Map<String,Object> params = new HashMap<String,Object>();
		if(jsonSearchParams == null || jsonSearchParams.length()<=0) {
			return params;
		}
		JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
		if(jsonSearch.containsKey("recId")) {
			params.put("recId", jsonSearch.getLong("recId"));
		}
		if(jsonSearch.containsKey("userId")) {
			params.put("userId", jsonSearch.getInteger("userId"));
		}
		if(jsonSearch.containsKey("staffId")) {
			params.put("staffId", jsonSearch.getString("staffId"));
		}
		if(jsonSearch.containsKey("nickname")) {
			params.put("nickname", jsonSearch.getString("nickname"));
		}
		if(jsonSearch.containsKey("email")) {
			params.put("email", jsonSearch.getString("email"));
		}
		if(jsonSearch.containsKey("phone")) {
			params.put("phone", jsonSearch.getInteger("phone"));
		}
		if(jsonSearch.containsKey("isKf")) {
			params.put("isKf", jsonSearch.getString("isKf"));
		}
		if(jsonSearch.containsKey("tagId")) {
			params.put("tagId", jsonSearch.getString("tagId"));
		}
		if(jsonSearch.containsKey("updateOpr")) {
			params.put("updateOpr", jsonSearch.getString("updateOpr"));
		}
		if(jsonSearch.containsKey("status")) {
			params.put("status", jsonSearch.getString("status"));
		}
		if(jsonSearch.containsKey("beginUpdateTime")) {
			params.put("beginUpdateTime", jsonSearch.getString("beginUpdateTime"));
		}
		if(jsonSearch.containsKey("endUpdateTime")) {
			params.put("endUpdateTime", jsonSearch.getString("endUpdateTime"));
		}
		return params;
	}
}
