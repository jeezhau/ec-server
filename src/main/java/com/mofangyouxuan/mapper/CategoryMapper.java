package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.Category;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(Category record);
    
    List<Category> selectAll(@Param("params")Map<String,Object> params);
    
    int countAll(@Param("params")Map<String,Object> params);
    
}