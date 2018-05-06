package com.mofangyouxuan.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Order;

/**
 * 商品订单管理服务
 * @author jeekhan
 *
 */
public interface OrderService {

	/**
	 * 新增订单
	 * @param order
	 * @return
	 */
	public BigInteger add(Order order);
	
	/**
	 * 修改订单
	 * @param order
	 * @return
	 */
	public int update(Order order);
	
	/**
	 * 删除订单
	 * @param order
	 * @return
	 */
	public int delete(Order order);
	
	/**
	 * 根据ID获取订单
	 * @param orderId
	 * @return
	 */
	public Order get(BigInteger orderId);
	
	/**
	 * 根据指定查询条件订单数量
	 * @param params
	 * @return
	 */
	public int countAll(JSONObject jsonParams);
	
	/**
	 * 根据指定查询条件、排序条件、分页信息获取订单信息
	 * @param jsonParams
	 * @param jsonSorts
	 * @param pageCond
	 * @return
	 */
	public List<Order> getAll(JSONObject jsonParams,JSONObject jsonSorts,PageCond pageCond);
	
	/**
	 * 分状态统计订单数量
	 * @param partnerId
	 * @param goodsId
	 * @param userId
	 * @return
	 */
	public  List<Map<String,Integer>> countPartibyStatus(Integer partnerId,Long goodsId,Integer userId);
	
}


