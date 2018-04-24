package com.mofangyouxuan.mapper;

import java.util.List;

import com.mofangyouxuan.model.Province;

public interface ProvinceMapper {
    int deleteByPrimaryKey(String provCode);

    int insert(Province record);

    Province selectByPrimaryKey(String provCode);
    
    Province selectByCodeName(String codeName);

    int updateByPrimaryKey(Province record);
    
    List<Province> selectAll();
    
}