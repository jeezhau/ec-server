package com.mofangyouxuan.mapper;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.VipBasic;

public interface VipBasicMapper {
    int deleteByPrimaryKey(Integer vipId);

    int insert(VipBasic record);

    int insertSelective(VipBasic record);

    VipBasic selectByPrimaryKey(Integer vipId);

    int updateByPrimaryKeySelective(VipBasic record);

    int updateByPrimaryKey(VipBasic record);
    
    int updateScores(@Param("vipId")Integer vipId,@Param("subScore")Integer subScore);
}