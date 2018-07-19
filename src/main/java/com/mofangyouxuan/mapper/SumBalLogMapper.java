package com.mofangyouxuan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.SumBalLog;

public interface SumBalLogMapper {
    int insert(SumBalLog record);

    int updateAmount(SumBalLog record);
    
    SumBalLog selectByVipAndTime(@Param("vipId")Integer vipId,@Param("flowTime")String flowTime);
    
    List<SumBalLog> sumByVIPMonth(@Param("vipId")Integer vipId,@Param("month")String month);
    
    List<SumBalLog> sumByVIPYear(@Param("vipId")Integer vipId,@Param("year")String year);
    
    int deleteFlows(@Param("vipId")Integer vipId,@Param("flowTime")String flowTime);
    
    SumBalLog sumAllByVIP(Integer vipId);
    
}