package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Goods;

public interface GoodsMapper {
	
    int deleteByPrimaryKey(Long id);

    int insert(Goods record);

    Goods selectByPrimaryKey(Long id);

    int updateByPrimaryKey(Goods record);
    
    List<Goods> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);
    
    int countAll(Map<String,Object> params);
    
}