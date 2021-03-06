package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Goods;

public interface GoodsMapper {
	
    int deleteByPrimaryKey(Long goodsId);

    int insert(Goods record);

    Goods selectByPrimaryKey(@Param("needPartner")Boolean needPartner,@Param("goodsId")Long goodsId,@Param("isSelf")Boolean isSelf);
    
    int updateByPrimaryKey(Goods record);
    
    List<Goods> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);
        
    int countAll(@Param("params")Map<String,Object> params);
    
    int countUsePostageCnt(Long postageId);
    

    
}