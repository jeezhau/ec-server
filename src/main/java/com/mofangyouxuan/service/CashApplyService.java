package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.CashApply;
import com.mofangyouxuan.model.VipBasic;

public interface CashApplyService {
	
	/**
	 * 添加申请记录
	 * 1、每三天可提现一次;
	 * 2、已有待处理申请不可再次提交申请；
	 * 
	 * @param apply
	 * @return
	 */
	public JSONObject add(VipBasic vip,CashApply apply);
	
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
	public JSONObject updateStat(VipBasic vip,CashApply old,String stat,Integer updateOpr,String memo) ;
	
	/**
	 * 删除申请记录
	 * @param apply
	 * @return
	 */
	public JSONObject delete(CashApply apply);
	
	/**
	 * 根据ID获取记录信息
	 * @param applyId
	 * @return
	 */
	public CashApply get(String applyId);
	
	/**
	 * 根据条件查询申请记录信息
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<CashApply> getAll(Map<String,Object>params,String sorts,PageCond pageCond);
	
	/**
	 * 统计申请记录信息
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object>params);

}
