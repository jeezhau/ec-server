package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Postage;

public interface PostageMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Postage record);

    Postage selectByPrimaryKey(Long id);

    int updateByPrimaryKey(Postage record);
    
    List<Postage> selectAll(@Param("params")Map<String,Object> params,@Param("pageCond")PageCond pageCond);
    
    int countAll(Map<String,Object> params);
}