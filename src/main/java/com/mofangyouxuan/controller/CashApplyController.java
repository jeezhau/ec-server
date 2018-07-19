package com.mofangyouxuan.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.CashApply;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.CashApplyService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.SignUtils;

@RestController
@RequestMapping("/cash")
public class CashApplyController {
	
	@Autowired
	private CashApplyService cashApplyService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	@Autowired
	private SysParamUtil sysParamUtil;
	
	/**
	 * 提交提现申请
	 * @param apply
	 * @param result
	 * @param vipId
	 * @param passwd
	 * @return
	 */
	@RequestMapping("/{vipId}/apply")
	public Object applyCash(@Valid CashApply apply,BindingResult result,
			@PathVariable("vipId")Integer vipId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.getVipBal(vipId);
			if(vipBasic == null || !"1".equals(vipBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息或未激活！");
				return jsonRet.toString();
			}
			if(!apply.getVipId().equals(vipId)) {
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
			//密码验证
			passwd = passwd.trim();
			String pwdSign = SignUtils.encodeSHA256Hex(passwd);
			if(!pwdSign.equals(vipBasic.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员操作密码不正确！");
				return jsonRet.toString();
			}
			//数据保存
			apply.setStatus("1"); //可用
			apply.setApplyOpr(vipId);
			jsonRet = this.cashApplyService.add(vipBasic, apply);
		}catch(Exception e) {
			//数据处理
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 删除提现申请信息
	 * 1、仅可删除未受理的申请记录；
	 * 【权限】
	 * 会员自己
	 * @param settleId
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{vipId}/delete")
	public Object delete(@PathVariable("vipId")Integer vipId,
			@RequestParam(value="applyId",required=true)String applyId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic vipBasic = this.vipBasicService.get(vipId);
			if(vipBasic == null || !"1".equals(vipBasic.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员信息或未激活！");
				return jsonRet.toString();
			}
			//密码验证
			passwd = passwd.trim();
			String pwdSign = SignUtils.encodeSHA256Hex(passwd);
			if(!pwdSign.equals(vipBasic.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
				jsonRet.put("errmsg", "会员操作密码不正确！");
				return jsonRet.toString();
			}
			//数据处理
			CashApply old = this.cashApplyService.get(applyId);
			if(old == null || !old.getVipId().equals(vipId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权执行该操作！");
			}else {
				if("0".equals(old.getStatus())) {
					jsonRet = this.cashApplyService.delete(old);
				}else {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您当前不可删除该条记录！");
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 业务管理人员更新提现申请结果
	 * 【权限人】
	 * 业务管理人员 
	 * @param vipId
	 * @param applyId
	 * @param stat
	 * @param memo
	 * @param operator
	 * @param passwd
	 * @return
	 */
	@RequestMapping("/updateStat")
	public Object updateStat(@RequestParam(value="vipId",required=true)Integer vipId,
			@RequestParam(value="applyId",required=true)String applyId,
			@RequestParam(value="stat",required=true)String stat,
			@RequestParam(value="memo",required=true)String memo,
			@RequestParam(value="operator",required=true)Integer operator,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			if(!"1".equals(stat) && !"S".equals(stat) && !"F".equals(stat)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "处理结果：取值【1-已受理，S-成功，F-失败】！");
				return jsonRet.toString();
			}
			if(memo.length()>1000) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "处理备注：长度不可超过1000字符！");
				return jsonRet.toString();
			}
			//操作员与密码验证
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			PartnerBasic sysPartner = this.partnerBasicService.getByID(this.sysParamUtil.getSysPartnerId());
			if(sysPartner == null) {
				jsonRet.put("errcode", ErrCodes.PARTNER_STATUS_ERROR);
				jsonRet.put("errmsg", "获取合作伙伴信息失败！");
				return jsonRet.toString();
			}
			if(operator.equals(sysPartner.getVipId())) { //绑定会员
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
				PartnerStaff staff = this.partnerStaffService.get(this.sysParamUtil.getSysPartnerId(), operator);
				if(staff != null && staff.getTagList() != null && 
						staff.getTagList().contains(PartnerStaff.TAG.ComplainDeal.getValue()) && signPwd.equals(staff.getPasswd())) { //员工密码验证
					isPass = true;
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			//数据处理
			VipBasic vipBasic = this.vipBasicService.getVipBal(vipId);
			CashApply old = this.cashApplyService.get(applyId);
			if(vipBasic == null || old == null || !old.getVipId().equals(vipId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "参数不正确！");
			}else {
				jsonRet = this.cashApplyService.updateStat(vipBasic, old, stat, operator, memo);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 获取会员的所有提现充值信息
	 * 
	 * @param jsonSearchParams	查询条件：{applyId,vipId,cashTyp,accountType,channelType}
	 * @param jsonPageCond	分页条件：{begin,pageSize}
	 * @return
	 */
	@RequestMapping("/getall")
	public Object getAll(String jsonSearchParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			String sorts = " order by apply_time desc ";
			Map<String,Object> params = this.getSearchMap(jsonSearchParams);
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
			int cnt = this.cashApplyService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "没有获取到满足条件的记录信息！");
			if(cnt>0) {
				List<CashApply> list = this.cashApplyService.getAll(params, sorts, pageCond);
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
		if(jsonSearch.containsKey("applyId")) {
			params.put("applyId", jsonSearch.getString("recId"));
		}
		if(jsonSearch.containsKey("userId")) {
			params.put("vipId", jsonSearch.getInteger("vipId"));
		}
		if(jsonSearch.containsKey("cashType")) {
			params.put("cashType", jsonSearch.getString("cashType"));
		}
		if(jsonSearch.containsKey("accountType")) {
			params.put("accountType", jsonSearch.getString("accountType"));
		}
		if(jsonSearch.containsKey("idNo")) {
			params.put("idNo", jsonSearch.getString("idNo"));
		}
		if(jsonSearch.containsKey("channelType")) {
			params.put("channelType", jsonSearch.getString("channelType"));
		}
		if(jsonSearch.containsKey("accountName")) {
			params.put("accountName", jsonSearch.getString("accountName"));
		}
		if(jsonSearch.containsKey("accountNo")) {
			params.put("accountNo", jsonSearch.getString("accountNo"));
		}
		if(jsonSearch.containsKey("accountBank")) {
			params.put("accountBank", jsonSearch.getString("accountBank"));
		}
		if(jsonSearch.containsKey("applyOpr")) {
			params.put("applyOpr", jsonSearch.getString("applyOpr"));
		}
		if(jsonSearch.containsKey("status")) {
			params.put("status", jsonSearch.getString("status"));
		}
		if(jsonSearch.containsKey("beginApplyTime")) {
			params.put("beginApplyTime", jsonSearch.getString("beginApplyTime"));
		}
		if(jsonSearch.containsKey("endApplyTime")) {
			params.put("endApplyTime", jsonSearch.getString("endApplyTime"));
		}
		if(jsonSearch.containsKey("updateOpr")) {
			params.put("updateOpr", jsonSearch.getString("updateOpr"));
		}
		return params;
	}

}
