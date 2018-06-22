package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PartnerBasic;

public interface PartnerBasicService {
	
	/**
	 * 根据ID获取合作胡伙伴基本信息
	 * @param id	
	 * @return
	 */
	public PartnerBasic getByID(Integer id);
	
	/**
	 * 根据绑定用户获取合作伙伴
	 * @param userId
	 * @return
	 */
	public PartnerBasic getByBindUser(Integer userId);
	
	
	/**
	 * 添加合作伙伴
	 * @param basic
	 * @return 新ID或小于0的错误码
	 */
	public Integer add(PartnerBasic basic);
	
	/**
	 * 自己更新合作伙伴信息
	 * @param basic
	 * @return 更新记录数
	 */
	public int updateBasic(PartnerBasic basic);
	
	/**
	 * 更新合作伙伴部分信息
	 * @param basic
	 * @return 更新记录数
	 */
	public int updateSelective(PartnerBasic basic);
	
	
	/**
	 * 记录审批人员的审批结果
	 * @param partnerId	合作伙伴ID
	 * @param reviewPidUid	审批人员：上级合作伙伴ID#员工ID
	 * @param review	审批意见
	 * @param result	审批结果
	 * @return 更新记录数
	 */
	public int review(Integer partnerId,String reviewPidUid,String review,String result);
	

	/**
	 * 自己暂时关闭店铺或打开
	 * @param partnerId	合作伙伴ID
	 * @param newStatus 新的状态：S-正常，C-关闭
	 * @return 更新记录数
	 */
	public int changeShopStatus(Integer partnerId,String newStatus);
	
	/**
	 * 更新商家的评价得分
	 * @param scoreLogis
	 * @param scoreServ
	 * @param scoreGoods
	 */
	public void updScore(Integer partnerId,Integer scoreLogis,Integer scoreServ,Integer scoreGoods);

	
	/**
	 * 获取所有的合作伙伴信息
	 * 
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<PartnerBasic> getAll(Map<String,Object> params,String sorts,PageCond pageCond);
	
	
	/**
	 * 统计所有的合作伙伴数量
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params);
	
	
}
