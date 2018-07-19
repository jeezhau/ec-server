package com.mofangyouxuan.service;

import com.mofangyouxuan.model.UserBasic;

public interface UserBasicService {
	
	/**
	 * 添加新用户
	 * @param userBasic
	 * @return 新用户ID
	 * @throws Exception 
	 */
	public Integer add(UserBasic userBasic) throws Exception;
	
	/**
	 * 更新用户信息
	 * @param userBasic
	 * @return 更新记录数
	 */
	public int update(UserBasic userBasic);
	
	/**
	 * 根据ID获取用户
	 * @param id
	 * @return
	 */
	public UserBasic get(Integer id);
	
	/**
	 * 根据微信OPENID获取用户信息
	 * @param openId 公众号下的OPENID或UNIONID
	 * @return
	 */
	public UserBasic get(String openId);
	
	/**
	 * 统计指定用户的已推广用户数
	 * @param userId
	 * @return
	 */
	public int countSpreadUsers(Integer userId);
	
	/**
	 * 更新登录密码
	 * @param userId
	 * @param passwd
	 * @return
	 */
	public int updPwd(Integer userId,String passwd);
	
	/**
	 * 更新手机号
	 * @param userId
	 * @param phone
	 * @return
	 */
	public int updPhone(Integer userId,String phone);
	
	/**
	 * 更新邮箱
	 * @param userId
	 * @param email
	 * @return
	 */
	public int updEmail(Integer userId,String email);

}
