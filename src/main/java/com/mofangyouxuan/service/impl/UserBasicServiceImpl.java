package com.mofangyouxuan.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.mapper.UserBasicMapper;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;

@Service
@Transactional
public class UserBasicServiceImpl implements UserBasicService{
	
	@Autowired
	private UserBasicMapper userBasicMapper;
	@Autowired
	private VipBasicService vipBasicService;
	@Value("${sys.spread-per-user-score}")
	private Integer spreadPerUserScore;
	
	/**
	 * 添加新用户
	 * @param userBasic
	 * @return 新用户ID或null
	 */
	@Override
	public Integer add(UserBasic userBasic) {
		userBasic.setUserId(null);
		userBasic.setRegistTime(new Date());
		userBasic.setUpdateTime(new Date());
		this.userBasicMapper.insert(userBasic);
		Integer id = userBasic.getUserId();
		if(id != null) {//初始化会员信息
			VipBasic vipBasic = new VipBasic();
			vipBasic.setVipId(id);
			vipBasicService.add(vipBasic);
		}
		if(userBasic.getSenceId() != null) { //有介绍人员
			VipBasic vip = this.vipBasicService.get(userBasic.getSenceId());
			if(vip != null) {
				this.vipBasicService.updScore(vip, spreadPerUserScore);
			}
		}
		return id;
	}
	
	/**
	 * 更新用户信息
	 * @param userBasic
	 * @return 更新记录数
	 */
	@Override
	public int update(UserBasic userBasic) {
		userBasic.setUpdateTime(new Date());
		int cnt = this.userBasicMapper.updateByPrimaryKey(userBasic);
		return cnt;
	}
	
	/**
	 * 根据ID获取用户
	 * @param id
	 * @return
	 */
	@Override
	public UserBasic get(Integer id) {
		return this.userBasicMapper.selectByPrimaryKey(id);
	}
	
	/**
	 * 根据微信OPENID获取用户信息
	 * @param openId 公众号下的OPENID或UNIONID
	 * @return
	 */
	@Override
	public UserBasic get(String openId) {
		return this.userBasicMapper.selectByOpenId(openId);
	}
	
	/**
	 * 统计指定用户的已推广用户数
	 * @param userId
	 * @return
	 */
	public int countSpreadUsers(Integer userId) {
		return this.userBasicMapper.countSpreadUsers(userId);
	}

}
