package com.mofangyouxuan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.GoodsSpec;

public interface GoodsSpecMapper {
	
    int insert(GoodsSpec record);

	int deleteAll(Long goodsId);

    int updateStock(@Param("goodsId")Long goodsId,@Param("name")String name,@Param("changeVal")Integer changeVal);

    List<GoodsSpec> selectAll(Long goodsId);
}