package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.UserBasic;

public interface UserBasicMapper {
	
    int deleteByPrimaryKey(Integer id);

    int insert(UserBasic record);

    UserBasic selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(UserBasic record);
    
    UserBasic selectByOpenId(String openId);
    
}