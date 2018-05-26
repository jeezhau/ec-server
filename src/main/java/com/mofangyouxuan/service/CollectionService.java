package com.mofangyouxuan.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.Collection;

public interface CollectionService {
	
	/**
	 * 添加用户的收藏
	 * @param collection
	 * @return
	 */
	public JSONObject add(Collection collection);
	
	/**
	 * 删除用户的收藏
	 * @param collection
	 * @return
	 */
	public int delete(Collection collection);
	
	/**
	 * 获取用户的所有收藏
	 * @param userId
	 * @return
	 */
	public List<Collection> getUsersAll(Integer userId);
	
	/**
	 * 统计用户的所有收藏
	 * @param userId
	 * @return
	 */
	public int countUsersAll(Integer userId);

}
