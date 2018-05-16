package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Order;

public interface OrderMapper {

	int deleteByPrimaryKey(String orderId);

    int insert(Order record);

    /**
     * params 需要显示哪些分类字段：needReceiver,needLogistics,needAppr,needAfterSales,needGoodsAndUser
     * @param orderId
     * @return
     */
    Order selectByPrimaryKey(@Param("params")Map<String,Object> params,@Param("orderId")String orderId);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
    
    int countAll(@Param("params")Map<String,Object> params);
    
    /**
     * 
     * @param params 包含查询条件，需要显示哪些分类字段：needReceiver,needLogistics,needAppr,needAfterSales,needGoodsAndUser
     * @param sorts
     * @param pageCond
     * @return
     */
    List<Order> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);
    
    List<Map<String,Integer>> countPartibyStatus(@Param("partnerId")Integer partnerId,
    		@Param("goodsId")Long goodsId,@Param("userId")Integer userId);
    
}