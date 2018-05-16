package com.mofangyouxuan.mapper;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.VipBasic;

public interface VipBasicMapper {
	
    int deleteByPrimaryKey(Integer vipId);

    int insert(VipBasic record);

    VipBasic selectByPrimaryKey(Integer vipId);

    int updateByPrimaryKey(VipBasic record);
    
    int sumAccount(Integer vipId);
    
    int updPasswd(@Param("vipdId")Integer vipId,@Param("passwd")String passwd);
    
    int updAccount(@Param("vipdId")Integer vipId,@Param("accountName")String accountName,
    		@Param("accountNo")String accountNo,@Param("accountBank")String accountBank);
    
}