package com.mofangyouxuan.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;

/**
 * 合作伙伴信息管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/partner")
public class PartnerBasicAction {
	
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
	public Object getPartnerByUser(@PathVariable(value="userId",required=true)Integer userId) throws JSONException {
		JSONObject jsonRet = new JSONObject();
		UserBasic user = this.userBasicService.get(userId);
		if(user == null) {
			jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
			jsonRet.put("errmsg", "系统中没有该用户！");
			return jsonRet.toString();
		}
		
		VipBasic vip = this.vipBasicService.get(user.getId());
		if(vip == null || !"1".equals(vip.getStatus()) ) {
			jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
			jsonRet.put("errmsg", "系统中没有该会员或未激活！");
			return jsonRet.toString();
		}
		if(!"1".equals(vip.getIsPartner())) {
			jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
			jsonRet.put("errmsg", "您还没开通合作伙伴功能！");
			return jsonRet.toString();
		}
		
		PartnerBasic partner = this.partnerBasicService.getByBindUser(user.getId());
		if(partner == null) {
			jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
			jsonRet.put("errmsg", "您还没开通合作伙伴功能！");
			return jsonRet.toString();
		}
		return partner;
	}
	
	/**
	 * 根据合作伙伴绑定的用户获取合作伙伴信息
	 * @param 合作伙伴ID
	 * 
	 * @return {errcode:0,errmsg:"ok"} 或 {合作伙伴的所有字段}
	 * @throws JSONException 
	 */
	@RequestMapping("/get/byid/{partnerId}")
	public Object getPartnerByID(@PathVariable("partnerId")Integer partnerId) throws JSONException {
		JSONObject jsonRet = new JSONObject();
		
		PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
		if(partner == null) {
			jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
			jsonRet.put("errmsg", "您还没开通合作伙伴功能！");
			return jsonRet.toString();
		}
		return partner;
	}
	
	

	/**
	 * 开通创建合作伙伴
	 * @param basic	合作伙伴信息
	 * @param result 字段验证结果
	 * 
	 * @return {errcode:0,errmsg:"ok"} 
	 * @throws JSONException
	 */
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public String create(@Valid PartnerBasic basic,BindingResult result) throws JSONException {
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
			UserBasic user = this.userBasicService.get(basic.getId());
			if(user == null) {
				jsonRet.put("errmsg", "系统中该用户不存在！");
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(user.getId());
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			PartnerBasic old = this.partnerBasicService.getByBindUser(user.getId());
			if( old != null) {	//已有，直接返回成功
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "系统中已有该合作伙伴，如果需要修改信息请使用修改功能！");
				return jsonRet.toString();
			}
			//数据处理
			basic.setStatus("0"); //待审核
			Integer id = this.partnerBasicService.add(basic);
			if(id == null) {
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
	 * 更新合作伙伴
	 * 
	 * @param basic	合作伙伴信息
	 * @param result 字段验证结果
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping(value="/update",method=RequestMethod.POST)
	public String update(@Valid PartnerBasic basic,BindingResult result) throws JSONException {
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
			VipBasic vip = this.vipBasicService.get(basic.getUserId());
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			if(basic.getId() == null || basic.getUserId() == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "合作伙伴ID和绑定的用户ID均不可为空！");
				return jsonRet.toString();
			}
			PartnerBasic old1 = this.partnerBasicService.getByID(basic.getId());
			PartnerBasic old2 = this.partnerBasicService.getByBindUser(basic.getUserId());
			if(old1 == null || old2 == null || !old1.getId().equals(old2.getId())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "合作伙伴ID和绑定的用户ID不匹配！");
				return jsonRet.toString();
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
	 * 变更合作伙伴状态：关闭、打开
	 * 
	 * @param partnerId
	 * @param currUserId	当前用户ID
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping("/changeStatus")
	public String changeStatus(@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId) throws JSONException {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic old1 = this.partnerBasicService.getByID(partnerId);
			PartnerBasic old2 = this.partnerBasicService.getByBindUser(currUserId);
			if(old1 == null || old2 == null || !old1.getId().equals(old2.getId())) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "合作伙伴ID和绑定的用户ID不匹配！");
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
			@RequestParam(value="result",required=true)String result) throws JSONException {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(currUserId);
			if(user == null || user.getId()<100 || user.getId()>=1000) {
				jsonRet.put("errcode", ErrCodes.USER_NOT_REVIEW_ADMIN);
				jsonRet.put("errmsg", "该用户不是审核管理员！");
				return jsonRet.toString();
			}
			PartnerBasic old = this.partnerBasicService.getByID(partnerId);
			if(old == null || "0".equals(old.getStatus())) {
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

}
