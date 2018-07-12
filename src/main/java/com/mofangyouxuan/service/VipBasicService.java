package com.mofangyouxuan.service;

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
	 */
	public int updScore(Integer vipId,Integer subScore);
	
}
