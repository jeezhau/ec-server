package com.mofangyouxuan.mapper;

import java.util.List;

import com.mofangyouxuan.model.Collection;

public interface CollectionMapper {
    int insert(Collection record);

    int delete(Collection record);
    
    List<Collection> selectUsersAll(Integer userId);
    
    int countUsersAll(Integer userId);
}