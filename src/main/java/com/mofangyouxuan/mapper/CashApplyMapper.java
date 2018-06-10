package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.CashApply;

public interface CashApplyMapper {
    int deleteByPrimaryKey(String applyId);

    int insert(CashApply record);

    CashApply selectByPrimaryKey(String applyId);

    int updateByPrimaryKeySelective(CashApply record);

    int updateByPrimaryKey(CashApply record);
    
    List<CashApply> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);
    
    int countAll(@Param("params")Map<String,Object> params);
    
}