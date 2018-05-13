package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.PayFlow;

public interface PayFlowMapper {
    int deleteByPrimaryKey(String flowId);

    int insert(PayFlow record);

    int insertSelective(PayFlow record);

    PayFlow selectByPrimaryKey(String flowId);

    int updateByPrimaryKeySelective(PayFlow record);

    int updateByPrimaryKey(PayFlow record);
    
    PayFlow selectLastestFlow(String orderId,String payType);
    
}