package com.mofangyouxuan.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.VipBasic;

public interface VipBasicService {
	
	/**
	 * 添加新会员
	 * @param vipBasic
	 * @return 新用户ID
	 */
	public Integer add(VipBasic vipBasic);
	
	/**
	 * 更新会员信息
	 * @param vipBasic
	 * @return 更新记录数
	 */
	public int update(VipBasic vipBasic);
	
	/**
	 * 根据ID获取会员信息
	 * @param id
	 * @return
	 */
	public VipBasic get(Integer id);
	
	/**
	 * 根据指定查询条件、分页信息获取变更流水信息
	 * @param jsonParams
	 * @param pageCond
	 * @return
	 */
	public List<ChangeFlow> getAll(JSONObject jsonParams,PageCond pageCond);
	
	/**
	 * 根据条件统计变更流水数量
	 */
	public int countAll(JSONObject jsonParams) ;
	
}
