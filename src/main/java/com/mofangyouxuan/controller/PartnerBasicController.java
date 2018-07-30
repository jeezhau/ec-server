package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerSettle;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.FileFilter;
import com.mofangyouxuan.utils.SignUtils;

/**
 * 合作伙伴信息管理
 * 1	、合作伙伴证件照文件路径: 
 *  1) /证件照主目录/PARTNERID_[partnerId]/cert/证件类型.jpg
 *  2) /证件照主临时目录/VIPID_[vipId]/cert/证件类型.jpg
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/partner")
public class PartnerBasicController {
	
	@Value("${sys.partner-img-dir}")
	private String partnerImgDir;	//合作伙伴照片保存目录
	
	private String[] certTypeArr = {"logo","idcard1","idcard2","licence","agreement"}; 	//当前支持的证件类型

	@Autowired
	private VipBasicService vipBasicService;
	
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	
	@Autowired
	private SysParamUtil sysParamUtil;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
	
	/**
	 * 根据合作伙伴绑定的用户获取合作伙伴信息
	 * @param userId	绑定用户的ID
	 * 
	 * @return {errcode:0,errmsg:"ok",partner:{...},settle:{}}
	 * @throws JSONException 
	 */
	@RequestMapping("/get/byvip/{vipId}")
	public Object getPartnerByVip(@PathVariable(value="vipId",required=true)Integer vipId){
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vip = this.vipBasicService.get(vipId);
			if(vip == null ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员！");
				return jsonRet.toString();
			}
			
			PartnerBasic partner = this.partnerBasicService.getByBindUser(vipId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			PartnerSettle settle = this.partnerBasicService.getSettle(partner.getPartnerId());
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("partner", partner);
			jsonRet.put("settle", settle);
			return jsonRet;
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
	 * @return {errcode:0,errmsg:"ok",partner:{...},settle:{}}
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
			PartnerSettle settle = this.partnerBasicService.getSettle(partner.getPartnerId());
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("partner", partner);
			jsonRet.put("settle", settle);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	private String basicCheck(PartnerBasic basic) {
		String errmsg = "";
		List<PartnerBasic> list;
		Map<String,Object> params1 = new HashMap<String,Object>();
		//busiName,legalPername,legalPeridno,compType,compName,licenceNo,phone,
		params1.put("busiName", basic.getBusiName());
		list = this.partnerBasicService.getAll(params1, null, new PageCond(0,1));
		if(list != null && list.size()>0 && !list.get(0).getPartnerId().equals(basic.getPartnerId())) {
			errmsg += "经营名称：已被使用！";
		}
		Map<String,Object> params2 = new HashMap<String,Object>();
		params2.put("compName", basic.getCompName());
		list = this.partnerBasicService.getAll(params2, null, new PageCond(0,1));
		if(list != null && list.size()>0 && !list.get(0).getPartnerId().equals(basic.getPartnerId())) {
			errmsg += "公司名称：已被使用！";
		}
		Map<String,Object> params3 = new HashMap<String,Object>();
		params3.put("licenceNo", basic.getLicenceNo());
		list = this.partnerBasicService.getAll(params3, null, new PageCond(0,1));
		if(list != null && list.size()>0 && !list.get(0).getPartnerId().equals(basic.getPartnerId())) {
			errmsg += "营业执照号(个人身份证号)：已被使用！";
		}
		return errmsg;
	}
	
	public String settleCheck(PartnerSettle settle) {
		String errmsg = "";
		if(settle == null) {
			return errmsg;
		}
		if(settle.getIsRetfee() == null) {
			settle.setIsRetfee("0");
		}
		if(settle.getServiceFeeRate() == null) {
			settle.setServiceFeeRate(this.sysParamUtil.getDefaultServiceFeeRatio());
		}else {
			if(settle.getServiceFeeRate().compareTo(this.sysParamUtil.getSysHighestServiceFeeRate())>0) {
				errmsg += "平台服务费费率高于系统最高费率！";
			}else if(settle.getServiceFeeRate().compareTo(this.sysParamUtil.getSysLowestServiceFeeRate())<0) {
				errmsg += "平台服务费费率低于系统最低费率！";
			}
		}
		if(settle.getShareProfitRate() == null) {
			settle.setShareProfitRate(this.sysParamUtil.getDefaultPartnerProfitRatio());//设置默认分润比例
		}else {
			if(settle.getShareProfitRate().compareTo(new BigDecimal(70))>0){//分润率大于服务费费率70%
				errmsg += "交易奖励资金比例不可大于70%！";
			}else if(settle.getShareProfitRate().compareTo(new BigDecimal(0))<0){
				errmsg += "交易奖励资金比例不可小于0！";
			}
		}
		return errmsg;
	}

	/**
	 * 会员自己开通创建合作伙伴
	 * @param basic	合作伙伴信息
	 * @param result 字段验证结果
	 * 
	 * @return {errcode:0,errmsg:"ok","partnerId":111} 
	 * @throws JSONException
	 */
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public String create(@Valid PartnerBasic basic,BindingResult result1,
			@Valid PartnerSettle settle,BindingResult result2,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
			StringBuilder errSb = new StringBuilder();
			if(result1.hasErrors()){
				List<ObjectError> list = result1.getAllErrors();
				for(ObjectError e :list){
					errSb.append(e.getDefaultMessage());
				}
			}
			if(result2.hasErrors()){
				List<ObjectError> list = result2.getAllErrors();
				for(ObjectError e :list){
					errSb.append(e.getDefaultMessage());
				}
			}
			if(errSb.length()>0) {
				jsonRet.put("errmsg", errSb.toString());
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
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
				jsonRet.put("errmsg", "系统中已有该会员绑定的合作伙伴，如果需要修改信息请使用修改功能！");
				return jsonRet.toString();
			}
			if(!vip.getVipId().equals(basic.getUpdateOpr())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员ID与操作员ID不一致！");
				return jsonRet.toString();
			}
			if(old == null) {//开通需要积分检查
				if(vip.getScores() < this.sysParamUtil.getPartnerOpenNeedSocre()) {
					jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
					jsonRet.put("errmsg", "您的当前会员积分还不够开通合作伙伴，开通需要：" + this.sysParamUtil.getPartnerOpenNeedSocre() + "会员积分！");
					return jsonRet.toString();
				}
			}
			if(basic.getUpPartnerId() != null) {
				PartnerBasic newUp = this.partnerBasicService.getByID(basic.getUpPartnerId());
				if(newUp == null || !"S".equals(newUp.getStatus())) {
					jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
					jsonRet.put("errmsg", "该上级合作伙伴不存在！");
					return jsonRet.toString();
				}
			}
			//数据可用检查
			String errmsg = this.basicCheck(basic);
			if(errmsg != null && errmsg.length()>1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", errmsg);
				return jsonRet.toString();
			}
			if(settle == null) {
				settle = new PartnerSettle();
			}else {
				settle.setServiceFeeRate(null);
				settle.setShareProfitRate(null);
			}
			errmsg = this.settleCheck(settle);
			if(errmsg != null && errmsg.length()>1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", errmsg);
				return jsonRet.toString();
			}
			//证件照必填检查
			File tempCertDir = new File(this.partnerImgDir +  "VIPID_" + basic.getVipId() + "/cert/" );  //临时目录
			if(!tempCertDir.exists() || !tempCertDir.isDirectory()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传相关证书照片！");
				return jsonRet.toString();
			}
			if(tempCertDir.listFiles(new FileFilter("logo")).length<=0) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传企业LOGO照片！");
				return jsonRet.toString();
			}
			if(tempCertDir.listFiles(new FileFilter("idcard1")).length<=0 || tempCertDir.listFiles(new FileFilter("idcard2")).length<=0) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传法人身份证照片！");
				return jsonRet.toString();
			}
			if(tempCertDir.listFiles(new FileFilter("licence")).length<=0 ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传公司营业执照或小微商户法人手持身份证正面照片！");
				return jsonRet.toString();
			}
			if(tempCertDir.listFiles(new FileFilter("agreement")).length<=0 ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传签约协议照片！");
				return jsonRet.toString();
			}
			//密码验证
			if(vip.getPasswd() == null || vip.getPasswd().length()<10) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您还未设置会员密码，请先到会员中心完成设置！");
				return jsonRet.toJSONString();
			}
			if(!SignUtils.encodeSHA256Hex(passwd).equals(vip.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的会员密码输入不正确！");
				return jsonRet.toJSONString();
			}
			//数据处理
			if(basic.getUpPartnerId() == null) {
				basic.setUpPartnerId(this.sysParamUtil.getSysPartnerId());
			}
			basic.setStatus("0"); //待审核
			basic.setFreviewLog(null);
			basic.setFreviewOpr(null);
			basic.setFreviewTime(null);
			basic.setLreviewLog(null);
			basic.setLreviewOpr(null);
			basic.setLreviewTime(null);
			Integer id = this.partnerBasicService.add(basic,settle);
			if(id == null) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				File certDir = new File(this.partnerImgDir +  "PARTNERID_" + id + "/cert/" ); //初次确立后不可变更
				FileUtils.moveDirectory(tempCertDir, certDir);
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
	 * 会员自己或员工更新合作伙伴
	 * 1、合作伙伴类型，上级ID不可变更；
	 * @param basic	合作伙伴信息
	 * @param result 字段验证结果
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/update",method=RequestMethod.POST)
	public String update(@Valid PartnerBasic basic,BindingResult result1,
			@Valid PartnerSettle settle,BindingResult result2,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
			StringBuilder errSb = new StringBuilder();
			if(result1.hasErrors()){
				List<ObjectError> list = result1.getAllErrors();
				for(ObjectError e :list){
					errSb.append(e.getDefaultMessage());
				}
			}
			if(result2.hasErrors()){
				List<ObjectError> list = result2.getAllErrors();
				for(ObjectError e :list){
					errSb.append(e.getDefaultMessage());
				}
			}
			if(errSb.length()>0) {
				jsonRet.put("errmsg", errSb.toString());
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
			}
			//数据检查
			if(basic.getPartnerId() == null || basic.getVipId() == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "合作伙伴ID和绑定的会员ID均不可为空！");
				return jsonRet.toString();
			}
			PartnerBasic oldBasic = this.partnerBasicService.getByID(basic.getPartnerId());
			if(oldBasic == null ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该合作伙伴的信息！");
				return jsonRet.toString();
			}
			PartnerSettle oldSettle = this.partnerBasicService.getSettle(basic.getPartnerId());
			VipBasic vip = this.vipBasicService.get(basic.getVipId());
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员！");
				return jsonRet.toString();
			}
			if(!oldBasic.getVipId().equals(vip.getVipId())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员ID与合作伙伴ID不匹配！");
				return jsonRet.toString();
			}
			
			String oldStatus = oldBasic.getStatus();
			if(!"0".equals(oldStatus) && !"S".equals(oldStatus) && !"C".equals(oldStatus)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "您当前不可变更信息，状态不正确！");
				return jsonRet.toString();
			}
			//数据可用检查
			String errmsg = this.basicCheck(basic);
			if(errmsg != null && errmsg.length()>1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", errmsg);
				return jsonRet.toString();
			}
			if(settle == null) {
				if(oldSettle == null) {
					settle = new PartnerSettle();
				}else {
					settle = oldSettle;
				}
			}else {
				if(oldSettle != null) {
					settle.setServiceFeeRate(oldSettle.getServiceFeeRate());
					settle.setShareProfitRate(oldSettle.getShareProfitRate());
				}else {
					settle.setServiceFeeRate(null);
					settle.setShareProfitRate(null);
				}
			}
			errmsg = this.settleCheck(settle);
			if(errmsg != null && errmsg.length()>1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", errmsg);
				return jsonRet.toString();
			}
			//证件照必填检查
			File certDir = new File(this.partnerImgDir +  "PARTNERID_" + oldBasic.getPartnerId() + "/cert/" );
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
			if(certDir.listFiles(new FileFilter("licence")).length<=0 ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传公司营业执照或小微商户法人手持身份证正面照片！");
				return jsonRet.toString();
			}
			if(certDir.listFiles(new FileFilter("agreement")).length<=0 ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_CERT_IMAGE);
				jsonRet.put("errmsg", "您还未上传签约协议照片！");
				return jsonRet.toString();
			}
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(vip != null && basic.getUpdateOpr().equals(basic.getVipId())) { //绑定会员
				if(signPwd.equals(vip.getPasswd())) { //会员密码验证
					isPass = true;
				}
			}
			if(isPass != true ) {
				PartnerStaff operator = this.partnerStaffService.get(basic.getPartnerId(), basic.getUpdateOpr()); //员工&& operator != null) {
				if(operator != null && operator.getTagList() != null && operator.getTagList().contains("basic") && signPwd.equals(operator.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			//数据处理
			basic.setStatus("0"); //待审核
			if(oldSettle == null) {
				this.partnerBasicService.add(settle);
			}
			int cnt = this.partnerBasicService.updateBasic(basic,settle);
			if(cnt < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("partnerId", oldBasic.getPartnerId());
				jsonRet.put("errmsg", "ok");
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
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			PartnerBasic old = this.partnerBasicService.getByID(partnerId);
			if(old == null ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(old.getVipId());
			if(vip == null) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(vip != null && currUserId.equals(old.getVipId())) { //绑定会员
				if(signPwd.equals(vip.getPasswd())) { //会员密码验证
					isPass = true;
				}
			}
			if(isPass != true ) {
				PartnerStaff operator = this.partnerStaffService.get(partnerId, currUserId); //员工&& operator != null) {
				if(operator != null && operator.getTagList() != null && operator.getTagList().contains("basic") && signPwd.equals(operator.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			String oldStatus = old.getStatus();
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
			int cnt = this.partnerBasicService.changeStatusOwn(partnerId, newStatus);
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
	 * 证件照上传
	 * @param certType	证件类型
	 * @param image		照片,jpg格式
	 * @param userId		当前操作用户
	 * @return {errcode:0,errmsg:""}
	 */
	@RequestMapping("/cert/upload")
	public String uploadCert(@RequestParam(value="certType",required=true)String certType,
			@RequestParam(value="image")MultipartFile image,
			@RequestParam(value="bindVipId",required=true)Integer bindVipId,
			@RequestParam(value="userId",required=true)Integer userId,
			@RequestParam(value="passwd",required=true)String passwd) {
		
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
			VipBasic vip = this.vipBasicService.get(bindVipId);
			if(vip == null ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员！");
				return jsonRet.toString();
			}
			PartnerBasic partner = this.partnerBasicService.getByBindUser(bindVipId);
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(vip != null && bindVipId.equals(userId)) { //绑定会员
				if(signPwd.equals(vip.getPasswd())) { //会员密码验证
					isPass = true;
				}
			}
			if(isPass != true && partner != null) {
				PartnerStaff operator = this.partnerStaffService.get(partner.getPartnerId(), userId); //员工
				if(operator != null && operator.getTagList() != null && operator.getTagList().contains("basic") && signPwd.equals(operator.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			File certDir = new File(this.partnerImgDir +  "VIPID_" + vip.getVipId() + "/cert/" );
			if(partner != null) {//修改已存在合作伙伴
				String oldStatus = partner.getStatus();
				if(!"0".equals(oldStatus) && !"S".equals(oldStatus) && !"C".equals(oldStatus)) {
					jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
					jsonRet.put("errmsg", "您当前不可变更信息，状态不正确！");
					return jsonRet.toString();
				}
				if(certDir != null) {
					FileUtils.deleteDirectory(certDir);
				}
				certDir = new File(this.partnerImgDir +  "PARTNERID_" + partner.getPartnerId() + "/cert/" );
			}
			//照片保存，删除旧的
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
			//变更状态
			if(partner != null) {
				this.partnerBasicService.changeStatusOwn(partner.getPartnerId(), "0");
			}
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
	@RequestMapping("/cert/show/{partnerId}/{certType}")
	public void showCert(@PathVariable(value="certType",required=true)String certType,
			@PathVariable(value="partnerId",required=true)Integer partnerId,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) throws IOException {
		
		File certDir = new File(this.partnerImgDir +  "PARTNERID_" + partnerId + "/cert/" );
		File[] files = certDir.listFiles(new FileFilter(certType));
		if(certDir.exists() && certDir.isDirectory() && files != null && files.length>0) {
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
	 * 变更上级合作伙伴
	 * 1、合作伙伴上级变更其下级；
	 * 2、顶级合作伙伴为其变更；
	 * @param partnerId	被操作的合作伙伴ID
	 * @param newUpId	新的上级ID
	 * @param oldUpId	旧的上级ID
	 * @param oprPartnerId	操作人合作伙伴
	 * @param operator	操作人ID
	 * @param passwd		操作密码
	 * @return
	 */
	@RequestMapping("/changeUp")
	public Object changeUpPartner(@RequestParam(value="partnerId",required=true)Integer partnerId,
		@RequestParam(value="newUpId",required=true)Integer newUpId,
		@RequestParam(value="oprPartnerId",required=true)Integer oprPartnerId,
		@RequestParam(value="operator",required=true)Integer operator,
		@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			
			//数据检查
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "目标合作伙伴不存在！");
				return jsonRet.toString();
			}
			PartnerBasic oprPartner = this.partnerBasicService.getByID(oprPartnerId);
			if(oprPartner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "操作者合作伙伴不存在！");
				return jsonRet.toString();
			}
			if(!oprPartnerId.equals(this.sysParamUtil.getSysPartnerId()) && !oprPartnerId.equals(partner.getUpPartnerId())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权限执行该操作！");
				return jsonRet.toString();
			}
			PartnerBasic newUp = this.partnerBasicService.getByID(newUpId);
			if(newUp == null || !"S".equals(newUp.getStatus()) || !newUp.getPbTp().contains("2")) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "新推广上级合作伙伴不存在！");
				return jsonRet.toString();
			}
			
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(operator.equals(oprPartner.getVipId())) { //绑定会员
				VipBasic vip = this.vipBasicService.get(operator);
				if(vip == null) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员或未激活！");
					return jsonRet.toString();
				}
				if(signPwd.equals(vip.getPasswd())) {
					isPass = true;
				}
			}
			if(isPass != true) {
				PartnerStaff staff = this.partnerStaffService.get(oprPartnerId, operator);
				if(staff != null && staff.getTagList() != null && staff.getTagList().contains("mypartners") && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			
			//数据处理保存
			PartnerBasic upd = new PartnerBasic();
			upd.setPartnerId(partnerId);
			upd.setUpPartnerId(newUpId);
			upd.setUpdateOpr(operator);
			int cnt = this.partnerBasicService.updateSelective(upd);
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
	 * 合作伙伴审核与抽查
	 * 上级审批下级信息
	 * 1、上级可对下级进行审核；
	 * 2、顶级对所有合作伙伴进行最终审核；
	 * 3、仅顶级审核通过后才算通过；
	 * 4、设置对合作伙伴设置的结算信息
	 * @param partnerId	待审批合作伙伴ID
	 * @param review 	审批意见
	 * @param result 	审批结果：S-通过，R-拒绝
	 * @param rewPartnerId	审批人合作伙伴ID
	 * @param operator	审批人ID，为上级合作伙伴的员工用户ID
	 * @param passwd		审批人操作密码
	 * @param settle		设置的结算信息，通过时有效
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException
	 */
	@RequestMapping("/review")
	public String review(@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="review",required=true)String review,
			@RequestParam(value="result",required=true)String result,
			@RequestParam(value="rewPartnerId",required=true)Integer rewPartnerId,
			@RequestParam(value="operator",required=true)Integer operator,
			@RequestParam(value="passwd",required=true)String passwd,
			PartnerSettle settle){
		JSONObject jsonRet = new JSONObject();
		try {
			if(!"S".equals(result) && !"R".equals(result) && !"1".equals(result)) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "审批结果不正确（S-通过，R-拒绝）！");
				return jsonRet.toString();
			}
			if(review == null || review.length()<2 || review.length()>600) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "审批意见：长度2-600字符！");
				return jsonRet.toString();
			}
			//数据检查
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null || (!"0".equals(partner.getStatus()) && !partner.getStatus().contains("S") && !partner.getStatus().contains("R")) ) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "该合作伙伴不存在或状态不正确！");
				return jsonRet.toString();
			}
			PartnerBasic rewPartner = this.partnerBasicService.getByID(rewPartnerId);
			if(rewPartner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "审核者合作伙伴不存在！");
				return jsonRet.toString();
			}
			if(!rewPartnerId.equals(this.sysParamUtil.getSysPartnerId()) && !rewPartnerId.equals(partner.getUpPartnerId())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权限执行该操作！");
				return jsonRet.toString();
			}
			
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(operator.equals(rewPartner.getVipId())) { //绑定会员
				VipBasic vip = this.vipBasicService.get(operator);
				if(vip == null || !"1".equals(vip.getStatus()) ) {
					jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
					jsonRet.put("errmsg", "系统中没有该会员或未激活！");
					return jsonRet.toString();
				}
				if(signPwd.equals(vip.getPasswd())) {
					isPass = true;
				}
			}
			if(isPass != true) {
				PartnerStaff staff = this.partnerStaffService.get(rewPartnerId, operator);
				if(staff != null && staff.getTagList() != null && staff.getTagList().contains("mypartners") && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			//数据处理保存
			PartnerSettle oldSettle = this.partnerBasicService.getSettle(partnerId);
			if(settle == null) {
				if(oldSettle == null) {
					settle = new PartnerSettle();
				}else {
					settle = oldSettle;
				}
			}else {
				if(oldSettle != null) {
					settle.setIsRetfee(oldSettle.getIsRetfee());
				}
			}
			String errmsg = this.settleCheck(settle);
			if(errmsg != null && errmsg.length()>1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", errmsg);
				return jsonRet.toString();
			}
			int cnt ;
			if(!rewPartnerId.equals(this.sysParamUtil.getSysPartnerId())) {//初审
				if("S".equals(result)) {
					result = "B";
				}else if("R".equals(result)) {
					result = "A";
				}
				cnt = this.partnerBasicService.firstReview(partnerId, rewPartnerId+"#"+operator, review, result,settle);
			}else {//终审
				cnt = this.partnerBasicService.lastReview(partnerId, rewPartnerId+"#"+operator, review, result,settle);
			}
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
	 * 查询指定查询条件、排序条件、分页条件的信息；
	 * @param jsonSearchParams	查询条件:{partnerId,pbTp,upPartnerId,country,province,city,area,busiName,legalPername,legalPeridno,compType,compName,licenceNo,phone,status,beginUpdateTime,endUpdateTime}
	 * @param jsonPageCond		分页信息:{begin:, pageSize:}
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/getall/{upPartnerId}")
	public Object getAll(@PathVariable("upPartnerId")Integer upPartnerId,
			String jsonSearchParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			String sorts = " order by update_time desc ";
			Map<String,Object> params = this.getSearchMap(jsonSearchParams);
			if(!upPartnerId.equals(this.sysParamUtil.getSysPartnerId())) {
				params.put("upPartnerId", upPartnerId);
			}
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
			int cnt = this.partnerBasicService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "没有获取到满足条件的记录信息！");
			if(cnt>0) {
				List<PartnerBasic> list = this.partnerBasicService.getAll(params, sorts, pageCond);
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
		if(jsonSearch.containsKey("partnerId")) {
			params.put("partnerId", jsonSearch.getInteger("partnerId"));
		}
		if(jsonSearch.containsKey("pbTp")) {
			params.put("pbTp", jsonSearch.getInteger("pbTp"));
		}
		if(jsonSearch.containsKey("upPartnerId")) {
			params.put("upPartnerId", jsonSearch.getString("upPartnerId"));
		}
		if(jsonSearch.containsKey("country")) {
			params.put("country", jsonSearch.getString("country"));
		}
		if(jsonSearch.containsKey("province")) {
			params.put("province", jsonSearch.getString("province"));
		}
		if(jsonSearch.containsKey("city")) {
			params.put("city", jsonSearch.getString("city"));
		}
		if(jsonSearch.containsKey("area")) {
			params.put("area", jsonSearch.getString("area"));
		}
		if(jsonSearch.containsKey("busiName")) {
			params.put("busiName", jsonSearch.getString("busiName"));
		}
		if(jsonSearch.containsKey("legalPername")) {
			params.put("legalPername", jsonSearch.getString("legalPername"));
		}
		if(jsonSearch.containsKey("legalPeridno")) {
			params.put("legalPeridno", jsonSearch.getString("legalPeridno"));
		}
		if(jsonSearch.containsKey("phone")) {
			params.put("phone", jsonSearch.getString("phone"));
		}
		if(jsonSearch.containsKey("compType")) {
			params.put("compType", jsonSearch.getString("compType"));
		}
		if(jsonSearch.containsKey("compName")) {
			params.put("compName", jsonSearch.getString("compName"));
		}
		if(jsonSearch.containsKey("licenceNo")) {
			params.put("licenceNo", jsonSearch.getString("licenceNo"));
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

