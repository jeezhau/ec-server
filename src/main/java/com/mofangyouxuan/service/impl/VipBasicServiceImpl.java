package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mofangyouxuan.mapper.VipBasicMapper;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.VipBasicService;

@Service
public class VipBasicServiceImpl implements VipBasicService{
	
	@Autowired
	private VipBasicMapper vipBasicMapper;
	
	/**
	 * 添加新会员
	 * @param vipBasic
	 * @return 新用户ID或null
	 */
	@Override
	public Integer add(VipBasic vipBasic) {
		vipBasic.setCreateTime(new Date());
		vipBasic.setBalance(new BigDecimal("0"));
		vipBasic.setFreeze(new BigDecimal("0"));
		vipBasic.setScores(0);
		vipBasic.setStatus("0");//未开通
		int cnt = this.vipBasicMapper.insert(vipBasic);
		if(cnt>0) {
			return vipBasic.getVipId();
		}
		return null;
	}
	
	/**
	 * 更新会员信息
	 * @param vipBasic
	 * @return 更新记录数
	 */
	@Override
	public int update(VipBasic vipBasic) {
		int cnt = this.vipBasicMapper.updateByPrimaryKey(vipBasic);
		return cnt;
	}
	
	/**
	 * 根据ID获取会员
	 * @param id
	 * @return
	 */
	@Override
	public VipBasic get(Integer id) {
		return this.vipBasicMapper.selectByPrimaryKey(id);
	}
	

}
