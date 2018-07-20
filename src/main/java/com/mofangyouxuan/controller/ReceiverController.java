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
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.Receiver;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.ReceiverService;
import com.mofangyouxuan.service.UserBasicService;

/**
 * 收货人信息管理
 * 1、每位用户(买家或卖家合作伙伴)的收货人信息数量有限制；
 * 2、每位用户都有一个默认收货人信息，第一个系统自动设为默认；
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/receiver/{userId}")
public class ReceiverController {
	
	@Autowired
	private ReceiverService  receiverService;
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	
	
	/**
	 * 获取指定用户的默认收货人信息
	 * @param userId
	 * @return {errcode:0,errmsg:"ok",receiver:{...}}
	 */
	@RequestMapping("/getdefault/{recvType}")
	public Object getDefault(@PathVariable("userId")Integer userId,@PathVariable("recvType")String recvType) {
		JSONObject jsonRet = new JSONObject();
		try {
			Receiver receiver = this.receiverService.getDefault(userId,recvType);
			if(receiver == null) {
				jsonRet.put("errcode", ErrCodes.RECEIVER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户的默认信息！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("receiver", receiver);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}

	/**
	 * 获取指定用户的所有收货人信息
	 * @param userId
	 * @return {errcode:0,errmsg:"ok",datas:[{...},{...},...]}
	 */
	@RequestMapping("/getbyuser/{recvType}")
	public Object getAllByUser(@PathVariable("userId")Integer userId,@PathVariable("recvType")String recvType) {
		JSONObject jsonRet = new JSONObject();
		try {
			List<Receiver> datas = this.receiverService.getAllByUser(userId,recvType);
			if(datas == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户的收货人信息！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("datas", datas);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}
	
	/**
	 * 设置默认收货人信息
	 * @param userId
	 * @param recvId
	 * @return {"errcode":0,"errmsg":"ok"}
	 */
	@RequestMapping("/setdefault/{recvId}")
	public Object setDefault(@PathVariable("userId")Integer userId,@PathVariable("recvId")Long recvId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Receiver receiver = this.receiverService.getById(recvId);
			if(receiver == null) {
				jsonRet.put("errcode", ErrCodes.RECEIVER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该收货人信息！");
				return jsonRet.toString();
			}
			if(!receiver.getUserId().equals(userId)) {
				jsonRet.put("errcode", ErrCodes.RECEIVER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			int cnt = this.receiverService.setDefault(receiver);
			if(cnt > 0) {
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
	 * 保存新收货人信息
	 * id为0则新增，否则修改
	 * @param userId
	 * @param receiver
	 * @param result
	 * @return {errcode:0,errmsg:"ok",recvId:111}
	 */
	@RequestMapping("/save")
	public Object save(@Valid Receiver receiver,BindingResult result,
			@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			if("1".equals(receiver.getRecvType())) { 
				UserBasic user = this.userBasicService.get(userId);
				if(user == null || !"1".equals(user.getStatus())) {
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "系统中没有该用户！");
					return jsonRet.toString();
				}
			}else {
				PartnerBasic partner = this.partnerBasicService.getByID(userId);
				if(partner == null || (!"S".equals(partner.getStatus()) && !"C".equals(partner.getStatus())) ) {
					jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
					jsonRet.put("errmsg", "系统中没有该合作伙伴！");
					return jsonRet.toString();
				}
			}
			if(!receiver.getUserId().equals(userId)) {
				jsonRet.put("errmsg", "用户信息不一致！");
				jsonRet.put("errcode", ErrCodes.RECEIVER_PARAM_ERROR);
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
				jsonRet.put("errcode", ErrCodes.RECEIVER_PARAM_ERROR);
				return jsonRet.toString();
			}
			//数据处理
			Long id = receiver.getRecvId();
			if(id == 0) {
				id = this.receiverService.add(receiver);
			}else {
				Receiver old = this.receiverService.getById(id);
				if(old == null) {
					jsonRet.put("errcode", ErrCodes.RECEIVER_NO_EXISTS);
					jsonRet.put("errmsg", "系统中没有该收货人信息！");
					return jsonRet.toString();
				}
				if(!old.getUserId().equals(userId)) {
					jsonRet.put("errcode", ErrCodes.RECEIVER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您无权执行该操作！");
					return jsonRet.toString();
				}
				id = (long)this.receiverService.update(receiver);
			}
			if(id  > 0) {
				jsonRet.put("recvId", id);
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
	 * 删除指定ID收货人信息
	 * @param recvId
	 * @param userId
	 * @return {errcode:0,errmsg:"ok"}
	 */
	@RequestMapping("/delete/{recvId}")
	public Object delete(@PathVariable("recvId")Long recvId,@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Receiver receiver = this.receiverService.getById(recvId);
			if(receiver == null) {
				jsonRet.put("errcode", ErrCodes.RECEIVER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该收货人信息！");
				return jsonRet.toString();
			}
			if(!receiver.getUserId().equals(userId)) {
				jsonRet.put("errcode", ErrCodes.RECEIVER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
				return jsonRet.toString();
			}
			int cnt = this.receiverService.delete(receiver);
			if(cnt > 0) {
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

}
