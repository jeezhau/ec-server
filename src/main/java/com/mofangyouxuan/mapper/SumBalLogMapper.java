package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.SumBalLog;

public interface SumBalLogMapper {
    int insert(SumBalLog record);

    int insertSelective(SumBalLog record);
}