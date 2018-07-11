package com.mofangyouxuan.mapper;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.SumBalLog;

public interface SumBalLogMapper {
    int insert(SumBalLog record);

    int updateAmount(SumBalLog record);
    
    SumBalLog selectByVipAndTime(@Param("vipId")Integer vipId,@Param("flowTime")String flowTime);
    
}