package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ComplainLog;

public interface ComplainLogMapper {
	
    int deleteByPrimaryKey(Integer cplanId);

    int insert(ComplainLog record);

    ComplainLog selectByPrimaryKey(Integer cplanId);

    int updateByPrimaryKey(ComplainLog record);
    
    List<ComplainLog> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);

    int countAll(@Param("params")Map<String,Object> params);

}