package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.OperateLog;

public interface OperateLogMapper {
    int insert(OperateLog record);

    int insertSelective(OperateLog record);
}