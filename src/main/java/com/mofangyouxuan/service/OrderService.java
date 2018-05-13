package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;

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
	public String add(Order order);
	
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
	public Order get(String orderId);
	
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
	
	/**
	 * 买家取消订单
	 * 
	 * @param order		订单信息
	 * @param userVip	买家会员账户
	 * @param mchtVipId	卖家会员账户ID
	 * @return
	 */
	public JSONObject cancelOrder(Order order,VipBasic userVip,Integer mchtVipId) ;
	
	/**
	 * 生成预支付订单
	 * 
	 * @param user		买家基本信息
	 * @param userVip	买家会员信息
	 * @param order		订单信息
	 * @param mchtVipId	卖家会员ID
	 * @param payType	支付方式
	 * @param ip		买家IP地址
	 * @return
	 */
	public JSONObject createPay(UserBasic user,VipBasic userVip,Order order,Integer mchtVipId,String payType,String ip);
	
}


