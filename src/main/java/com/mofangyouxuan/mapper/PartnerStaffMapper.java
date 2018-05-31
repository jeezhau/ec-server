package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PartnerStaff;

public interface PartnerStaffMapper {
	
    int deleteByPrimaryKey(Long recId);

    int insert(PartnerStaff record);

    PartnerStaff selectByPrimaryKey(Long recId);
    
    PartnerStaff selectByUser(@Param("partnerId")Integer partnerId,@Param("userId")Integer userId);

    int updateByPrimaryKeySelective(PartnerStaff record);

    int updateByPrimaryKey(PartnerStaff record);
    
    List<PartnerStaff> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);

    int countAll(@Param("params")Map<String,Object> params);
    
    int addTag2Staff(@Param("partnerId")Integer partnerId,@Param("userId")Integer userId,@Param("tagId")Long tagId,@Param("updateOpr")Integer updateOpr);
    
    int removeTagFromStaff(@Param("partnerId")Integer partnerId,@Param("userId")Integer userId,@Param("tagId")Long tagId,@Param("updateOpr")Integer updateOpr);
    
    int removeTagFromAll(@Param("partnerId")Integer partnerId,@Param("tagId")Long tagId,@Param("updateOpr")Integer updateOpr);
    
}

