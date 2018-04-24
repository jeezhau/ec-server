package com.mofangyouxuan.mapper;

import java.util.List;

import com.mofangyouxuan.model.City;

public interface CityMapper {
    int deleteByPrimaryKey(String cityCode);

    int insert(City record);

    City selectByPrimaryKey(String cityCode);

    City selectByCodeName(String codeName);
    
    int updateByPrimaryKey(City record);
    
    List<City> selectByProvCode(String provCode);
    
}