package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.CashApplyMapper;
import com.mofangyouxuan.model.CashApply;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.CashApplyService;
import com.mofangyouxuan.service.ChangeFlowService;

@Service
@Transactional
public class CashApplyServiceImpl implements CashApplyService{

	@Autowired
	private CashApplyMapper cashApplyMapper;
	@Autowired
	private ChangeFlowService changeFlowService;
	
	BigDecimal cashFeeRate = new BigDecimal(0.02); //提现费率2%，手续费不足1元按照1元计算
	
	/**
	 * 添加申请记录
	 * 1、每三天可提现一次
	 * 2、已有待处理申请不可再次提交申请；
	 * @param apply
	 * @return
	 */
	public JSONObject add(VipBasic vip,CashApply apply)	{
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		Long fee = this.cashFeeRate.multiply(new BigDecimal(apply.getCashAmount())).longValue();
		if(fee<100) {
			fee = 100l;
		}
		//可提现检查
		if((apply.getCashAmount() + fee)> vip.getBalance()) {
			jsonRet.put("errcode", ErrCodes.USER_PARAM_ERROR);
			jsonRet.put("errmsg", "提现金额：超过会员最大可用余额！");
			return jsonRet;
		}
		Date currDate = new Date();
		params.put("vipId", apply.getVipId());
		params.put("status", "0");
		int hasCnt1 = this.cashApplyMapper.countAll(params);
		if(hasCnt1 > 0) {
			jsonRet.put("errcode", ErrCodes.CASH_APPLY_HAS_REC);
			jsonRet.put("errmsg", "您还有未处理的提现申请！");
			return jsonRet;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(currDate);
		cal.add(Calendar.DAY_OF_MONTH, -3);
		params.put("beginApplyTime", new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
		params.put("status", "0,1,S");
		int hasCnt2 = this.cashApplyMapper.countAll(params);
		if(hasCnt2 > 0) {
			jsonRet.put("errcode", ErrCodes.CASH_APPLY_HAS_REC);
			jsonRet.put("errmsg", "您最近已有提现申请记录！");
			return jsonRet;
		}
		
		String applyId = this.genApplyId(apply.getVipId());
		apply.setStatus("0");
		apply.setCashFee(fee);
		apply.setApplyId(applyId);
		apply.setApplyTime(currDate);
		int cnt = this.cashApplyMapper.insert(apply);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 更新申请记录状态
	 *  
	 * @param vip
	 * @param old
	 * @param stat
	 * @param updateOpr
	 * @param memo
	 * @return
	 */
	public JSONObject updateStat(VipBasic vip,CashApply old,String stat,Integer updateOpr,String memo) {
		JSONObject jsonRet = new JSONObject();
		Long amount = old.getCashAmount() + old.getCashFee();
		if("1".equals(stat)) {
			//可提现检查
			if((old.getCashAmount() + old.getCashFee())> vip.getBalance()) {
				jsonRet.put("errcode", ErrCodes.CASH_APPLY_NO_FULL_BAL);
				jsonRet.put("errmsg", "提现金额：超过会员最大可用余额！");
				return jsonRet;
			}
			if(!"0".equals(old.getStatus())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "该提现申请当前不可受理！");
				return jsonRet;
			}
			//添加提现申请记录
			String ret = this.changeFlowService.cashApply(amount, old.getVipId(), updateOpr, "提现申请");
			if(!"00".equals(ret)) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库数据保存错误！");
				return jsonRet;
			}
		}else if("S".equals(stat)) {
			if(!"0".equals(old.getStatus()) && !"1".equals(old.getStatus())) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "该提现申请当前不可处理为提现成功 ！");
				return jsonRet;
			}
			if("0".equals(old.getStatus())) {//待受理
				//可提现检查
				if((old.getCashAmount() + old.getCashFee())> vip.getBalance()) {
					jsonRet.put("errcode", ErrCodes.CASH_APPLY_NO_FULL_BAL);
					jsonRet.put("errmsg", "提现金额：超过会员最大可用余额！");
					return jsonRet;
				}
				//添加提现申请记录
				String ret = this.changeFlowService.cashApply(amount, old.getVipId(), updateOpr, "提现申请");
				if(!"00".equals(ret)) {
					jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
					jsonRet.put("errmsg", "数据库数据保存错误！");
					return jsonRet;
				}
			}
			String ret = this.changeFlowService.cashFinish(true, amount, old.getVipId(), updateOpr, "提现成功");
			if(!"00".equals(ret)) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库数据保存错误！");
				return jsonRet;
			}
		}else if("F".equals(stat)) {
			if("1".equals(old.getStatus())) { //已受理
				String ret = this.changeFlowService.cashFinish(false, amount, old.getVipId(), updateOpr, "提现失败");
				if(!"00".equals(ret)) {
					jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
					jsonRet.put("errmsg", "数据库数据保存错误！");
					return jsonRet;
				}
			}
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
			jsonRet.put("errmsg", "处理结果状态：取值不正确！");
			return jsonRet;
		}
		CashApply updApply = new CashApply();
		updApply.setApplyId(old.getApplyId());
		updApply.setStatus(stat);
		updApply.setUpdateOpr(updateOpr);
		updApply.setUpdateTime(new Date());
		updApply.setMemo(memo);
		int cnt = this.cashApplyMapper.updateStat(updApply);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}
	
	
	/**
	 * 删除申请记录
	 * @param apply
	 * @return
	 */
	public JSONObject delete(CashApply apply) {
		JSONObject jsonRet = new JSONObject();
		int cnt = this.cashApplyMapper.deleteByPrimaryKey(apply.getApplyId());
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 根据ID获取记录信息
	 * @param applyId
	 * @return
	 */
	public CashApply get(String applyId) {
		return this.cashApplyMapper.selectByPrimaryKey(applyId);
	}
	
	/**
	 * 根据条件查询申请记录信息
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<CashApply> getAll(Map<String,Object>params,String sorts,PageCond pageCond){
		if(pageCond == null) {
			pageCond = new PageCond(0,30);
		}
		return this.cashApplyMapper.selectAll(params, sorts, pageCond);
	}
	
	/**
	 * 统计申请记录信息
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object>params) {
		return this.cashApplyMapper.countAll(params);
	}
	
	/**
	 * 生成25位的ID：14位时间+11位用户ID
	 * 
	 * @param userVipId	转11位
	 * @return
	 */
	private String genApplyId(Integer userVipId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");//14位时间
		String currTime = sdf.format(new Date());
		String uId = userVipId + "";
		int len = uId.length();
		for(int i=0;i<11-len;i++) {
			uId = "0" + uId;
		}
		return currTime + uId ;
	}
}
