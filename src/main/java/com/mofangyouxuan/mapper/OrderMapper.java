package com.mofangyouxuan.mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Order;

public interface OrderMapper {

	int deleteByPrimaryKey(BigInteger orderId);

    int insert(Order record);

    Order selectByPrimaryKey(BigInteger orderId);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
    
    int countAll(@Param("params")Map<String,Object> params);
    
    List<Order> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);
    
}