package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Aftersale;

public interface AftersaleMapper {

	int deleteByPrimaryKey(String orderId);

    int insert(Aftersale record);

    int insertSelective(Aftersale record);

    Aftersale selectByPrimaryKey(String orderId);

    int updateByPrimaryKeySelective(Aftersale record);

    int updateByPrimaryKey(Aftersale record);
    
    int countAll(@Param("params")Map<String,Object> params);
    
    List<Aftersale> selectAll(@Param("params")Map<String,Object> params,@Param("pageCond")PageCond pageCond);
    
}