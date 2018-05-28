package com.mofangyouxuan.mapper;

import java.util.List;

import com.mofangyouxuan.model.SysParam;

public interface SysParamMapper {
    int insert(SysParam record);

    int update(SysParam record);
    
    int delete(String paramName);
    
    List<SysParam> selectByTp(String paramTp);
    
    List<SysParam> selectAll();
    
    SysParam selectByKey(String paramName);
    
}