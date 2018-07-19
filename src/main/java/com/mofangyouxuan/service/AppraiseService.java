package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Appraise;
import com.mofangyouxuan.model.Order;

public interface AppraiseService {
	
	public JSONObject save(Appraise appraise);
	
	public JSONObject update(Appraise appraise);
	
	public Appraise getByID(Long Id);
	
	public int countAll(Map<String,Object> params);
	
	public List<Appraise> getAll(Map<String,Object> params,PageCond pageCond);
	
	public Appraise getByOrderIdAndObj(String orderId,String object);
	
	/**
	 * 添加买家对商家的评价或者系统自动超时评价
	 * @param order	订单信息
	 * @param scoreLogistics		物流得分
	 * @param scoreMerchant	商家服务得分
	 * @param scoreGoods		商品描述得分
	 * @param content	评价内容
	 * @return
	 */
	public JSONObject appraise2Mcht(Order order,Integer scoreLogistics,Integer scoreMerchant,
			Integer scoreGoods,String content);
	
	/**
	 * 添加卖家对买家的评价或者系统自动超时评价
	 * @param order	订单信息
	 * @param score	得分
	 * @param content	评价内容
	 * @return
	 */
	public JSONObject appraise2User(Order order,Integer score,String content,Integer updateOpr);

	/**
	 * 记录评价审批结果
	 * @param orderId
	 * @param rewPartnerId
	 * @param oprId
	 * @param result 审批结果：1-审核通过，2-审核拒绝
	 * @param review
	 * @return
	 */
	public JSONObject review(String orderId,Integer rewPartnerId,Integer oprid,String result,String review);
	

}
