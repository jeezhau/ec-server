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
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.PayFlowMapper;
import com.mofangyouxuan.mapper.OrderMapper;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.model.GoodsSpec;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.GoodsService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.utils.CommonUtil;
import com.mofangyouxuan.wxapi.WXPay;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderMapper orderMapper;
	
	@Autowired
	private PayFlowMapper payFlowMapper;
	@Autowired
	private GoodsService goodsService;
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
		order.setOrderId(CommonUtil.genOrderId(order.getUserId()));
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
	 * @param params 需要显示哪些分类字段：needReceiver,needLogistics,needAppr,needAfterSales,needGoodsAndUser
     * @param orderId
	 * @return
	 */
	@Override
	public Order get(Boolean needReceiver,Boolean needLogistics,Boolean needAppr,Boolean needAfterSales,Boolean needGoodsAndUser,String orderId) {
		Map<String,Object> params = new HashMap<String,Object>();
		if(needReceiver != null) {
			params.put("needReceiver", needReceiver);
		}
		if(needLogistics != null) {
			params.put("needLogistics", needLogistics);
		}
		if(needAppr != null) {
			params.put("needAppr", needAppr);
		}
		if(needAfterSales != null) {
			params.put("needAfterSales", needAfterSales);
		}
		if(needGoodsAndUser != null) {
			params.put("needGoodsAndUser", needGoodsAndUser);
		}
		return this.orderMapper.selectByPrimaryKey(params,orderId);
	}
	
	/**
	 * 根据指定查询条件、排序条件、分页信息获取订单信息
	 * @param jsonShowGroups	需要显示的字段分组
	 * @param jsonParams
	 * @param jsonSorts
	 * @param pageCond
	 * @return
	 */
	public List<Order> getAll(JSONObject jsonShow,JSONObject jsonParams,JSONObject jsonSorts,PageCond pageCond){
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
		if(jsonShow != null) {
			if(jsonShow.containsKey("needReceiver") && jsonShow.get("needReceiver") != null) {
				params.put("needReceiver", true);
			}
			if(jsonShow.containsKey("needLogistics") && jsonShow.get("needLogistics") != null) {
				params.put("needLogistics", true);
			}
			if(jsonShow.containsKey("needAppr") && jsonShow.get("needAppr") != null) {
				params.put("needAppr", true);
			}
			if(jsonShow.containsKey("needAfterSales") && jsonShow.get("needAfterSales") != null) {
				params.put("needAfterSales", true);
			}
			if(jsonShow.containsKey("needGoodsAndUser") && jsonShow.get("needGoodsAndUser") != null) {
				params.put("needGoodsAndUser", true);
			}
		}
		
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
	private Map<String,Object> getSearParamsMap(JSONObject jsonParams){
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
	 * @param userVipId	买家会员账户ID
	 * @param mchtVipId	卖家会员账户ID
	 * @param reason		退款理由
	 * @return {errcode,errmsg}
	 */
	public JSONObject cancelOrder(Order order,Integer userVipId,Integer mchtVipId,String reason) {
		JSONObject jsonRet = new JSONObject();
		//未支付直接取消
		if("10".equals(order.getStatus()) || "12".equals(order.getStatus())) {
			Order cancelOrder = new Order();
			cancelOrder.setOrderId(order.getOrderId());
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
			jsonRet.put("errmsg","您当前不可执行退款！！");
			return jsonRet;
		}
		
		//查询已有最新支付流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),"1");
		if(oldFlow == null) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","系统没有您的支付流水信息！");
			return jsonRet;
		}
		if(!"11".equals(oldFlow.getStatus())) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","您的订单没有支付成功信息（未支付到账、支付失败或已退款）！");
			return jsonRet;
		}
		//退款
		return this.execRefund(false,order, oldFlow, userVipId, mchtVipId, "0", reason);
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
		Long totalAmount = null; //总实付金额
		if(oldFlow != null) {
			if(!"00".equals(oldFlow.getStatus()) && !"01".equals(oldFlow.getStatus()) && !"F1".equals(oldFlow.getStatus())) {//非待支付或支付失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "支付预付单生成失败：订单已在支付或退款！");
				return jsonRet;
			}else if("00".equals(oldFlow.getStatus())) {//待支付
				Long t = (new Date().getTime()-oldFlow.getCreateTime().getTime())/1000/3600;
				if(t<=1) {//支付未超时
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
		flowId = CommonUtil.genPayFlowId(order.getOrderId(), oldFlowId); //新消费流水单号
		//向微信申请预付单
		if("2".equals(payType)) {
			fee = amount.multiply(new BigDecimal(wXPay.wxFeeRate)).setScale(0, BigDecimal.ROUND_CEILING);
			totalAmount = amount.longValue() + fee.longValue();//支付金额
			JSONObject wxRet = wXPay.unifiedOrder(order,flowId, totalAmount, user, ip);
			if(wxRet.containsKey("prepay_id")) {//成功
				payAccount = user.getOpenId();
				outTradeNo = wxRet.getString("prepay_id");
			}else {//失败
				if("ORDERCLOSED".equals(jsonRet.getString("errcode"))) {//已关闭
					//再次申请
					flowId = CommonUtil.genPayFlowId(order.getOrderId(), flowId);
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
						flowId = CommonUtil.genPayFlowId(order.getOrderId(), flowId);
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
			if(userVip.getBalance().multiply(new BigDecimal(100)).compareTo(amount)<0) {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "余额支付失败，余额不足！");
				return jsonRet;
			}else {
				fee = new BigDecimal(0);		//余额支付手续费
				totalAmount = amount.longValue() + fee.longValue();//总支付金额
				payAccount = userVip.getVipId() + "";
				outTradeNo = null;
			}
		}
		//支付流水数据处理保存
		Date payTime = new Date();
		PayFlow newFlow = new PayFlow();
		newFlow.setCreateTime(payTime);
		newFlow.setCurrencyType("CNY");
		newFlow.setFeeAmount(fee.longValue());
		newFlow.setFlowId(flowId);
		newFlow.setFlowType("1");
		newFlow.setGoodsId(order.getGoodsId());
		newFlow.setOutTradeNo(outTradeNo);
		newFlow.setOrderId(order.getOrderId());
		newFlow.setPayType(payType);
		newFlow.setPayAccount(payAccount);
		newFlow.setPayAmount(amount.longValue());
		newFlow.setUserId(user.getUserId());
		newFlow.setStatus("00");
		int cnt = this.payFlowMapper.insert(newFlow);
		if(cnt>0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("prepay_id", newFlow.getOutTradeNo());
			jsonRet.put("payType", payType);
			jsonRet.put("total", totalAmount); //实付：分
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据保存出错！");
		}
		return jsonRet;
	}
	
	/**
	 * 提交余额支付
	 * @param payFlow
	 * @param userVip
	 * @param order
	 * @param mchtVipId
	 */
	@Override
	public void submitBalPay(PayFlow payFlow,VipBasic userVip,Order order,Integer mchtVipId) {
		Long totalAmount = payFlow.getPayAmount() + payFlow.getFeeAmount();
		//添加现金余额流水
		this.changeFlowService.paySuccess(true, new BigDecimal(totalAmount/100), userVip, 
				"商品购买【订单号:" + payFlow.getOrderId() + "】", 
				payFlow.getUserId(), mchtVipId);
		//更新支付流水
		payFlow.setStatus("11");	//支付完成
		payFlow.setIncomeAmount(totalAmount);//入账金额，分
		payFlow.setIncomeTime(new Date());
		this.payFlowMapper.updateByPrimaryKeySelective(payFlow);
		//更新订单
		Order newO = new Order();
		newO.setOrderId(payFlow.getOrderId());
		newO.setStatus("20"); //支付成功，待发货
		this.orderMapper.updateByPrimaryKeySelective(newO);
		
		//更新库存:减少
		//Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
		List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
		this.goodsService.changeSpec(order.getGoodsId(), buySpec, 3, null, null);
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
	 * 客户端支付完成
	 * @param user
	 * @param order
	 * @param clientStatus 支付结果：success，fail
	 * @return {errcode,errmsg,payType,payTime,amount,fee}
	 */
	@Override
	public JSONObject payFinish(UserBasic user,Order order,String clientStatus) {
		JSONObject jsonRet = new JSONObject();
		//查询已有最新支付流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),"1");
		if(oldFlow == null) {
			jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
			jsonRet.put("errmsg", "系统中没有您的支付记录！");
			return jsonRet;
		}
		jsonRet.put("payType", oldFlow.getPayType());
		jsonRet.put("payTime", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(oldFlow.getCreateTime()));
		jsonRet.put("amount", oldFlow.getPayAmount()/100.0);
		jsonRet.put("fee", oldFlow.getFeeAmount()/100.0);
		String sysStat = oldFlow.getStatus();
		
		if("11".equals(sysStat)) {//后端成功
			//更新订单
			if(!"20".equals(order.getStatus())) {
				Order newO = new Order();
				newO.setOrderId(order.getOrderId());
				newO.setStatus("20"); //支付成功，待发货
				this.orderMapper.updateByPrimaryKeySelective(newO);
				//更新库存:减少
				List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
				this.goodsService.changeSpec(order.getGoodsId(), buySpec, 
						3, null, null);
			}
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "支付成功！");
		}else if("00".equals(sysStat) || "10".equals(sysStat)){
			if( "success".equals(clientStatus)) {//前端成功，
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "我们还未收到第三方支付平台给出的您的支付结果通知，如果您确定已完成支付，请稍后再查看！！");
			}else {
				oldFlow.setStatus("01");//支付失败
				this.payFlowMapper.updateByPrimaryKeySelective(oldFlow);
				Order newO = new Order();
				newO.setOrderId(order.getOrderId());
				newO.setStatus("12"); //支付失败
				this.orderMapper.updateByPrimaryKeySelective(newO);
				jsonRet.put("errcode", ErrCodes.ORDER_PAY_ERROR);
				jsonRet.put("errmsg", "订单支付失败，您可重新支付！");
			}
		}else {
			jsonRet.put("errcode", ErrCodes.ORDER_PAY_ERROR);
			jsonRet.put("errmsg", "该订单当前状态不在支付流程中！");
		}
		return jsonRet;
	}
	
	/**
	 * 获取指定订单的最新支付流水
	 * @param orderId
	 * @param flowType 流水类型：1-支付，2-退款
	 * @return
	 */
	@Override
	public PayFlow getLastedFlow(String orderId,String flowType) {
		return this.payFlowMapper.selectLastestFlow(orderId,flowType);
	}
	
	/**
	 * 订单退款执行
	 * 1、向第三方支付申请退款，或余额退款；
	 * 2、保存退款流水；
	 * 3、更新订单售后信息；
	 * @param isMcht		是否是商家申请退款
	 * @param order		订单信息
	 * @param payFlow	支付流水
	 * @param userVipId	买家会员账户
	 * @param mchtVipId	卖家会员ID
	 * @param type		退款类型 ：0-买家取消，1-卖家未发货，2-买家未收到货，3-买家收货后不满意，卖家同意
	 * @param reason		退款理由，对于收货退款，其中包含退款方式与快递信息
	 * @return
	 */
	@Override
	public JSONObject execRefund(boolean isMcht,Order order,PayFlow payFlow,Integer userVipId,Integer mchtVipId,String type,String reason) {
		JSONObject jsonRet = new JSONObject();

		String refundFlowId = CommonUtil.genPayFlowId(payFlow.getOrderId(), payFlow.getFlowId()); //退款流水ID
		Long totalAmount = payFlow.getPayAmount() + payFlow.getFeeAmount();//退款金额，分
		PayFlow refundFlow = null;
		Order updOdr = new Order();
		Date currTime = new Date();
		String changeReason = ""; //资金变更流水理由前缀
		if("1".equals(payFlow.getPayType())) {//余额支付
			updOdr.setStatus("65"); //65 退款完成
			if("0".equals(type)) {
				changeReason = "买家取消全额退款";
			}else if("1".equals(type)) {//未发货退款
				changeReason = "卖家限时未发货全额退款";
			}else if("2".equals(type)) {//未收到货退款
				changeReason = "买家限时未收到货全额退款";
			}else { //3-收货后退款
				changeReason = "买家收货后申请全额退款";
			}
			//账户余额退款
			this.changeFlowService.refundSuccess(true, new BigDecimal(totalAmount/100), 
					userVipId, changeReason + "【订单号:" + order.getOrderId() +"】", isMcht?mchtVipId:userVipId, mchtVipId);
			refundFlow = new PayFlow();
			refundFlow.setPayType("1");
			refundFlow.setStatus("21");//退款成功
			refundFlow.setIncomeAmount(totalAmount);//入账金额，分
			refundFlow.setIncomeTime(currTime);
			
			//更新库存:增加
			List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
			this.goodsService.changeSpec(order.getGoodsId(), buySpec, 2, null, null);
			
		}else if("2".equals(payFlow.getPayType())){
			//微信退款
			JSONObject wxRet = this.wXPay.applyRefund(refundFlowId, totalAmount, payFlow.getOutFinishId());
			if(wxRet.containsKey("refund_id")) {//申请成功
				refundFlow = new PayFlow();
				refundFlow.setPayType("2");
				refundFlow.setOutFinishId(wxRet.getString("refund_id"));
				refundFlow.setStatus("20");
				
				updOdr.setStatus("64"); //64:同意退款，申请资金回退，
			}else {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg","您的退款申请发送至微信退款失败，请稍后再试！");
				return jsonRet;
			}
		}
		
		if(refundFlow != null) {//退款流水
			refundFlow.setCreateTime(currTime);
			refundFlow.setCurrencyType("CNY");
			refundFlow.setFeeAmount(0L);
			refundFlow.setFlowId(refundFlowId);
			refundFlow.setFlowType("2");
			refundFlow.setGoodsId(order.getGoodsId());
			refundFlow.setOrderId(order.getOrderId());
			refundFlow.setPayAccount(refundFlow.getPayAccount());
			refundFlow.setPayAmount(totalAmount);
			refundFlow.setUserId(refundFlow.getUserId());
			
			updOdr.setOrderId(order.getOrderId());
			JSONObject asr = new JSONObject();
			asr.put("time", currTime);
			asr.put("content", reason);
			if(isMcht) { //商户申请处理退款
				String oldAsr = order.getAftersalesResult()==null ? "[]" : order.getAftersalesResult();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(asr);
				updOdr.setAftersalesDealTime(currTime);
				updOdr.setAftersalesResult(asrArr.toJSONString());
			}else { //卖家申请退款
				String oldAsr = order.getAftersalesReason()==null ? "[]" : order.getAftersalesReason();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(asr);
				updOdr.setAftersalesApplyTime(currTime);
				updOdr.setAftersalesReason(asrArr.toJSONString());
			}
			this.orderMapper.updateByPrimaryKeySelective(updOdr);
		}
		//保存退款流水
		int cnt = this.payFlowMapper.insert(refundFlow);
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
	 * 添加买家对商家的评价或者系统自动超时评价
	 * @param order	订单信息
	 * @param scoreLogistics		物流得分
	 * @param scoreMerchant	商家服务得分
	 * @param scoreGoods		商品描述得分
	 * @param content	评价内容
	 * @return
	 */
	@Override
	public JSONObject appraise2Mcht(Order order,Integer scoreLogistics,Integer scoreMerchant,
			Integer scoreGoods,String content) {
		JSONObject jsonRet = new JSONObject();
		//更新订单信息
		Date currTime = new Date();
		Order updOdr = new Order();
		if(order.getStatus().equals("40")) {
			updOdr.setStatus("41"); //41:评价完成
		}else {
			updOdr.setStatus("56"); //56：评价完成（换货结束）
		}
		updOdr.setOrderId(order.getOrderId());
		updOdr.setAftersalesApplyTime(currTime);
		updOdr.setScoreGoods(scoreGoods);
		updOdr.setScoreLogistics(scoreLogistics);
		updOdr.setScoreMerchant(scoreMerchant);
		updOdr.setAppraiseTime(currTime);
		if(content != null && content.length()>1) {
			JSONObject asr = new JSONObject();
			asr.put("time", currTime);
			asr.put("content", content);
			String oldAsr = order.getAftersalesReason()==null ? "[]" : order.getAftersalesReason();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(asr);
			updOdr.setAppraiseInfo(asrArr.toJSONString());
		}
		int cnt = this.orderMapper.updateByPrimaryKeySelective(updOdr);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据出错！");
		}
		return jsonRet;
	}
	
	/**
	 * 添加卖家对买家的评价或者系统自动超时评价
	 * @param order	订单信息
	 * @param score	得分
	 * @param content	评价内容
	 * @return
	 */
	@Override
	public JSONObject appraise2User(Order order,Integer score,String content) {
		JSONObject jsonRet = new JSONObject();
		//更新订单信息
		Date currTime = new Date();
		Order updOdr = new Order();
		updOdr.setOrderId(order.getOrderId());
		updOdr.setScoreUser(score);
		updOdr.setApprUserTime(currTime);
		if(content != null && content.length()>1) {
			JSONObject asr = new JSONObject();
			asr.put("time", currTime);
			asr.put("content", content);
			String oldAsr = order.getAftersalesReason()==null ? "[]" : order.getAftersalesReason();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(asr);
			updOdr.setApprUser(asrArr.toJSONString());
		}
		int cnt = this.orderMapper.updateByPrimaryKeySelective(updOdr);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据出错！");
		}
		return jsonRet;
	}
	
	
}
