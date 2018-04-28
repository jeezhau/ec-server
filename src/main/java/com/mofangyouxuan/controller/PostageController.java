package com.mofangyouxuan.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.Postage;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PostageService;
import com.mofangyouxuan.service.VipBasicService;

/**
 * 运费模板管理服务接口
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/postage")
public class PostageController {
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PostageService postageService;
	
	/**
	 * 获取指定ID的模版信息
	 * @param postageId
	 * @return {errcode:0,errmsg:"ok",postage:{...}}
	 */
	@RequestMapping("/get/{postageId}")
	public Object getById(@PathVariable("postageId")Long postageId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Postage postage = this.postageService.get(postageId);
			if(postage == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该模版信息！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("postage", postage);
			}
		}catch(Exception e) {
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}

	/**
	 * 获取指定合作伙伴的所有模版信息
	 * @param partnerId
	 * @return {errcode:0,errmsg:"ok",postages:[{...},{...},...]}
	 */
	@RequestMapping("/getbypartner/{partnerId}")
	public Object getByPartner(@PathVariable("partnerId")Integer partnerId) {
		JSONObject jsonRet = new JSONObject();
		try {
			List<Postage> postages = this.postageService.getByPartnerId(partnerId);
			if(postages == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴的模版信息！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("postages", postages);
			}
		}catch(Exception e) {
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}
	
	/**
	 * 统计指定正被使用的次数
	 * @param postageId
	 * @return {errcode:0,errmsg:"ok",cnt:1}
	 */
	@RequestMapping("/getusingcnt/{postageId}")
	public Object getUsingCnt(@PathVariable("postageId")Long postageId) {
		JSONObject jsonRet = new JSONObject();
		try {
			int cnt = this.postageService.getUsingCnt(postageId);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("cnt", cnt);
		}catch(Exception e) {
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}
	
	/**
	 * 添加新运费模版
	 * @param postage
	 * @param result
	 * @return {errcode:0,errmsg:"ok",postageId:111}
	 */
	@RequestMapping("/add")
	public Object add(@Valid Postage postage,BindingResult result,
			@RequestParam(value="currVipId",required=true)Integer currVipId) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vip = this.vipBasicService.get(currVipId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			PartnerBasic partner = this.partnerBasicService.getByBindUser(currVipId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			if(!postage.getPartnerId().equals(partner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.POSTAGE_PARAM_ERROR);
				return jsonRet.toString();
			}
			//其他验证
			
			Long id = this.postageService.add(postage);
			if(id != null) {
				jsonRet.put("postageId", 0);
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存出现错误！");
			}
		}catch(Exception e) {
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}
	
	/**
	 * 更新模板信息
	 * @param postage
	 * @param result
	 * @return {errcode:0,errmsg:"ok",postageId:111}
	 */
	@RequestMapping("/update")
	public Object update(@Valid Postage postage,BindingResult result,
			@RequestParam(value="currVipId",required=true)Integer currVipId) {
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
				jsonRet.put("errcode", ErrCodes.POSTAGE_PARAM_ERROR);
				return jsonRet.toString();
			}
			
			
			VipBasic vip = this.vipBasicService.get(currVipId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			PartnerBasic partner = this.partnerBasicService.getByBindUser(currVipId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			Postage old = this.postageService.get(postage.getPostageId());
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该模版信息！");
				return jsonRet.toString();
			}
			if(!postage.getPartnerId().equals(partner.getPartnerId()) 
					|| !postage.getPartnerId().equals(old.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			
			int cnt = this.postageService.update(postage);
			if(cnt > 0) {
				jsonRet.put("postageId", 0);
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存出现错误！");
			}
		}catch(Exception e) {
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}

	/**
	 * 删除指定运费模版
	 * 已在使用中的模板不可删除
	 * @param postageId
	 * @param currVipId
	 * @return {errcode:0,errmsg:"ok"}
	 */
	@RequestMapping("/delete")
	public Object delete(@RequestParam(value="postageId",required=true)Long postageId,
			@RequestParam(value="currVipId",required=true)Integer currVipId) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vip = this.vipBasicService.get(currVipId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			PartnerBasic partner = this.partnerBasicService.getByBindUser(currVipId);
			if(partner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该合作伙伴信息！");
				return jsonRet.toString();
			}
			Postage postage = this.postageService.get(postageId);
			if(postage == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该模版信息！");
				return jsonRet.toString();
			}
			if(!postage.getPartnerId().equals(partner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			int cnt = this.postageService.delete(postage);
			if(cnt > 0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", cnt);
				jsonRet.put("errmsg", "删除数据出现错误，错误码：" + cnt + "！");
			}
		}catch(Exception e) {
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}
}
