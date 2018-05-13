package com.mofangyouxuan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ChangeFlow;

public interface ChangeFlowMapper {
    int deleteByPrimaryKey(String flowId);

    int insert(ChangeFlow record);

    int insertSelective(ChangeFlow record);

    ChangeFlow selectByPrimaryKey(String flowId);

    int updateByPrimaryKeySelective(ChangeFlow record);

    int updateByPrimaryKeyWithBLOBs(ChangeFlow record);

    int updateByPrimaryKey(ChangeFlow record);
    
    List<ChangeFlow> selectByVip(@Param("vipId")Integer vipId,@Param("pageCond")PageCond pageCond);
    
}