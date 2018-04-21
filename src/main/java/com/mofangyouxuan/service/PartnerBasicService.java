package com.mofangyouxuan.service;

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
	public int update(PartnerBasic basic);
	
	/**
	 * 记录审批人员的审批结果
	 * @param partnerId	合作伙伴ID
	 * @param oprId	审批人员ID
	 * @param review	审批意见
	 * @param result	审批结果
	 * @return 更新记录数
	 */
	public int review(Integer partnerId,Integer oprId,String review,String result);
	

	/**
	 * 自己暂时关闭店铺或打开
	 * @param partnerId	合作伙伴ID
	 * @param newStatus 新的状态：S-正常，C-关闭
	 * @return 更新记录数
	 */
	public int changeShopStatus(Integer partnerId,String newStatus);

}
