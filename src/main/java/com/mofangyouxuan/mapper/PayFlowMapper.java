package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PayFlow;

public interface PayFlowMapper {
    int deleteByPrimaryKey(String flowId);

    int insert(PayFlow record);

    int insertSelective(PayFlow record);

    PayFlow selectByPrimaryKey(String flowId);

    int updateByPrimaryKeySelective(PayFlow record);

    int updateByPrimaryKey(PayFlow record);
    
    PayFlow selectLastestFlow(@Param("orderId")String orderId,@Param("flowType")String flowType);
    
    List<PayFlow> selectAll(@Param("params")Map<String,Object> params,@Param("pageCond")PageCond pageCond);
    
    int countAll(@Param("params")Map<String,Object> params);
    
}

