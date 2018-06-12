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
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.mapper.PayFlowMapper;
import com.mofangyouxuan.mapper.OrderMapper;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.model.GoodsSpec;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.pay.AliPay;
import com.mofangyouxuan.pay.WXPay;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.GoodsService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.CommonUtil;

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
	@Autowired
	private AliPay aliPay;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private SysParamUtil sysParamUtil;
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
	 * 获取指定订单的最新支付流水
	 * @param orderId
	 * @param flowType 流水类型：1-支付，2-退款,可为空
	 * @return
	 */
	@Override
	public PayFlow getLastedFlow(String orderId,String flowType) {
		return this.payFlowMapper.selectLastestFlow(orderId,flowType);
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
		JSONObject ctn = new JSONObject();
		ctn.put("reason", reason);
		//未支付直接取消
		if("10".equals(order.getStatus()) || "12".equals(order.getStatus())) {
			Order cancelOrder = new Order();
			cancelOrder.setOrderId(order.getOrderId());
			cancelOrder.setStatus("DS");
			JSONObject asr = new JSONObject();
			Date currTime = new Date();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
			asr.put("type", "申请取消");
			asr.put("content", ctn);
			String oldAsr = order.getAftersalesReason()==null ? "[]" : order.getAftersalesReason();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(asr);
			cancelOrder.setAftersalesApplyTime(currTime);
			cancelOrder.setAftersalesReason(asrArr.toJSONString());
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
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),null);
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
		//退款申请
		return this.applyRefund(false,order, oldFlow, userVipId, mchtVipId, ctn);
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
	 * @return {errcode,errmsg,payType,prepay_id,outPayUrl,total,AliPayForm}
	 */
	public JSONObject createPrePay(UserBasic user,VipBasic userVip,Order order,Integer mchtVipId,String payType,String ip) {
		JSONObject jsonRet = new JSONObject();
		//查询已有最新支付流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),null);
		BigDecimal amount = order.getAmount().multiply(new BigDecimal(100)).setScale(0);//订单金额，分
		BigDecimal fee = new BigDecimal(0);
		String outTradeNo = null;	//外部系统的预付单号
		String outPayUrl = null;		//外部支付须调起的支付页面
		String payAccount = null;	//支付账户
		String flowId = null;		//支付流水ID
		Long totalAmount = null; //总实付金额
		if(oldFlow != null) {
			if(!"00".equals(oldFlow.getStatus()) && !"01".equals(oldFlow.getStatus()) && !"F1".equals(oldFlow.getStatus())) {//非待支付或支付失败
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "支付预付单生成失败：订单已在支付或退款！");
				return jsonRet;
			}else if("00".equals(oldFlow.getStatus())) {//待支付
				Long t = (new Date().getTime()-oldFlow.getCreateTime().getTime())/1000/3600;
				if(t<=1 && payType.equals(oldFlow.getPayType())) {//同支付类型支付未超时1h
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
					jsonRet.put("payType", payType);
					if(payType.startsWith("2")) {
						jsonRet.put("prepay_id", oldFlow.getOutTradeNo());
						jsonRet.put("outPayUrl", oldFlow.getOutTradeUrl());
					}else if(payType.startsWith("3")) { //支付宝支付
						BigDecimal feeRate = sysParamUtil.getAliFeeRate();
						fee = amount.multiply(feeRate).setScale(0, BigDecimal.ROUND_CEILING); //计算手续费
						totalAmount = amount.longValue() + fee.longValue();//支付金额
						jsonRet = this.aliPay.createPayApply(payType, order, oldFlow.getFlowId(), new BigDecimal(totalAmount).divide(new BigDecimal(100)));
					}
					return jsonRet;
				}else {//超时未支付关闭
					oldFlow.setStatus("01");
					this.payFlowMapper.updateByPrimaryKeySelective(oldFlow);
				}
			}
		}
		//新申请预付单
		String oldFlowId = oldFlow == null ? null : oldFlow.getFlowId();
		flowId = CommonUtil.genPayFlowId(order.getOrderId(), oldFlowId); //支付流水单号
		//向微信申请预付单
		if(payType.startsWith("2")) {
			BigDecimal feeRate = sysParamUtil.getWxFeeRate();
			fee = amount.multiply(feeRate).setScale(0, BigDecimal.ROUND_CEILING); //计算手续费
			totalAmount = amount.longValue() + fee.longValue();//支付金额
			JSONObject wxRet = wXPay.createPrePay(payType,order,flowId, totalAmount, user.getOpenId(), ip);
			if(wxRet.containsKey("prepay_id")) {//成功
				payAccount = user.getOpenId();
				outTradeNo = wxRet.getString("prepay_id");
				if(wxRet.getString("code_url") != null) {  //扫码支付
					outPayUrl = wxRet.getString("code_url");
				}
				if(wxRet.getString("mweb_url") != null) {  //h5
					outPayUrl = wxRet.getString("mweb_url");
				}
				if(wxRet.containsKey("payFlowId")) {
					flowId = wxRet.getString("payFlowId");
				}
			}else {//失败
				return wxRet;
			}
		}else if("1".equals(payType)){
			if(userVip.getBalance() < amount.longValue()) {//判断余额是否够支付
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "余额支付失败，余额不足！");
				return jsonRet;
			}else {
				fee = new BigDecimal(0);		//余额支付手续费，分
				totalAmount = amount.longValue() + fee.longValue();//总支付金额，分
				payAccount = userVip.getVipId() + "";
				outTradeNo = null;
			}
		}else if(payType.startsWith("3")) {//支付宝支付
			BigDecimal feeRate = sysParamUtil.getAliFeeRate();
			fee = amount.multiply(feeRate).setScale(0, BigDecimal.ROUND_CEILING); //计算手续费
			totalAmount = amount.longValue() + fee.longValue();	//支付金额
			jsonRet = this.aliPay.createPayApply(payType, order, flowId, new BigDecimal(totalAmount).divide(new BigDecimal(100)));
			if(!jsonRet.containsKey("AliPayForm")) {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "调用支付宝支付失败！");
				return jsonRet;
			}
			payAccount = userVip.getVipId() + "";
			outTradeNo = jsonRet.getString("tradeNo");
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
		newFlow.setOutTradeUrl(outPayUrl);
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
			jsonRet.put("outPayUrl", outPayUrl);
			jsonRet.put("total", totalAmount); //实付：分
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据保存出错！");
		}
		return jsonRet;
	}
	
	/**
	 * 执行支付成功
	 * @param useBal
	 * @param payFlow
	 * @param userVip
	 * @param order
	 * @param mchtVipId
	 */
	@Override
	public void execPaySucc(boolean useBal,PayFlow oldPayFlow,VipBasic userVip,Order order,Integer mchtVipId,String outFinishId) {
		//添加现金余额流水
		this.changeFlowService.paySuccess(useBal, oldPayFlow.getPayAmount(), userVip, 
				"商品购买【订单号:" + oldPayFlow.getOrderId() + "】", 
				oldPayFlow.getUserId(), mchtVipId);
		//更新会员积分
		this.vipBasicService.updScore(userVip.getVipId(), (int)(oldPayFlow.getPayAmount()/100));
		//更新支付流水
		PayFlow updPayFow = new PayFlow();
		updPayFow.setFlowId(oldPayFlow.getFlowId());
		updPayFow.setStatus("11");	//支付完成
		updPayFow.setIncomeAmount(oldPayFlow.getPayAmount());//入账金额，分
		updPayFow.setIncomeTime(new Date());
		updPayFow.setOutFinishId(outFinishId);
		this.payFlowMapper.updateByPrimaryKeySelective(updPayFow);
		//更新订单
		Order newO = new Order();
		newO.setOrderId(oldPayFlow.getOrderId());
		newO.setStatus("20"); //支付成功，待发货
		this.orderMapper.updateByPrimaryKeySelective(newO);
		
		//更新库存:减少
		//Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
		List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
		this.goodsService.changeSpec(order.getGoodsId(), buySpec, 3, null, null,1);
	}
	
	
	/**
	 * 外部支付成功
	 * 1、判断支付入账信息是否正确;
	 * 2、判断是否已经完成支付；
	 * @param payFlowId		支付流水号
	 * @param totalAmount	入账金额
	 * @param accountId		付款人账户
	 * @param outFinishId	外部支付单号
	 * @return
	 */
	@Override
	public synchronized String outPaySucc(String payFlowId,Long totalAmount,String outFinishId) {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该支付流水信息！";
		}
		Long oldTotal = payFlow.getFeeAmount() + payFlow.getPayAmount();
		if(oldTotal != totalAmount) {
			return "付款金额不正确！";
		}
		String stat = payFlow.getStatus();
		if("00".equals(stat) || "10".equals(stat)) {
			Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
			VipBasic vip = this.vipBasicService.get(payFlow.getUserId());
			this.execPaySucc(false, payFlow,vip, order, order.getMchtUId(),outFinishId);
		}else if(!"11".equals(stat)) {
			return "订单支付状态有误！";
		}
		return "00";
	}

	/**
	 * 外部支付失败
	 * @param payFlowId		支付流水号
	 * @param accountId		付款人账户
	 * @param fail			失败信息
	 * @return
	 */
	@Override
	public synchronized String outPayFail(String payFlowId,String outFinishId,String fail) {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该支付流水信息！";
		}
		String stat = payFlow.getStatus();
		if("00".equals(stat) ||"10".equals(stat)) {
			//更新支付流水
			PayFlow failFlow = new PayFlow();
			failFlow.setFlowId(payFlowId);
			failFlow.setStatus("F1");
			failFlow.setMemo(fail);
			this.payFlowMapper.updateByPrimaryKeySelective(failFlow);
			//更新订单
			Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
			Order updOrder = new Order();
			updOrder.setOrderId(order.getOrderId());
			updOrder.setStatus("12");
			this.orderMapper.updateByPrimaryKeySelective(updOrder);
			
			return "00";
		}
		return "订单支付状态有误！";
	}
	
	
	/**
	 * 关闭支付
	 * 1、支付超时；
	 * 2、退款完成；
	 * @param payFlowId
	 * @param totalAmount
	 * @param outFinishId
	 * @return
	 */
	public String closePay(String payFlowId,Long totalAmount,String outFinishId) {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该支付流水信息！";
		}
		Long oldTotal = payFlow.getFeeAmount() + payFlow.getPayAmount();
		if(oldTotal != totalAmount) {
			return "付款金额不正确！";
		}
		String stat = payFlow.getStatus();
		PayFlow updFlow = new PayFlow();
		updFlow.setFlowId(payFlowId);
		if("00".equals(stat)) { //支付超时
			updFlow.setStatus("01");
			this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
		}else if("20".equals(stat)) { //退款成功
			Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
			this.execRefundSucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), "退款成功", outFinishId);
		}
		return "00";
	}

	/**
	 * 客户端支付完成
	 * @param userVip
	 * @param order
	 * @return {errcode,errmsg,payType,payTime,amount,fee}
	 */
	@Override
	public JSONObject payFinish(VipBasic userVip,Order order) {
		JSONObject jsonRet = new JSONObject();
		//查询已有最新支付、退款流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),null);
		if(oldFlow == null) {
			jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
			jsonRet.put("errmsg", "系统中没有您的支付记录！");
			return jsonRet;
		}
		jsonRet.put("payType", oldFlow.getPayType());
		jsonRet.put("payTime", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(oldFlow.getCreateTime()));
		jsonRet.put("amount", oldFlow.getPayAmount()/100.0);
		jsonRet.put("fee", oldFlow.getFeeAmount()/100.0);
		String sysStat = oldFlow.getStatus();  //系统状态
		String payType = oldFlow.getPayType();	//支付方式
		if("00".equals(sysStat) || "10".equals(sysStat)){
			Long curr = System.currentTimeMillis()/1000;//秒
			if(curr - oldFlow.getCreateTime().getTime()/1000 < 15) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "系统还未收到第三方支付平台给出的您的支付成功通知，请稍后再查看！！");
				return jsonRet;	
			}
			if(payType.startsWith("2")) {	//微信支付，执行订单查询
				JSONObject queryRet = this.wXPay.queryOrder(oldFlow.getFlowId());
				if(queryRet.containsKey("total_fee")) { //已经成功
					if(queryRet.getLong("total_fee") == oldFlow.getPayAmount() + oldFlow.getFeeAmount()) {
						this.execPaySucc(false, oldFlow, userVip, order, order.getMchtUId(), queryRet.getString("transaction_id"));
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "支付成功！");
						return jsonRet;
					}
				}
			}else if(payType.startsWith("3")) { //支付宝支付
				JSONObject queryRet = this.aliPay.queryOrder(oldFlow.getFlowId());
				if(queryRet.containsKey("total_fee")) { //已经成功
					if(queryRet.getLong("total_fee") == oldFlow.getPayAmount() + oldFlow.getFeeAmount()) {
						this.execPaySucc(false, oldFlow, userVip, order, order.getMchtUId(), queryRet.getString("transaction_id"));
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "支付成功！");
						return jsonRet;
					}
				}
			}
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg", "系统还未收到第三方支付平台给出的您的支付成功通知，请稍后再查看！！");
			return jsonRet;	
		}else if("11".equals(sysStat)) {//后端成功
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "支付成功！");
		}else if("20".equals(sysStat)){ //退款未到账
			Long curr = System.currentTimeMillis()/1000;
			if(curr - oldFlow.getCreateTime().getTime()/1000 > 15) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "系统还未收到第三方支付平台给出的您的退款成功通知，请稍后再查看！！");
				return jsonRet;	
			}
			if(payType.startsWith("2")) {
				JSONObject queryRet = this.wXPay.queryRefund(oldFlow.getFlowId());
				if(queryRet.containsKey("refund_fee")) { //已经成功
					if(queryRet.getLong("refund_fee") == oldFlow.getPayAmount()) {
						this.execRefundSucc(false, oldFlow, userVip.getVipId(), order, order.getMchtUId(), "退款成功", queryRet.getString("settlement_refund_fee"));
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "退款成功！");
						return jsonRet;
					}
				}
			}else if(payType.startsWith("3")) { //支付宝支付
				JSONObject queryRet = this.aliPay.queryRefund(oldFlow.getFlowId(),oldFlow.getOutTradeNo());
				if(queryRet.containsKey("errcode")) { //已经成功
					if(queryRet.getLong("errcode") == 0) {
						this.execRefundSucc(false, oldFlow, userVip.getVipId(), order, order.getMchtUId(), "退款成功", oldFlow.getOutTradeNo());
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "退款成功！");
						return jsonRet;
					}
				}
			}
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg", "系统还未收到第三方支付平台给出的您的退款成功通知，请稍后再查看！！");
			return jsonRet;	
		}else if("21".equals(sysStat)) {	//退款成功
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "退款成功！");
		}else {
			jsonRet.put("errcode", ErrCodes.ORDER_PAY_ERROR);
			jsonRet.put("errmsg", "该订单当前状态不在正常支付流程中(可能已经失败)！");
		}
		return jsonRet;
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
	 * @param type		退款类型 ：0-买家取消，3-买家申请，卖家同意
	 * @param reason		退款理由，对于收货退款，其中包含退款方式与快递信息
	 * @return
	 */
	@Override
	public JSONObject applyRefund(boolean isMcht,Order order,PayFlow payFlow,Integer userVipId,Integer mchtVipId,JSONObject reason) {
		JSONObject jsonRet = new JSONObject();
	
		String refundFlowId = CommonUtil.genPayFlowId(payFlow.getOrderId(), payFlow.getFlowId()); //退款流水ID
		Long totalAmount = payFlow.getPayAmount();//退款总金额，分
		PayFlow refundFlow = null;
		Date currTime = new Date();
		String changeReason = ""; //资金变更流水理由前缀
		if(!isMcht) {
			changeReason = "买家取消全额退款";	//手续费由买家承担
		}else { //卖家申请退款
			changeReason = "卖家申请全额退款";
			totalAmount = payFlow.getPayAmount() + payFlow.getFeeAmount(); //手续费由卖家承担
		}
		if("1".equals(payFlow.getPayType())) {
			//余额支付
			refundFlow = new PayFlow();
			refundFlow.setPayType("1");
		}else if(payFlow.getPayType().startsWith("2")){
			//微信退款
			JSONObject wxRet = this.wXPay.applyRefund(refundFlowId, totalAmount, payFlow.getOutFinishId());
			if(wxRet.containsKey("refund_id")) {//申请成功
				refundFlow = new PayFlow();
				refundFlow.setPayType(payFlow.getPayType());
				refundFlow.setOutTradeNo(wxRet.getString("refund_id"));
			}else {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg","您的退款申请发送至微信退款失败，请稍后再试！");
				return jsonRet;
			}
		}else if(payFlow.getPayType().startsWith("3")) {
			//支付宝退款
			JSONObject aliRet = this.aliPay.applyRefund(refundFlowId, new BigDecimal(totalAmount).divide(new BigDecimal(100)), payFlow.getOutFinishId());
			if(aliRet.containsKey("refund_id")) {//申请成功
				refundFlow = new PayFlow();
				refundFlow.setPayType(payFlow.getPayType());
				refundFlow.setOutTradeNo(aliRet.getString("refund_id"));
			}else {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg","您的退款申请发送至支付宝退款失败，请稍后再试！");
				return jsonRet;
			}
		}
		
		if(refundFlow != null) {//退款流水
			refundFlow.setStatus("20");
			refundFlow.setCreateTime(currTime);
			refundFlow.setCurrencyType("CNY");
			if(!isMcht) { //买家承担手续费
				refundFlow.setFeeAmount(0l); 
			}else { //卖家承担手续费
				refundFlow.setFeeAmount(payFlow.getFeeAmount());
			}
			refundFlow.setFlowId(refundFlowId);
			refundFlow.setFlowType("2");
			refundFlow.setGoodsId(order.getGoodsId());
			refundFlow.setOrderId(order.getOrderId());
			refundFlow.setPayAccount(payFlow.getPayAccount());
			refundFlow.setPayAmount(payFlow.getPayAmount()); //退款金额：订单额
			refundFlow.setUserId(payFlow.getUserId());
			this.payFlowMapper.insert(refundFlow);
			
			Order updOrder = new Order();
			updOrder.setOrderId(order.getOrderId());
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
			asr.put("type", changeReason);
			asr.put("content", reason);
			if(isMcht) { //商户申请处理退款
				String oldAsr = order.getAftersalesResult()==null ? "[]" : order.getAftersalesResult();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(0,asr);
				updOrder.setStatus("65"); //65:同意退款，资金回退中
				updOrder.setAftersalesDealTime(currTime);
				updOrder.setAftersalesResult(asrArr.toJSONString());
			}else { //买家申请退款
				String oldAsr = order.getAftersalesReason()==null ? "[]" : order.getAftersalesReason();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(0,asr);
				updOrder.setStatus("D0"); //D0:资金回退中
				updOrder.setAftersalesApplyTime(currTime);
				updOrder.setAftersalesReason(asrArr.toJSONString());
			}
			this.orderMapper.updateByPrimaryKeySelective(updOrder);
			//冻结卖家
			this.changeFlowService.refundApply(payFlow.getPayAmount(), mchtVipId, changeReason, mchtVipId);
			if("1".equals(payFlow.getPayType())) {//余额支付，执行退款成功
				this.execRefundSucc(true, refundFlow, userVipId, updOrder, mchtVipId, changeReason, null);
			}
		}
		
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		
		return jsonRet;
	}

	/**
	 * 外部退款成功
	 * 1、判断支付入账信息是否正确;
	 * 2、判断是否已经完成支付；
	 * @param payFlowId		退款流水号
	 * @param totalAmount	退款金额
	 * @param outFinishId	外部退款单号
	 * @return
	 */
	@Override
	public synchronized String outRefundSucc(String payFlowId,Long totalAmount,String outFinishId) {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该退款流水信息！";
		}
		Long oldTotal = payFlow.getFeeAmount() + payFlow.getFeeAmount(); //退款金额
		if(oldTotal != totalAmount) {
			return "退款金额不正确！";
		}
		String stat = payFlow.getStatus();
		if("20".equals(stat)) {
			Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
			this.execRefundSucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), "退款成功", outFinishId);
		}else if(!"21".equals(stat)) {
			return "订单退款状态有误！";
		}
		return "00";
	}

	/**
	 * 外部退款失败
	 * 
	 * @param payFlowId		退款流水号
	 * @param accountId		退款人账户
	 * @param fail			失败信息
	 * @return
	 */
	@Override
	public synchronized String outRefundFail(String payFlowId,String outFinishId,String fail) {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该退款流水信息！";
		}
		String stat = payFlow.getStatus();
		if("20".equals(stat)) {
			//更新支付流水
			PayFlow failFlow = new PayFlow();
			failFlow.setFlowId(payFlowId);
			failFlow.setStatus("F2");
			failFlow.setMemo(fail);
			this.payFlowMapper.updateByPrimaryKeySelective(failFlow);
			//更新订单
			Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
			Order updOrder = new Order();
			updOrder.setOrderId(order.getOrderId());
			if("65".equals(order.getStatus())) {
				updOrder.setStatus("67");
			}else {
				updOrder.setStatus("DF");
			}
			this.orderMapper.updateByPrimaryKeySelective(updOrder);
			//更新资金流水
			this.changeFlowService.refundFail(payFlow.getPayAmount(), order.getMchtUId(), fail, order.getMchtUId());
			return "00";
		}
		return "订单退款状态有误！";
	}

	/**
	 * 执行退款成功
	 * @param useBal		是否使用余额支付
	 * @param refundFlow	退款流水
	 * @param userVip	用户VIP
	 * @param order		订单信息
	 * @param mchtVipId	商家VIP
	 * @param outFinishId 外部系统的退款单号
	 */
	private void execRefundSucc(boolean useBal,PayFlow refundFlow,Integer userVipId,Order order,Integer mchtVipId,String changeReason,String outFinishId) {
		Long totalAmount = refundFlow.getPayAmount() + refundFlow.getFeeAmount();
		//账户退款：更新用户与商家余额
		Date currTime = new Date();
		this.changeFlowService.refundSuccess(useBal, totalAmount, 
				userVipId, changeReason + "【订单号:" + order.getOrderId() +"】", mchtVipId, mchtVipId);
		//更新会员积分
		this.vipBasicService.updScore(userVipId, (int)(totalAmount/100));
		//更新退款流水
		PayFlow updFlow = new PayFlow();
		updFlow.setFlowId(refundFlow.getFlowId());
		updFlow.setStatus("21");		//退款成功
		updFlow.setIncomeAmount(refundFlow.getPayAmount());	//入账金额，分
		updFlow.setIncomeTime(currTime);
		updFlow.setOutFinishId(outFinishId);
		this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
		//更新订单
		Order newO = new Order();
		newO.setOrderId(refundFlow.getOrderId());
		if("65".equals(order.getStatus())) {
			newO.setStatus("66"); //退款成功
		}else {
			newO.setStatus("DS"); //退款成功
		}
		this.orderMapper.updateByPrimaryKeySelective(newO);
		//更新库存:增加
		List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
		this.goodsService.changeSpec(order.getGoodsId(), buySpec, 2, null, null,1);
		
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
		if("30".equals(order.getStatus()) || "31".equals(order.getStatus()) || "40".equals(order.getStatus()) || "41".equals(order.getStatus())) {
			updOdr.setStatus("41"); //41:评价完成
		}else {
			updOdr.setStatus("56"); //56：评价完成（换货结束）
		}
		if("30".equals(order.getStatus()) || "31".equals(order.getStatus()) || "54".equals(order.getStatus()) || "55".equals(order.getStatus())){
			updOdr.setSignTime(currTime);
			updOdr.setSignUser(order.getNickname());
		}
		if(updOdr.getAppraiseStatus() == null || "0".equals(updOdr.getAppraiseStatus())) {
			updOdr.setAppraiseStatus("1");
		}
		updOdr.setOrderId(order.getOrderId());
		updOdr.setScoreGoods(scoreGoods);
		updOdr.setScoreLogistics(scoreLogistics);
		updOdr.setScoreMerchant(scoreMerchant);
		updOdr.setAppraiseTime(currTime);
		if(content != null && content.length()>1) {
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
			asr.put("content", content);
			String oldAsr = order.getAppraiseInfo()==null ? "[]" : order.getAppraiseInfo();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(0, asr);
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
	public JSONObject appraise2User(Order order,Integer score,String content,Integer updateOpr) {
		JSONObject jsonRet = new JSONObject();
		//更新订单信息
		Date currTime = new Date();
		Order updOdr = new Order();
		updOdr.setOrderId(order.getOrderId());
		updOdr.setScoreUser(score);
		updOdr.setApprUserTime(currTime);
		if(updOdr.getAppraiseStatus() == null || "0".equals(updOdr.getAppraiseStatus())) {
			updOdr.setAppraiseStatus("1");
		}
		if(content != null && content.length()>1) {
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
			asr.put("operator", updateOpr);
			asr.put("content", content);
			String oldAsr = order.getApprUser()==null ? "[]" : order.getApprUser();
			JSONArray asrArr = JSONArray.parseArray(oldAsr);
			asrArr.add(0,asr);
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
