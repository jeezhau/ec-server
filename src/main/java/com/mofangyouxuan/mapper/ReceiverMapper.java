package com.mofangyouxuan.mapper;

import java.util.List;

import com.mofangyouxuan.model.Receiver;

public interface ReceiverMapper {
	
    int deleteByPrimaryKey(Long recvId);

    int insert(Receiver record);

    int insertSelective(Receiver record);

    Receiver selectByPrimaryKey(Long recvId);

    int updateByPrimaryKeySelective(Receiver record);

    int updateByPrimaryKey(Receiver record);
    
    int setDefault(Long recvId);
    
    int unDefault(Integer userId);
    
    List<Receiver> selectDefault(Integer userId);
    
    List<Receiver> selectAllByUser(Integer userId);
    
    int countAllByUser(Integer userId);
}
