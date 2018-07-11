package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ChangeFlow;

public interface ChangeFlowMapper {
    int deleteByPrimaryKey(String flowId);

    int insert(ChangeFlow record);

    int insertSelective(ChangeFlow record);

    ChangeFlow selectByPrimaryKey(String flowId);

    int updateSumFlag(ChangeFlow record);

    List<ChangeFlow> selectAll(@Param("params")Map<String,Object> params,
    		@Param("pageCond")PageCond pageCond,@Param("sorts")String sorts);
   
    int countAll(@Param("params")Map<String,Object> params);
    
    List<Map<String,Object>> sumAllGroupTD(@Param("params")Map<String,Object> params);
    
    int updateFlag(@Param("params")Map<String,Object> params);
    
}
