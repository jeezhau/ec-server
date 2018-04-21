package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.PartnerBasic;

public interface PartnerBasicMapper {
	
    int deleteByPrimaryKey(Integer id);

    int insert(PartnerBasic record);

    PartnerBasic selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(PartnerBasic record);
    
    PartnerBasic selectByBindUser(Integer userId);
    
}