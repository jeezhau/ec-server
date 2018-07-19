package com.mofangyouxuan.service;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
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
	 * @throws Exception 
	 */
	public VipBasic get(Integer id) throws Exception;
	
	/**
	 * 更新资金密码
	 * @param vipId
	 * @param passwd
	 * @return
	 */
	public int updPwd(Integer vipId,String passwd);
	
	/**
	 * 更新会员积分
	 * @param vipId
	 * @param subScore 需要增加的积分
	 * @return
	 * @throws Exception 
	 */
	public int updScore(Integer vipId,Integer subScore) throws Exception;
	
	
	public VipBasic getVipBal(Integer vipId) throws Exception;
	
	/**
	 * 更新会员开通与余额信息
	 * 1、检查会员开通状态；
	 * 2、按日累积未统计的流水
	 * @param date 统计的日期
	 * @param id	   会员ID，可为空
	 * @return
	 * @throws Exception 
	 */
	public JSONObject sumDetailFlowByDay(Date date,Integer vipId) throws Exception;
	
	/**
	 * 按月统计资金流水
	 * 1、从统计表中整理月份统计，同时清除日统计记录；
	 * 2、
	 * @param isMohtn 	是否月份统计，否则按年统计
	 * @param time	月份:yyyyMM，年:yyyy
	 * @param vipId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject sumFlowByTime(boolean isMonth,String time,Integer vipId) throws Exception ;
	
}
