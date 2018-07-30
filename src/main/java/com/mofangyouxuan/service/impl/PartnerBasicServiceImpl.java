package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.PartnerBasicMapper;
import com.mofangyouxuan.mapper.PartnerSettleMapper;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerSettle;
import com.mofangyouxuan.service.PartnerBasicService;

@Service
@Transactional
public class PartnerBasicServiceImpl implements PartnerBasicService{
	
	@Autowired
	private PartnerBasicMapper partnerBasicMapper;
	@Autowired
	private PartnerSettleMapper partnerSettleMapper;
	
	
	
	/**
	 * 根据ID获取合作胡伙伴基本信息
	 * @param id	
	 * @return
	 */
	@Override
	public PartnerBasic getByID(Integer partnerId) {
		return this.partnerBasicMapper.selectByPrimaryKey(partnerId);
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
	 * 根据ID获取合作伙伴结算设置信息
	 * @param id	
	 * @return
	 */
	public PartnerSettle getSettle(Integer partnerId) {
		return this.partnerSettleMapper.selectByPrimaryKey(partnerId);
	}
	
	/**
	 * 添加合作伙伴
	 * @param basic
	 * @return 新ID或小于0的错误码
	 */
	@Override
	public Integer add(PartnerBasic basic,PartnerSettle settle) {
		basic.setPartnerId(null);
		basic.setUpdateTime(new Date());
		
		int cnt = this.partnerBasicMapper.insert(basic);
		if(cnt>0) {
			settle.setPartnerId(basic.getPartnerId());
			this.partnerSettleMapper.insert(settle);
			return basic.getPartnerId();
		}
		return ErrCodes.COMMON_DB_ERROR;
	}
	
	
	/**
	 * 添加合作伙伴结算信息
	 * @param settle
	 * @return 新ID或小于0的错误码
	 */
	@Override
	public Integer add(PartnerSettle settle) {
		int cnt = this.partnerSettleMapper.insert(settle);
		return cnt;
	}
	
	/**
	 * 自己更新合作伙伴所有信息
	 * @param basic
	 * @return 更新记录数
	 */
	@Override
	public int updateBasic(PartnerBasic basic,PartnerSettle settle) {
		basic.setUpdateTime(new Date());
		this.partnerBasicMapper.updateByPrimaryKey(basic);
		settle.setPartnerId(basic.getPartnerId());
		return this.partnerSettleMapper.updateByPrimaryKey(settle);
	}
	
	/**
	 * 更新合作伙伴部分信息
	 * @param basic
	 * @return 更新记录数
	 */
	public int updateSelective(PartnerBasic basic) {
		basic.setUpdateTime(new Date());
		return this.partnerBasicMapper.updateByPrimaryKeySelective(basic);
	}
	
	/**
	 * 记录初审审批人员的审批结果
	 * @param partnerId	合作伙伴ID
	 * @param reviewPidUid	审批人员：上级合作伙伴ID#员工ID
	 * @param review	审批意见
	 * @param result	审批结果
	 * @return 更新记录数
	 */
	@Override
	public int firstReview(Integer partnerId,String reviewPidUid,String review,String result) {
		PartnerBasic basic = new PartnerBasic();
		basic.setPartnerId(partnerId);
		basic.setFreviewOpr(reviewPidUid);
		basic.setFreviewLog(review);
		basic.setFreviewTime(new Date());
		basic.setStatus(result);
		return this.partnerBasicMapper.firstReview(basic);
	}
	
	/**
	 * 记录终审审批人员的审批结果
	 * @param partnerId	合作伙伴ID
	 * @param reviewPidUid	审批人员：上级合作伙伴ID#员工ID
	 * @param review	审批意见
	 * @param result	审批结果
	 * @return 更新记录数
	 */
	@Override
	public int lastReview(Integer partnerId,String reviewPidUid,String review,String result) {
		PartnerBasic basic = new PartnerBasic();
		basic.setPartnerId(partnerId);
		basic.setLreviewOpr(reviewPidUid);
		basic.setLreviewLog(review);
		basic.setLreviewTime(new Date());
		basic.setStatus(result);
		return this.partnerBasicMapper.lastReview(basic);
	}

	/**
	 * 自己暂时关闭店铺或打开
	 * @param partnerId	合作伙伴ID
	 * @param newStatus 新的状态：S-正常，C-关闭
	 * @return 更新记录数
	 */
	@Override
	public int changeStatusOwn(Integer partnerId,String newStatus) {
		PartnerBasic basic = new PartnerBasic();
		basic.setPartnerId(partnerId);
		basic.setStatus(newStatus);
		return this.partnerBasicMapper.updateByPrimaryKeySelective(basic);
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
	
	/**
	 * 获取所有的合作伙伴信息
	 * 
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<PartnerBasic> getAll(Map<String,Object> params,String sorts,PageCond pageCond){
		if(pageCond == null) {
			pageCond = new PageCond(0,20);
		}
		return this.partnerBasicMapper.selectAll(params, sorts, pageCond);
	}
	
	
	/**
	 * 统计所有的合作伙伴数量
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params) {
		return this.partnerBasicMapper.countAll(params);
	}
	
	
}
