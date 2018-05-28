package com.mofangyouxuan.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.SysParam;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.SysParamService;
import com.mofangyouxuan.service.UserBasicService;

@RestController
@RequestMapping("/sysparam")
public class SysParamController {
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private UserBasicService userBasicService;
	
	/**
	 * 新增数据
	 * @param sysParam
	 * @param result
	 * @param userId
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{userId}/add")
	public Object save(@Valid SysParam sysParam,BindingResult result,@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			if(!sysParam.getUpdateOpr().equals(userId) || userId<1000) {//超级管理员才可执行
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
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
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			jsonRet = this.sysParamService.add(sysParam, userId);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 更新数据
	 * @param sysParam
	 * @param result
	 * @param userId
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{userId}/update")
	public Object update(@Valid SysParam sysParam,BindingResult result,@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			if(!sysParam.getUpdateOpr().equals(userId) || userId<1000) {//超级管理员才可执行
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
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
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				return jsonRet.toString();
			}
			jsonRet = this.sysParamService.update(sysParam, userId);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 获取所有系统参数
	 * @param userId
	 * @return {errcode,errmsg,datas:[{...},...]}
	 */
	@RequestMapping("/sys/getall")
	public Object getAll() {
		JSONObject jsonRet = new JSONObject();
		try {
			List<SysParam > list = this.sysParamService.getAll();
			if(list != null && list.size() >0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("datas", list);
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "没有获取到数据！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	

}
