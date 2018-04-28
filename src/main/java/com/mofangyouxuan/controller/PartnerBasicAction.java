package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.FileFilter;

/**
 * 合作伙伴信息管理
 * 1	、合作伙伴证件照文件路径: /证件照主目录/VIPID_[vipId]/证件类型.jpg
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/partner")
public class PartnerBasicAction {
	
	@Value("${sys.partner-cert-dir}")
	private String partnerCertDir;	//合作伙伴证件照片保存目录
	
	private String[] certTypeArr = {"logo","idcard1","idcard2","licence"}; 	//当前支持的证件类型
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	
	@Autowired
	private PartnerBasicService partnerBasicService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
	
	/**
	 * 根据合作伙伴绑定的用户获取合作伙伴信息
	 * @param userId	绑定用户的ID
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {合作伙伴的所有字段}
	 * @throws JSONException 
	 */
	@RequestMapping("/get/byuser/{userId}")
	public Object getPartnerByUser(@PathVariable(value="userId",required=true)Integer userId){
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(userId);
			if(user == null) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			
			VipBasic vip = this.vipBasicService.get(user.getUserId());
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			
			PartnerBasic partner = this.partnerBasicService.getByBindUser(user.getUserId());
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			return partner;
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	/**
	 * 根据合作伙伴绑定的用户获取合作伙伴信息
	 * @param 合作伙伴ID
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {合作伙伴的所有字段}
	 * @throws JSONException 
	 */
	@RequestMapping("/get/byid/{partnerId}")
	public Object getPartnerByID(@PathVariable("partnerId")Integer partnerId){
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			return partner;
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	

	/**
	 * 开通创建合作伙伴
	 * @param basic	合作伙伴信息
	 * @param result 字段验证结果
	 * 
	 * @return {errcode:0,errmsg:"ok","partnerId":111} 
	 * @throws JSONException
	 */
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public String create(@Valid PartnerBasic basic,BindingResult result) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
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
			VipBasic vip = this.vipBasicService.get(basic.getVipId());
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			PartnerBasic old = this.partnerBasicService.getByBindUser(vip.getVipId());
			if( old != null) {	//已有，直接返回成功
				jsonRet.put("errcode", 0);
				jsonRet.put("partnerId", old.getPartnerId());
				jsonRet.put("errmsg", "系统中已有该合作伙伴，如果需要修改信息请使用修改功能！");
				return jsonRet.toString();
			}
			//证件照必填检查
			File certDir = new File(this.partnerCertDir + "VIPID_" + basic.getVipId());
			if(!certDir.exists() || !certDir.isDirectory()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传相关证书照片！");
				return jsonRet.toString();
			}
			if(certDir.listFiles(new FileFilter("logo")).length<=0) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传企业LOGO照片！");
				return jsonRet.toString();
			}
			if(certDir.listFiles(new FileFilter("idcard1")).length<=0 || certDir.listFiles(new FileFilter("idcard2")).length<=0) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传法人身份证照片！");
				return jsonRet.toString();
			}
			String compType = basic.getCompType();
			if("2".equals(compType)) {
				if(certDir.listFiles(new FileFilter("licence")).length<=0 ) {
					jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
					jsonRet.put("errmsg", "您还未上传营业执照照片！");
					return jsonRet.toString();
				}
			}
			//数据处理
			basic.setStatus("0"); //待审核
			basic.setReviewLog("");
			basic.setReviewOpr(null);
			basic.setReviewTime(null);
			basic.setCertDir(this.partnerCertDir + "VIPID_" + basic.getVipId()); //初次确立后不可变更
			Integer id = this.partnerBasicService.add(basic);
			if(id == null) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("partnerId", id);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	/**
	 * 更新合作伙伴
	 * 
	 * @param basic	合作伙伴信息
	 * @param result 字段验证结果
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/update",method=RequestMethod.POST)
	public String update(@Valid PartnerBasic basic,BindingResult result) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
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
			if(basic.getPartnerId() == null || basic.getVipId() == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "合作伙伴ID和绑定的会员ID均不可为空！");
				return jsonRet.toString();
			}
			PartnerBasic old1 = this.partnerBasicService.getByID(basic.getPartnerId());
			PartnerBasic old2 = this.partnerBasicService.getByBindUser(basic.getVipId());
			if(old1 == null || old2 == null || !old1.getPartnerId().equals(old2.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员不存在或合作伙伴ID和绑定的会员ID不匹配！");
				return jsonRet.toString();
			}
			String oldStatus = old1.getStatus();
			if(!"0".equals(oldStatus) && !"S".equals(oldStatus) && !"C".equals(oldStatus)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "您当前不可变更信息，状态不正确！");
				return jsonRet.toString();
			}
			//证件照必填检查
			File certDir = new File(this.partnerCertDir + "VIPID_" + basic.getVipId());
			if(!certDir.exists() || !certDir.isDirectory()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传相关证书照片！");
				return jsonRet.toString();
			}
			if(certDir.listFiles(new FileFilter("logo")).length<=0) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传企业LOGO照片！");
				return jsonRet.toString();
			}
			if(certDir.listFiles(new FileFilter("idcard1")).length<=0 || certDir.listFiles(new FileFilter("idcard2")).length<=0) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传法人身份证照片！");
				return jsonRet.toString();
			}
			String compType = basic.getCompType();
			if("2".equals(compType)) {
				if(certDir.listFiles(new FileFilter("licence")).length<=0 ) {
					jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
					jsonRet.put("errmsg", "您还未上传营业执照照片！");
					return jsonRet.toString();
				}
			}
			
			//数据处理
			basic.setStatus("0"); //待审核
			basic.setReviewLog("");
			basic.setReviewOpr(null);
			basic.setReviewTime(null);
			basic.setCertDir(old1.getCertDir()); //初次确立后不可变更
			
			int cnt = this.partnerBasicService.update(basic);
			if(cnt < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("partnerId", old1.getPartnerId());
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
	 * 合作伙伴自我变更状态：关闭、打开
	 * 
	 * @param partnerId
	 * @param currUserId	当前用户ID
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping("/changeStatus")
	public String changeOwnStatus(@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId) {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic old1 = this.partnerBasicService.getByID(partnerId);
			PartnerBasic old2 = this.partnerBasicService.getByBindUser(currUserId);
			if(old1 == null || old2 == null || !old1.getPartnerId().equals(old2.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "合作伙伴ID和绑定的会员ID不匹配！");
				return jsonRet.toString();
			}
			String oldStatus = old1.getStatus();
			if(!"S".equals(oldStatus) && !"C".equals(oldStatus)) { //正常或关闭
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "您不可变更当前状态！！");
				return jsonRet.toString();
			}
			String newStatus = "";
			if("S".equals(oldStatus)) {
				newStatus = "C";
			}else {
				newStatus = "S";
			}
			int cnt = this.partnerBasicService.changeShopStatus(partnerId, newStatus);
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
	 * 合作伙伴审核
	 * 
	 * @param partnerId	带审批合作伙伴ID
	 * @param currUserId	审批人
	 * @param review 审批意见
	 * @param result 审批结果：S-通过，R-拒绝
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException
	 */
	@RequestMapping("/review")
	public String review(@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="review",required=true)String review,
			@RequestParam(value="result",required=true)String result){
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(currUserId);
			if(user == null || user.getUserId()<100 || user.getUserId()>=1000) {
				jsonRet.put("errcode", ErrCodes.USER_NOT_REVIEW_ADMIN);
				jsonRet.put("errmsg", "该用户不是审核管理员！");
				return jsonRet.toString();
			}
			PartnerBasic old = this.partnerBasicService.getByID(partnerId);
			if(old == null || !"0".equals(old.getStatus())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "该合作伙伴不存在或状态不正确！");
				return jsonRet.toString();
			}
			if(!"S".equals(result) && !"R".equals(result)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "审批结果不正确（S-通过，R-拒绝）！");
				return jsonRet.toString();
			}
			int cnt = this.partnerBasicService.review(partnerId, currUserId, review, result);
			if(cnt < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//异常处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 证件照上传
	 * @param certType	证件类型
	 * @param image		照片,jpg格式
	 * @param currUserId	当前操作用户
	 * @return {errcode:0,errmsg:""}
	 */
	@RequestMapping("/cert/upload")
	public String uploadCert(@RequestParam(value="certType",required=true)String certType,
			@RequestParam(value="image")MultipartFile image,
			@RequestParam(value="currUserId",required=true)Integer currUserId) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "证件照片信息不可为空！");
				return jsonRet.toString();
			}
			//文件类型判断
			String imgType = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf('.')+1);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "证件图片文件必须是jpg,jpeg,png格式！");
				return jsonRet.toString();
			}
			//证件类型判断
			boolean flag = false;
			for(String tp:certTypeArr) {
				if(tp.equals(certType)) {
					flag = true;
					break;
				}
			}
			if(!flag) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "证件类型只可是：" + Arrays.toString(certTypeArr) + "！");
				return jsonRet.toString();
			}
			//数据检查
			VipBasic vip = this.vipBasicService.get(currUserId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			//照片保存，删除旧的
			File certDir = new File(this.partnerCertDir,"VIPID_" + currUserId);
			if(!certDir.exists()) {
				certDir.mkdirs();
			}else {
				File[] oldFiles = certDir.listFiles(new FileFilter(certType));
				if(oldFiles != null && oldFiles.length>0) {
					for(File oldFile:oldFiles) {
						oldFile.delete();
					}
				}
			}
			File newFile = new File(certDir,certType + "." + imgType.toLowerCase());
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
		
	}
	/**
	 * 显示证件照
	 * @param certType
	 * @param currUserId
	 * @param out
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/cert/show/{currUserId}/{certType}")
	public void showCert(@PathVariable(value="certType",required=true)String certType,
			@PathVariable(value="currUserId",required=true)Integer currUserId,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) throws IOException {
		File dir = new File(this.partnerCertDir+"VIPID_"+currUserId);
		File[] files = dir.listFiles(new FileFilter(certType));
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
