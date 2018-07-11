package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.OrderBal;

public interface OrderBalMapper {

	int deleteByPrimaryKey(String orderId);

    int insert(OrderBal record);

    int insertSelective(OrderBal record);

    OrderBal selectByPrimaryKey(String orderId);

    int updateByPrimaryKeySelective(OrderBal record);

    int updateByPrimaryKey(OrderBal record);
}