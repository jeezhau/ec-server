package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.PartnerSettle;

public interface PartnerSettleMapper {
    int deleteByPrimaryKey(Integer partnerId);

    int insert(PartnerSettle record);

    int insertSelective(PartnerSettle record);

    PartnerSettle selectByPrimaryKey(Integer partnerId);

    int updateByPrimaryKeySelective(PartnerSettle record);

    int updateByPrimaryKey(PartnerSettle record);
}