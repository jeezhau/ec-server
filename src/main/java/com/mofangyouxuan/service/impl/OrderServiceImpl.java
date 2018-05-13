package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
import com.mofangyouxuan.mapper.PayFlowMapper;
import com.mofangyouxuan.mapper.OrderMapper;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.wxapi.WXPay;

@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderMapper orderMapper;
	
	@Autowired
	private PayFlowMapper payFlowMapper;
	@Autowired
	private ChangeFlowService changeFlowService;
	@Autowired
	private WXPay wXPay;
	
	/**
	 * 新增订单
	 * @param order
	 * @return
	 */
	@Override
	public String add(Order order) {
		order.setOrderId(this.genOrderId(order.getUserId()));
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
		return ErrCodes.COMMON_DB_ERROR + "";
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
	public Order get(String orderId) {
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
		
		Map<String,Object> params = getSearParamsMap(jsonParams);
		
		return this.orderMapper.selectAll(params, sorts, pageCond);
	}

	/**
	 * 根据条件统计订单数量
	 */
	@Override
	public int countAll(JSONObject jsonParams) {
		Map<String,Object> params = getSearParamsMap(jsonParams);
		return this.orderMapper.countAll(params);
	}
	
	
	/**
	 * 解析查询条件
	 * @param jsonParams
	 * @return
	 */
	public Map<String,Object> getSearParamsMap(JSONObject jsonParams){
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
	
	/**
	 * 买家取消订单
	 * 
	 * @param order		订单信息
	 * @param userVip	买家会员账户
	 * @param mchtVipId	卖家会员账户ID
	 * @return {errcode,errmsg}
	 */
	public JSONObject cancelOrder(Order order,VipBasic userVip,Integer mchtVipId) {
		JSONObject jsonRet = new JSONObject();
		Order cancelOrder = new Order();
		cancelOrder.setOrderId(order.getOrderId());
		//未支付直接取消
		if("10".equals(order.getStatus()) || "12".equals(order.getStatus())) {
			cancelOrder.setStatus("DS");
			int cnt = this.orderMapper.updateByPrimaryKeySelective(cancelOrder);
			if(cnt>0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg","订单已成功取消！");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg","数据库处理失败！");
			}
			return jsonRet;
		}
		if(!"11".equals(order.getStatus()) && !"20".equals(order.getStatus())) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","您当前还未完成支付，不可执行退款！！");
			return jsonRet;
		}
		//查询已有最新支付流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),"1");
		if(oldFlow == null) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","系统没有您的支付流水信息！");
			return jsonRet;
		}
		if(!"10".equals(oldFlow.getStatus()) || !"11".equals(oldFlow.getStatus())) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","您的订单没有支付成功信息（未支付、支付失败或已退款）！");
			return jsonRet;
		}
		
		String refundFlowId = this.genFlowId(oldFlow.getOrderId(), oldFlow.getFlowId());
		Long totalAmount = oldFlow.getPayAmount() + oldFlow.getFeeAmount();
		PayFlow newFlow = null;
		if("1".equals(oldFlow.getPayType())) {
			//账户余额退款
			this.changeFlowService.refundSuccess(true, order.getAmount(), 
					userVip, "买家申请全额退款", userVip.getVipId(), mchtVipId);
			newFlow = new PayFlow();
			newFlow.setStatus("21");//退款成功
		}else if("2".equals(oldFlow.getPayType())){
			//微信退款
			JSONObject wxRet = this.wXPay.applyRefund(refundFlowId, totalAmount, oldFlow.getFlowId());
			if(wxRet.containsKey("refund_id")) {//申请成功
				newFlow = new PayFlow();
				newFlow.setOutFinishId(wxRet.getString("refund_id"));
				newFlow.setStatus("20");
			}else {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg","您的退款申请发送至微信退款失败，请稍后再试！");
				return jsonRet;
			}
		}
		if(newFlow != null) {
			newFlow.setCreateTime(new Date());
			newFlow.setCurrencyType("CNY");
			newFlow.setFeeAmount(0L);
			newFlow.setFlowId(refundFlowId);
			newFlow.setFlowType("2");
			newFlow.setGoodsId(order.getGoodsId());
			newFlow.setOrderId(order.getOrderId());
			newFlow.setPayAccount(oldFlow.getPayAccount());
			newFlow.setPayAmount(totalAmount);
			newFlow.setUserId(oldFlow.getUserId());
		}
		int cnt = this.payFlowMapper.insert(newFlow);
		if(cnt>0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据保存出错！");
		}
		return jsonRet;
	}
	
	/**
	 * 生成预支付订单
	 * 
	 * @param user		买家基本信息
	 * @param userVip	买家会员信息
	 * @param order		订单信息
	 * @param mchtVipId	卖家会员ID
	 * @param payType	支付方式
	 * @param ip		买家IP地址
	 * @return {errcode,errmsg,payType,prepay_id,total}
	 */
	public JSONObject createPay(UserBasic user,VipBasic userVip,Order order,Integer mchtVipId,String payType,String ip) {
		JSONObject jsonRet = new JSONObject();
		//查询已有最新支付流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),"1");
		BigDecimal amount = order.getAmount().multiply(new BigDecimal(100)).setScale(0);//订单金额，分
		BigDecimal fee = new BigDecimal(0);
		String outTradeNo = null;
		String payAccount = null;
		String flowId = null;
		if(oldFlow != null) {
			if(!"00".equals(oldFlow.getStatus()) && !"F1".equals(oldFlow.getStatus())) {//非待支付或支付失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "支付预付单生成失败，订单已在支付或退款！");
				return jsonRet;
			}else if("00".equals(oldFlow.getStatus())) {//待支付
				Long t = (new Date().getTime()-oldFlow.getCreateTime().getTime())/1000/3600;
				if(t<=1) {//支付超时
					jsonRet.put("errcode", 0);
					jsonRet.put("payType", payType);
					jsonRet.put("prepay_id", oldFlow.getOutTradeNo());
					jsonRet.put("errmsg", "ok");
					return jsonRet;
				}else {//超时未支付关闭
					oldFlow.setStatus("01");
					this.payFlowMapper.updateByPrimaryKeySelective(oldFlow);
				}
			}
		}
		//新申请预付单
		String oldFlowId = oldFlow == null ? null : oldFlow.getFlowId();
		flowId = this.genFlowId(order.getOrderId(), oldFlowId); //新消费流水单号
		//向微信申请预付单
		if("2".equals(payType)) {
			fee = amount.multiply(new BigDecimal(wXPay.wxFeeRate)).setScale(0, BigDecimal.ROUND_CEILING);
			Long totalAmount = amount.longValue() + fee.longValue();//支付金额
			JSONObject wxRet = wXPay.unifiedOrder(order,flowId, totalAmount, user, ip);
			if(wxRet.containsKey("prepay_id")) {//成功
				payAccount = user.getOpenId();
				outTradeNo = wxRet.getString("prepay_id");
			}else {//失败
				if("ORDERCLOSED".equals(jsonRet.getString("errcode"))) {//已关闭
					//再次申请
					flowId = this.genFlowId(order.getOrderId(), flowId);
					wxRet = wXPay.unifiedOrder(order, flowId,totalAmount, user, ip);
					if(wxRet.containsKey("prepay_id")) {//成功
						payAccount = user.getOpenId();
						outTradeNo = wxRet.getString("prepay_id");
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
						return jsonRet;
					}
				}else if("OUT_TRADE_NO_USED".equals(jsonRet.getString("errcode"))) {//微信有，系统无
					wxRet = wXPay.closeOrder(flowId, user);
					if(wxRet.getIntValue("errcode") == 0) {
						//再次申请
						flowId = this.genFlowId(order.getOrderId(), flowId);
						wxRet = wXPay.unifiedOrder(order, flowId,totalAmount, user, ip);
						if(wxRet.containsKey("prepay_id")) {//成功
							payAccount = user.getOpenId();
							outTradeNo = wxRet.getString("prepay_id");
						}else {
							jsonRet.put("errcode", -1);
							jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
							return jsonRet;
						}
					}else {
						jsonRet.put("errcode", -1);
						jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
						return jsonRet;
					}
				}else {
					jsonRet.put("errcode", -1);
					jsonRet.put("errmsg", "微信支付预付单生成失败，" + wxRet.getString("errmsg"));
					return jsonRet;
				}
			}
		}else if("1".equals(payType)){
			if(userVip.getBalance().compareTo(amount)<0) {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "余额支付失败，余额不足！");
				return jsonRet;
			}else {
				payAccount = userVip.getVipId() + "";
				outTradeNo = null;
			}
		}
		//数据处理保存
		PayFlow newFlow = new PayFlow();
		newFlow.setCreateTime(new Date());
		newFlow.setCurrencyType("CNY");
		newFlow.setFeeAmount(fee.longValue());
		newFlow.setFlowId(flowId);
		newFlow.setFlowType("1");
		newFlow.setGoodsId(order.getGoodsId());
		newFlow.setOutTradeNo(outTradeNo);
		newFlow.setOrderId(order.getOrderId());
		newFlow.setPayAccount(payAccount);
		newFlow.setPayAmount(amount.longValue());
		newFlow.setUserId(user.getUserId());
		newFlow.setStatus("00");
		if("1".equals(payType)) {
			this.changeFlowService.paySuccess(true, order.getAmount(), userVip, 
					"订单号:" + order.getOrderId() + ";商品购买：" + order.getGoodsSpec(), 
					order.getUserId(), mchtVipId);
			newFlow.setStatus("11");
		}
		int cnt = this.payFlowMapper.insert(newFlow);
		if(cnt>0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("prepay_id", newFlow.getOutTradeNo());
			jsonRet.put("payType", payType);
			jsonRet.put("total", amount.longValue()+fee.longValue());
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据保存出错！");
		}
		return jsonRet;
	}
	
	/**
	 * 更新支付结果信息
	 * @param flow
	 * @return
	 */
	public int updPayResut(PayFlow flow) {
		return this.payFlowMapper.updateByPrimaryKeySelective(flow);
	}
	
	/**
	 * 更新退款结果信息
	 * @param flow
	 * @return
	 */
	public int updRefundResutl(PayFlow flow) {
		return this.payFlowMapper.updateByPrimaryKeySelective(flow);
	}
	
	/**
	 * 生成30位的订单ID
	 * @param userId
	 * @return
	 */
	private String genOrderId(Integer userId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String currTime = sdf.format(new Date());
		String uId = userId + "";
		for(int i=0;i<16-uId.length();i++) {
			uId = "0" + uId;
		}
		return currTime + uId;
	}
	
	/**
	 * 生成32位的消费流水ID
	 * @param orderId
	 * @return
	 */
	private String genFlowId(String orderId,String oldId) {
		int next = 0;
		if(oldId != null && oldId.length() == 32) {
			next = Integer.parseInt(oldId.substring(30));
		}
		next += 1;
		if(next<10) {
			return orderId + "0" +next;
		}
		return orderId + next;
	}
	
}
