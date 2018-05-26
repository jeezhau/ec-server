package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.mapper.CollectionMapper;
import com.mofangyouxuan.model.Collection;
import com.mofangyouxuan.service.CollectionService;

@Service
@Transactional
public class CollectionServiceImpl implements CollectionService{

	@Autowired
	private CollectionMapper collectionMapper;
	@Value("${sys.user-collection-limit}")
	private int collectionLimit;
	
	/**
	 * 添加用户的收藏
	 * @param collection
	 * @return
	 */
	public JSONObject add(Collection collection) {
		JSONObject jsonRet = new JSONObject();
		int hasCnt = this.collectionMapper.countUsersAll(collection.getUserId());
		if(hasCnt >= this.collectionLimit) {
			jsonRet.put("errcode", ErrCodes.COLLECTION_OVER_LIMIT);
			jsonRet.put("errmsg", "您已经拥有达到最大数量限制的收藏！");
		}
		collection.setCreateTime(new Date());
		this.collectionMapper.delete(collection);
		int cnt = this.collectionMapper.insert(collection);
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
	 * 删除用户的收藏
	 * @param collection
	 * @return
	 */
	@Override
	public int delete(Collection collection) {
		return this.collectionMapper.delete(collection);
	}
	
	/**
	 * 获取用户的所有收藏
	 * @param userId
	 * @return
	 */
	public List<Collection> getUsersAll(Integer userId){
		return this.collectionMapper.selectUsersAll(userId);
	}
	
	/**
	 * 统计用户的所有收藏
	 * @param userId
	 * @return
	 */
	public int countUsersAll(Integer userId) {
		return this.collectionMapper.countUsersAll(userId);
	}
	
}
