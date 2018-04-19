package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.VipBasic;

public interface VipBasicMapper {
	
    int deleteByPrimaryKey(Integer vipId);

    int insert(VipBasic record);

    VipBasic selectByPrimaryKey(Integer vipId);

    int updateByPrimaryKey(VipBasic record);
    
}