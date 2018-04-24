package com.mofangyouxuan.mapper;

import java.util.List;

import com.mofangyouxuan.model.Area;

public interface AreaMapper {
    int deleteByPrimaryKey(String areaCode);

    int insert(Area record);

    Area selectByPrimaryKey(String areaCode);
    
    Area selectByCodeName(String codeName);

    int updateByPrimaryKey(Area record);
    
    List<Area> selectByCityCode(String cityCode);
    
    
    
}