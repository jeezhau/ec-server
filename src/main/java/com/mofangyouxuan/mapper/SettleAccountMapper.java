package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.SettleAccount;

public interface SettleAccountMapper {
	
    int deleteByPrimaryKey(Long settleId);

    int insert(SettleAccount record);

    SettleAccount selectByPrimaryKey(Long settleId);

    int updateByPrimaryKeySelective(SettleAccount record);

    int updateByPrimaryKey(SettleAccount record);
    
    List<SettleAccount> selectAll(@Param("params")Map<String,Object> params);
    
    int countAll(@Param("params")Map<String,Object> params);
    
}
