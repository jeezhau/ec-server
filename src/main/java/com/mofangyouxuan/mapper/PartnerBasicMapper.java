package com.mofangyouxuan.mapper;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.PartnerBasic;


public interface PartnerBasicMapper {
	
    int deleteByPrimaryKey(Integer id);

    int insert(PartnerBasic record);

    PartnerBasic selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(PartnerBasic record);
    
    PartnerBasic selectByBindUser(Integer userId);
    
    int updateScore(@Param("partnerId")Integer partnerId,
    		@Param("scoreLogis")Integer scoreLogis,@Param("scoreServ")Integer scoreServ,@Param("scoreGoods")Integer scoreGoods);
    
}