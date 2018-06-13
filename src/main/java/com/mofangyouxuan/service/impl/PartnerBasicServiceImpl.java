package com.mofangyouxuan.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.mapper.PartnerBasicMapper;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.service.PartnerBasicService;

@Service
@Transactional
public class PartnerBasicServiceImpl implements PartnerBasicService{
	
	@Autowired
	private PartnerBasicMapper partnerBasicMapper;
	
	/**
	 * 根据ID获取合作胡伙伴基本信息
	 * @param id	
	 * @return
	 */
	@Override
	public PartnerBasic getByID(Integer id) {
		return this.partnerBasicMapper.selectByPrimaryKey(id);
	}
	
	/**
	 * 根据绑定用户获取合作伙伴
	 * @param userId
	 * @return
	 */
	@Override
	public PartnerBasic getByBindUser(Integer userId) {
		return this.partnerBasicMapper.selectByBindUser(userId);
	}
	
	
	/**
	 * 添加合作伙伴
	 * @param basic
	 * @return 新ID或小于0的错误码
	 */
	@Override
	public Integer add(PartnerBasic basic) {
		basic.setPartnerId(null);
		basic.setUpdateTime(new Date());
		int cnt = this.partnerBasicMapper.insert(basic);
		if(cnt>0) {
			return basic.getPartnerId();
		}
		return ErrCodes.COMMON_DB_ERROR;
	}
	
	/**
	 * 自己更新合作伙伴信息
	 * @param basic
	 * @return 更新记录数
	 */
	@Override
	public int update(PartnerBasic basic) {
		basic.setUpdateTime(new Date());
		return this.partnerBasicMapper.updateByPrimaryKey(basic);
	}
	
	/**
	 * 记录审批人员的审批结果
	 * @param partnerId	合作伙伴ID
	 * @param oprId	审批人员ID
	 * @param review	审批意见
	 * @param result	审批结果
	 * @return 更新记录数
	 */
	@Override
	public int review(Integer partnerId,Integer oprId,String review,String result) {
		PartnerBasic basic = new PartnerBasic();
		basic.setPartnerId(partnerId);
		basic.setReviewOpr(oprId);
		basic.setReviewLog(review);
		basic.setReviewTime(new Date());
		basic.setStatus(result);
		return this.partnerBasicMapper.updateByPrimaryKey(basic);
	}
	

	/**
	 * 自己暂时关闭店铺或打开
	 * @param partnerId	合作伙伴ID
	 * @param newStatus 新的状态：S-正常，C-关闭
	 * @return 更新记录数
	 */
	@Override
	public int changeShopStatus(Integer partnerId,String newStatus) {
		PartnerBasic basic = new PartnerBasic();
		basic.setPartnerId(partnerId);
		basic.setStatus(newStatus);
		return this.partnerBasicMapper.updateByPrimaryKey(basic);
	}

	/**
	 * 更新商家的评价得分
	 * @param scoreLogis
	 * @param scoreServ
	 * @param scoreGoods
	 */
	public void updScore(Integer partnerId,Integer scoreLogis,Integer scoreServ,Integer scoreGoods) {
		this.partnerBasicMapper.updateScore(partnerId, scoreLogis, scoreServ, scoreGoods);
	}
	
}
