package com.mofangyouxuan.mapper;

import com.mofangyouxuan.model.QrCodeSence;

public interface QrCodeSenceMapper {
	
    int deleteByPrimaryKey(Integer userId);

    int insert(QrCodeSence record);

    QrCodeSence selectByPrimaryKey(Integer userId);

    int updateByPrimaryKey(QrCodeSence record);
    
}