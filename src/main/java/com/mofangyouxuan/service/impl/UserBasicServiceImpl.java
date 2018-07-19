package com.mofangyouxuan.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.common.SysParamUtil;
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
	
	@Autowired
	private SysParamUtil sysParamUtil;
	
	/**
	 * 添加新用户
	 * @param userBasic
	 * @return 新用户ID或null
	 * @throws Exception 
	 */
	@Override
	public Integer add(UserBasic userBasic) throws Exception {
		userBasic.setUserId(null);
		userBasic.setRegistTime(new Date());
		userBasic.setUpdateTime(new Date());
		this.userBasicMapper.insert(userBasic);
		Integer id = userBasic.getUserId();
		if(id != null) {//初始化会员信息
			VipBasic vipBasic = new VipBasic();
			vipBasic.setVipId(id);
			vipBasic.setUpdateTime(new Date());
			vipBasicService.add(vipBasic);
		}
		if(userBasic.getSenceId() != null) { //有介绍人员，积分处理
			Integer spread_per_user_score = this.spreadPerUserScore;
			try {
				if(this.sysParamUtil.getSysParam("spread_per_user_score") != null) {
					spread_per_user_score = new Integer(this.sysParamUtil.getSysParam("spread_per_user_score"));
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			VipBasic vip = this.vipBasicService.get(userBasic.getSenceId());
			if(vip != null) {
				this.vipBasicService.updScore(vip.getVipId(), spread_per_user_score);
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

	/**
	 * 更新密码
	 * @param userId
	 * @param passwd
	 * @return
	 */
	@Override
	public int updPwd(Integer userId,String passwd) {
		UserBasic user = new UserBasic();
		user.setUserId(userId);
		user.setPasswd(passwd);
		int cnt = this.userBasicMapper.updateByPrimaryKey(user);
		return cnt;
	}
	
	
	/**
	 * 更新手机号
	 * @param userId
	 * @param phone
	 * @return
	 */
	public int updPhone(Integer userId,String phone) {
		UserBasic user = new UserBasic();
		user.setUserId(userId);
		user.setPhone(phone);
		int cnt = this.userBasicMapper.updateByPrimaryKey(user);
		return cnt;
	}
	
	/**
	 * 更新邮箱
	 * @param userId
	 * @param email
	 * @return
	 */
	public int updEmail(Integer userId,String email) {
		UserBasic user = new UserBasic();
		user.setUserId(userId);
		user.setEmail(email);
		int cnt = this.userBasicMapper.updateByPrimaryKey(user);
		return cnt;
	}
	
}
