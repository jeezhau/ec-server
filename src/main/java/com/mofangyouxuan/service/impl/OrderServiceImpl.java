package com.mofangyouxuan.service.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.OrderMapper;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderMapper orderMapper;
	
	/**
	 * 新增订单
	 * @param order
	 * @return
	 */
	@Override
	public BigInteger add(Order order) {
		order.setOrderId(null);
		order.setCreateTime(new Date());
		order.setStatus("10");
		order.setLogisticsComp(null);
		order.setLogisticsNo(null);
		order.setSendTime(null);
		order.setSignTime(null);
		order.setSignUser(null);
		order.setScoreGoods(null);
		order.setScoreLogistics(null);
		order.setScoreMerchant(null);
		order.setAppraiseInfo(null);
		order.setAppraiseStatus(null);
		order.setAftersalesReason(null);
		order.setAftersalesResult(null);
		int cnt = this.orderMapper.insert(order);
		if(cnt>0) {
			return order.getOrderId();
		}
		return new BigInteger(ErrCodes.COMMON_DB_ERROR+"");
	}
	
	/**
	 * 修改订单
	 * @param order
	 * @return
	 */
	@Override
	public int update(Order order) {
		
		int cnt = this.orderMapper.updateByPrimaryKeySelective(order);
		return cnt;
	}
	
	/**
	 * 删除订单
	 * @param order
	 * @return
	 */
	@Override
	public int delete(Order order) {
		
		int cnt = this.orderMapper.deleteByPrimaryKey(order.getOrderId());
		return cnt;
	}
	
	/**
	 * 根据ID获取订单
	 * @param orderId
	 * @return
	 */
	@Override
	public Order get(BigInteger orderId) {
		return this.orderMapper.selectByPrimaryKey(orderId);
	}
	
	/**
	 * 根据指定查询条件、排序条件、分页信息获取订单信息
	 * @param jsonParams
	 * @param jsonSorts
	 * @param pageCond
	 * @return
	 */
	public List<Order> getAll(JSONObject jsonParams,JSONObject jsonSorts,PageCond pageCond){
		String sorts = null;
		if(jsonSorts != null) {
			Map<Integer,String> sortMap = new HashMap<Integer,String>();
			if(jsonSorts.containsKey("createTime")) {
				String value = jsonSorts.getString("createTime");
				if(value != null && value.length()>0) {
					String[] arr = value.split("#");
					sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " create_time asc " : " create_time desc " );
				}
			}
			if(jsonSorts.containsKey("sendTime")) {
				String value = jsonSorts.getString("sendTime");
				if(value != null && value.length()>0) {
					String[] arr = value.split("#");
					sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " send_time asc " : " send_time desc " );
				}
			}				
			if(jsonSorts.containsKey("signTime")) {
				String value = jsonSorts.getString("signTime");
				if(value != null && value.length()>0) {
					String[] arr = value.split("#");
					sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " sign_time asc " : " sign_time desc " );
				}
			}
			if(jsonSorts.containsKey("appraiseTime")) {
				String value = jsonSorts.getString("appraiseTime");
				if(value != null && value.length()>0) {
					String[] arr = value.split("#");
					sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " appraise_time asc " : " appraise_time desc " );
				}
			}
			if(jsonSorts.containsKey("aftersalesApplyTime")) {
				String value = jsonSorts.getString("aftersalesApplyTime");
				if(value != null && value.length()>0) {
					String[] arr = value.split("#");
					sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " aftersales_apply_time asc " : " aftersales_apply_time desc " );
				}
			}
			if(jsonSorts.containsKey("aftersalesDealTime")) {
				String value = jsonSorts.getString("aftersalesDealTime");
				if(value != null && value.length()>0) {
					String[] arr = value.split("#");
					sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " aftersales_deal_time asc " : " aftersales_deal_time desc " );
				}
			}
			Set<Integer> set = new TreeSet<Integer>(sortMap.keySet());
			StringBuilder sb = new StringBuilder();
			for(Integer key:set) {
				sb.append(",");
				sb.append(sortMap.get(key));
			}
			if(sb.length()>0) {
				sorts = " order by " + sb.substring(1);
			}else {
				sorts = " order by order_id desc ";
			}
		}
		
		Map<String,Object> params = getParamsMap(jsonParams);
		
		return this.orderMapper.selectAll(params, sorts, pageCond);
	}

	/**
	 * 根据条件统计订单数量
	 */
	@Override
	public int countAll(JSONObject jsonParams) {
		Map<String,Object> params = getParamsMap(jsonParams);
		return this.orderMapper.countAll(params);
	}
	
	
	/**
	 * 解析查询条件
	 * @param jsonParams
	 * @return
	 */
	public Map<String,Object> getParamsMap(JSONObject jsonParams){
		Map<String,Object> params = new HashMap<String,Object>();
		//params.put("status", "41,56");	//默认评价完成
		
		if(jsonParams.containsKey("userId")) { //下单用户
			params.put("userId", jsonParams.getInteger("userId"));
		}
		if(jsonParams.containsKey("goodsId")) {
			params.put("goodsId", jsonParams.getLong("goodsId"));
		}
		if(jsonParams.containsKey("status")) {//多个状态使用逗号分隔
			params.put("status", jsonParams.getString("status"));
		}
		if(jsonParams.containsKey("partnerId")) {//查询指定合作伙伴
			params.put("partnerId", jsonParams.getInteger("partnerId"));
		}
		if(jsonParams.containsKey("keywords")) {//使用关键字查询
			params.put("goodsName", jsonParams.getString("keywords"));
			params.put("orderId", jsonParams.getString("keywords"));
		}
		if(jsonParams.containsKey("categoryId")) { //商品分类ID
			params.put("categoryId", jsonParams.getInteger("categoryId"));
		}
		if(jsonParams.containsKey("dispatchMode")) { //派送模式
			params.put("dispatchMode", jsonParams.getString("dispatchMode"));
		}
		if(jsonParams.containsKey("postageId")) { //运费模板ID
			params.put("postageId", jsonParams.getInteger("postageId"));
		}
		if(jsonParams.containsKey("appraiseStatus")) { //评价内容状态
			params.put("appraiseStatus", jsonParams.getString("appraiseStatus"));
		}
		
		if(jsonParams.containsKey("beginCreateTime")) { //订单创建开始时间
			params.put("beginCreateTime", jsonParams.getString("beginCreateTime"));
		}
		if(jsonParams.containsKey("endCreateTime")) { //订单创建结束时间
			params.put("endCreateTime", jsonParams.getString("endCreateTime"));
		}
		if(jsonParams.containsKey("beginSendTime")) { //订单发货开始时间
			params.put("beginSendTime", jsonParams.getString("beginSendTime"));
		}
		if(jsonParams.containsKey("endSendTime")) { //订单发货结束时间
			params.put("endSendTime", jsonParams.getString("endSendTime"));
		}
		if(jsonParams.containsKey("beginSignTime")) { //订单签收开始时间
			params.put("beginSignTime", jsonParams.getString("beginSignTime"));
		}
		if(jsonParams.containsKey("endSignTime")) { //订单签收结束时间
			params.put("endSignTime", jsonParams.getString("endSignTime"));
		}
		return params;
	}
	
	/**
	 * 分状态统计订单数量
	 * @param partnerId
	 * @param goodsId
	 * @param userId
	 * @return
	 */
	public  List<Map<String,Integer>> countPartibyStatus(Integer partnerId,Long goodsId,Integer userId){
		return this.orderMapper.countPartibyStatus(partnerId, goodsId, userId);
	}
}
