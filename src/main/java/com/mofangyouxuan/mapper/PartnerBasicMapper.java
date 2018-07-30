package com.mofangyouxuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PartnerBasic;


public interface PartnerBasicMapper {
	
    int deleteByPrimaryKey(Integer id);

    int insert(PartnerBasic record);

    PartnerBasic selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(PartnerBasic record);
    
    int updateByPrimaryKeySelective(PartnerBasic record);
    
    int updateStatusOwn(PartnerBasic record);
    
    int firstReview(PartnerBasic record);
    
    int lastReview(PartnerBasic record);
    
    PartnerBasic selectByBindUser(Integer userId);
    
    int updateScore(@Param("partnerId")Integer partnerId,
    		@Param("scoreLogis")Integer scoreLogis,@Param("scoreServ")Integer scoreServ,@Param("scoreGoods")Integer scoreGoods);
    
    List<PartnerBasic> selectAll(@Param("params")Map<String,Object> params,@Param("sorts")String sorts,@Param("pageCond")PageCond pageCond);

    int countAll(@Param("params")Map<String,Object> params);
    
}