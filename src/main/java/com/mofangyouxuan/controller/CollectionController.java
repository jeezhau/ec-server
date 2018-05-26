package com.mofangyouxuan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.Collection;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.CollectionService;
import com.mofangyouxuan.service.UserBasicService;

@RestController
@RequestMapping("/collection")
public class CollectionController {
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private UserBasicService userBasicService;
	
	/**
	 * 添加收藏
	 * @param collection
	 * @param result
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{userId}/add")
	public Object add(Collection collection,BindingResult result,
			@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			if(!collection.getUserId().equals(userId)) {
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
			jsonRet = this.collectionService.add(collection);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 删除收藏
	 * @param collection
	 * @param result
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{userId}/delete")
	public Object delete(Collection collection,BindingResult result,
			@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			if(!collection.getUserId().equals(userId)) {
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
			int cnt = this.collectionService.delete(collection);
			if(cnt >0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库数据保存错误！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 统计所有收藏收藏
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{userId}/count")
	public Object countAll(@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			int cnt = this.collectionService.countUsersAll(userId);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("cnt", cnt);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 获取用户所有
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{userId}/getall")
	public Object getAll(@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic userBasic = this.userBasicService.get(userId);
			if(userBasic == null || !"1".equals(userBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			List<Collection > list = this.collectionService.getUsersAll(userId);
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
