package com.mofangyouxuan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.Postage;


public interface PostageMapper {
    int deleteByPrimaryKey(Long postageId);

    int insert(Postage record);

    Postage selectByPrimaryKey(Long postageId);

    int updateByPrimaryKey(Postage record);
    
    List<Postage> selectByPartner(Integer partnerId);
    
    Postage selectByPartnerAndName(@Param("partnerId")Integer partnerId,@Param("postageName")String postageName);
    
}
