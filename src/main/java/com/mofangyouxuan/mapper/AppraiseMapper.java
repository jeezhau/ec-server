package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Appraise;

public interface AppraiseMapper {
    int deleteByPrimaryKey(Long apprId);

    int insert(Appraise record);

    int insertSelective(Appraise record);

    Appraise selectByPrimaryKey(Long apprId);

    int updateByPrimaryKeySelective(Appraise record);

    int updateByPrimaryKey(Appraise record);
    
    int countAll(@Param("params")Map<String,Object> params);
    
    List<Appraise> selectAll(@Param("params")Map<String,Object> params,@Param("pageCond")PageCond pageCond);
    
}