package com.mofangyouxuan.controller;

import java.math.BigDecimal;
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
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.Postage;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PostageService;
import com.mofangyouxuan.service.impl.AuthSecret;

/**
 * 运费模板管理服务接口
 * 1、已被使用中的模版可被编辑修改，但在提交之前给出提示，确认后方可提交后台；
 * 2、已被使用的模版不可删除；
 * 3、同一个合作伙伴下不可有两个同名的模版；
 * 4、每个合作伙伴的邮费模板有数量限制；
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/postage")
public class PostageController {
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PostageService postageService;
	@Autowired
	private AuthSecret authSecret;
	
	/**
	 * 获取指定ID的模版信息
	 * @param postageId
	 * @return {errcode:0,errmsg:"ok",postage:{...}}
	 */
	@RequestMapping("/{partnerId}/get/{postageId}")
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
			e.printStackTrace();
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
	@RequestMapping("/{partnerId}/getall")
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
			e.printStackTrace();
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
	@RequestMapping("/{partnerId}/getusingcnt/{postageId}")
	public Object getUsingCnt(@PathVariable("postageId")Long postageId) {
		JSONObject jsonRet = new JSONObject();
		try {
			int cnt = this.postageService.getUsingCnt(postageId);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("cnt", cnt);
		}catch(Exception e) {
			e.printStackTrace();
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
	@RequestMapping("/{partnerId}/add")
	public Object add(@Valid Postage postage,BindingResult result,
			@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
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
			//其他验证
			String isCityWide = postage.getIsCityWide();
			String isFree = postage.getIsFree();
			StringBuilder sb = new StringBuilder();
			if(!"1".equals(isFree)) {//非无条件免邮
				Integer firstWeight = postage.getFirstWeight();
				BigDecimal firstWPrice = postage.getFirstWPrice();
				Integer additionWeight = postage.getAdditionWeight();
				BigDecimal additionWPrice = postage.getAdditionWPrice();
				if(firstWeight == null || 
						firstWPrice == null ||
						additionWeight == null ||
						additionWPrice == null) {
					sb.append(" 首重、首重价格、续重、续重价格：不可为空！");
				}
				if(isFree.contains("2")) {//重量限制免邮
					Integer freeWeight = postage.getFreeWeight();
					if(freeWeight == null) {
						sb.append(" 免邮重量：不可为空，最小值1(kg)！");
					}
				}
				if(isFree.contains("3")) {//金额限制免邮
					BigDecimal freeAmount = postage.getFreeAmount();
					if(freeAmount == null) {
						sb.append(" 免邮金额：不可为空，最小值1(元)！");
					}
				}
				if("0".equals(isCityWide)) {//全国
					;
				}else {//同城
					if(isFree.contains("4")) {//距离限制免邮
						Integer freeDist = postage.getFreeDist();
						if(freeDist == null) {
							sb.append(" 免邮距离：不可为空，最小值1(km)！");
						}
					}
					Integer firstDist = postage.getFirstDist();
					BigDecimal firstDPrice = postage.getFirstDPrice();
					Integer additionDist = postage.getAdditionDist();
					BigDecimal additionDPrice = postage.getAdditionDPrice();
					if(firstDist == null || 
							firstDPrice == null ||
							additionDist == null ||
							additionDPrice == null) {
						sb.append(" 首距、首距价格、续距、续距价格：不可为空！");
					}
				}
			}
			if("0".equals(isCityWide)) {//全国
				String provLimit = postage.getProvLimit();
				if(provLimit == null || provLimit.length()<2) {
					sb.append(" 配送省份： 不可为空！");
				}
				if(provLimit.contains("全国")) {
					postage.setProvLimit("全国");
				}
			}else {//同城
				Integer distLimit = postage.getDistLimit();
				if(distLimit == null) {
					sb.append(" 配送距离： 不可为空！");
				}
			}

			if(sb.length()>0) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PARAM_ERROR);
				jsonRet.put("errmsg", sb.toString());
				return jsonRet.toString();
			}
			//安全检查
			PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.postage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			if(!postage.getPartnerId().equals(myPartner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//数据处理
			postage.setUpdateOpr(currUserId);
			Long id = this.postageService.add(postage);
			if(id  > 0) {
				jsonRet.put("postageId", id);
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", id);
				jsonRet.put("errmsg", "数据保存出现错误！错误码：" + id);
			}
		}catch(Exception e) {
			e.printStackTrace();
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
	@RequestMapping("/{partnerId}/update")
	public Object update(@Valid Postage postage,BindingResult result,
			@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
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
			//其他验证
			String isCityWide = postage.getIsCityWide();
			String isFree = postage.getIsFree();
			StringBuilder sb = new StringBuilder();
			if(!"1".equals(isFree)) {//非无条件免邮
				Integer firstWeight = postage.getFirstWeight();
				BigDecimal firstWPrice = postage.getFirstWPrice();
				Integer additionWeight = postage.getAdditionWeight();
				BigDecimal additionWPrice = postage.getAdditionWPrice();
				if(firstWeight == null || 
						firstWPrice == null ||
						additionWeight == null ||
						additionWPrice == null) {
					sb.append(" 首重、首重价格、续重、续重价格：不可为空！");
				}
				if(isFree.contains("2")) {//重量限制免邮
					Integer freeWeight = postage.getFreeWeight();
					if(freeWeight == null) {
						sb.append(" 免邮重量：不可为空，最小值1(kg)！");
					}
				}
				if(isFree.contains("3")) {//金额限制免邮
					BigDecimal freeAmount = postage.getFreeAmount();
					if(freeAmount == null) {
						sb.append(" 免邮金额：不可为空，最小值1(元)！");
					}
				}
				if("0".equals(isCityWide)) {//全国
					String provLimit = postage.getProvLimit();
					if(provLimit == null || provLimit.length()<2) {
						sb.append(" 配送省份： 不可为空！");
					}
					if(provLimit.contains("全国")) {
						postage.setProvLimit("全国");
					}
				}else {//同城
					if(isFree.contains("4")) {//距离限制免邮
						Integer freeDist = postage.getFreeDist();
						if(freeDist == null) {
							sb.append(" 免邮距离：不可为空，最小值1(km)！");
						}
					}
					Integer firstDist = postage.getFirstDist();
					BigDecimal firstDPrice = postage.getFirstDPrice();
					Integer additionDist = postage.getAdditionDist();
					BigDecimal additionDPrice = postage.getAdditionDPrice();
					if(firstDist == null || 
							firstDPrice == null ||
							additionDist == null ||
							additionDPrice == null) {
						sb.append(" 首距、首距价格、续距、续距价格：不可为空！");
					}
				}
			}
			if("0".equals(isCityWide)) {//全国
				String provLimit = postage.getProvLimit();
				if(provLimit == null || provLimit.length()<2) {
					sb.append(" 配送省份： 不可为空！");
				}
				if(provLimit.contains("全国")) {
					postage.setProvLimit("全国");
				}
			}else {//同城
				Integer distLimit = postage.getDistLimit();
				if(distLimit == null) {
					sb.append(" 配送距离： 不可为空！");
				}
			}

			if(sb.length()>0) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PARAM_ERROR);
				jsonRet.put("errmsg", sb.toString());
				return jsonRet.toString();
			}
			//安全检查
			PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.postage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			if(!postage.getPartnerId().equals(myPartner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			if(!postage.getPartnerId().equals(myPartner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			Postage old = this.postageService.get(postage.getPostageId());
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该模版信息！");
				return jsonRet.toString();
			}
			if(!postage.getPartnerId().equals(myPartner.getPartnerId()) 
					|| !postage.getPartnerId().equals(old.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			//数据处理
			postage.setUpdateOpr(currUserId);
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
			e.printStackTrace();
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
	@RequestMapping("/{partnerId}/delete")
	public Object delete(@RequestParam(value="postageId",required=true)Long postageId,
			@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.postage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			//数据检查
			Postage postage = this.postageService.get(postageId);
			if(postage == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该模版信息！");
				return jsonRet.toString();
			}
			if(!postage.getPartnerId().equals(myPartner.getPartnerId()) ) {
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
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}
}
