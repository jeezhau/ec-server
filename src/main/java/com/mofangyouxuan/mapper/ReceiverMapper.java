package com.mofangyouxuan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.model.Receiver;

public interface ReceiverMapper {
	
    int deleteByPrimaryKey(Long recvId);

    int insert(Receiver record);

    int insertSelective(Receiver record);

    Receiver selectByPrimaryKey(Long recvId);

    int updateByPrimaryKeySelective(Receiver record);

    int updateByPrimaryKey(Receiver record);
    
    int setDefault(Long recvId);
    
    int unDefault(@Param("userId")Integer userId,@Param("recvType")String recvType);
    
    List<Receiver> selectDefault(@Param("userId")Integer userId,@Param("recvType")String recvType);
    
    List<Receiver> selectAllByUser(@Param("userId")Integer userId,@Param("recvType")String recvType);
    
    int countAllByUser(@Param("userId")Integer userId,@Param("recvType")String recvType);
    
}
