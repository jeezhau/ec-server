package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.LoginLog;

public interface LoginLogMapper {
    int insert(LoginLog record);

    int insertSelective(LoginLog record);
    
    int countAll(@Param("params")Map<String,Object> params);
    
    List<LoginLog> selectAll(@Param("params")Map<String,Object> params,@Param("pageCond")PageCond pageCond);
    
}