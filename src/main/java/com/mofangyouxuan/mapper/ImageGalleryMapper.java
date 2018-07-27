package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.ImageGallery;

public interface ImageGalleryMapper {
    int deleteByPrimaryKey(@Param("imgId")String imgId,@Param("partnerId")Integer partnerId);

    int insert(ImageGallery record);

    int insertSelective(ImageGallery record);

    ImageGallery selectByPrimaryKey(@Param("imgId")String imgId,@Param("partnerId")Integer partnerId);

    int updateByPrimaryKeySelective(ImageGallery record);

    int updateByPrimaryKey(ImageGallery record);
    
    int countAll(@Param("params")Map<String,Object> params);
    
    List<ImageGallery> selectAll(@Param("params")Map<String,Object> params);
    
}