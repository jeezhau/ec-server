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
import com.mofangyouxuan.mapper.OrderBalMapper;
import com.mofangyouxuan.mapper.OrderMapper;
import com.mofangyouxuan.mapper.PayFlowMapper;
import com.mofangyouxuan.model.Aftersale;
import com.mofangyouxuan.model.GoodsSpec;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.OrderBal;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerSettle;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.pay.AliPay;
import com.mofangyouxuan.pay.WXPay;
import com.mofangyouxuan.service.AftersaleService;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.GoodsService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.CommonUtil;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderBalMapper orderBalMapper;
	@Autowired
	private PayFlowMapper payFlowMapper;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private ChangeFlowService changeFlowService;
	@Autowired
	private WXPay wXPay;
	@Autowired
	private AliPay aliPay;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private SysParamUtil sysParamUtil;
	@Autowired
	private AftersaleService aftersaleService;
	
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
		if(jsonParams.containsKey("upPartnerId")) {//查询指定上级合作伙伴
			params.put("upPartnerId", jsonParams.getInteger("upPartnerId"));
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
		if(jsonParams.containsKey("incart")) { //订单签收结束时间
			params.put("incart", jsonParams.getString("incart"));
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
	 * @throws Exception 
	 */
	public JSONObject cancelOrder(Order order,Integer userVipId,Integer mchtVipId,String reason) throws Exception {
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
			Aftersale aftersale = this.aftersaleService.getByID(order.getOrderId());
			if(aftersale == null) {
				aftersale = new Aftersale();
			}
			JSONArray asrArr = JSONArray.parseArray(aftersale.getApplyReason()==null ? "[]" : aftersale.getApplyReason());
			asrArr.add(asr);
			aftersale.setGoodsId(order.getGoodsId());
			aftersale.setOrderId(order.getOrderId());
			aftersale.setApplyReason(asrArr.toJSONString());
			int cnt = this.orderMapper.updateByPrimaryKeySelective(cancelOrder);
			if(cnt>0) {
				this.aftersaleService.saveAF(aftersale);
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg","订单已成功取消！");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg","数据库处理失败！");
			}
			return jsonRet;
		}
		if(!"11".equals(order.getStatus()) && !"20".equals(order.getStatus()) && !"DF".equals(order.getStatus())) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","您当前不可执行取消并退款！！");
			return jsonRet;
		}
		
		//查询已有最新支付流水单
		PayFlow oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),"1");
		if(oldFlow == null || !"11".equals(oldFlow.getStatus())) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","系统没有您的支付成功流水信息！");
			return jsonRet;
		}
		oldFlow = this.payFlowMapper.selectLastestFlow(order.getOrderId(),null);
		if(oldFlow == null || (!"11".equals(oldFlow.getStatus()) && !"F2".equals(oldFlow.getStatus()))) {
			jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
			jsonRet.put("errmsg","您的订单没有支付成功信息（未支付到账、支付失败或已退款）！");
			return jsonRet;
		}
		//退款申请
		return this.applyRefund(false,order, oldFlow, userVipId, mchtVipId, ctn);
	}
	
	/**
	 * 生成预支付订单
	 * 1、发送支付请求；
	 * 2、移出购物车
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
		Long totalAmount = null; 	//总实付金额
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
			fee = amount.multiply(feeRate).setScale(0, BigDecimal.ROUND_CEILING); //计算手续费,分
			totalAmount = amount.longValue() + fee.longValue();//支付金额
			JSONObject wxRet = wXPay.createPrePay(payType,order,flowId, totalAmount, user.getOpenId(), ip);
			if(wxRet.containsKey("prepay_id")) {//成功
				if(user.getOpenId() != null) {
					payAccount = user.getOpenId();
				}else {
					payAccount = userVip.getVipId() + "";
				}
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
		//移出购物车
		if("1".equals(order.getIncart())) {
			Order updOrder = new Order();
			updOrder.setIncart("0");
			this.orderMapper.updateByPrimaryKeySelective(updOrder);
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
	 * @throws Exception 
	 */
	@Override
	public void execPaySucc(boolean useBal,PayFlow oldPayFlow,Integer userVipId,Order order,Integer mchtVipId,String outFinishId) throws Exception {
		//添加现金余额流水
		this.changeFlowService.paySuccess(useBal, oldPayFlow.getPayAmount(), userVipId, 
				"商品购买【订单号:" + oldPayFlow.getOrderId() + "】", 
				oldPayFlow.getUserId(), mchtVipId,oldPayFlow.getOrderId());
		//更新会员积分
		this.vipBasicService.updScore(userVipId, (int)(oldPayFlow.getPayAmount()/100));
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
		newO.setStatus("20"); //支付成功(待发货)
		this.orderMapper.updateByPrimaryKeySelective(newO);
		
		//更新库存:减少
		//Order order = this.get(false, false, false, false, true, payFlow.getOrderId());
		List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
		this.goodsService.changeSP(order.getGoodsId(), buySpec, 3, null, null,1);
	}
	
	
	/**
	 * 外部支付成功
	 * 1、判断支付入账信息是否正确;
	 * 2、判断是否已经完成支付；
	 * @param payFlowId		支付流水号
	 * @param totalAmount	入账金额，分
	 * @param accountId		付款人账户
	 * @param outFinishId	外部支付单号
	 * @return
	 * @throws Exception 
	 */
	@Override
	public synchronized String outPaySucc(String payFlowId,Long totalAmount,String outFinishId) throws Exception {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该支付流水信息！";
		}
		Long oldTotal = payFlow.getFeeAmount() + payFlow.getPayAmount();
		if(oldTotal != totalAmount) {
			return "付款金额不正确！";
		}
		String stat = payFlow.getStatus();
		if("00".equals(stat) || "10".equals(stat) || "F1".equals(stat)) {
			Order order = this.get( payFlow.getOrderId());
			VipBasic vip = this.vipBasicService.get(payFlow.getUserId());
			this.execPaySucc(false, payFlow,vip.getVipId(), order, order.getMchtUId(),outFinishId);
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
		if("00".equals(stat) ||"10".equals(stat) || "F1".equals(stat)) {
			//更新支付流水
			PayFlow failFlow = new PayFlow();
			failFlow.setFlowId(payFlowId);
			failFlow.setStatus("F1");
			failFlow.setMemo(fail);
			this.payFlowMapper.updateByPrimaryKeySelective(failFlow);
			//更新订单
			Order order = this.get(payFlow.getOrderId());
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
	 * @throws Exception 
	 */
	public String closePay(String payFlowId,Long totalAmount,String outFinishId) throws Exception {
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
			Order order = this.get(payFlow.getOrderId());
			this.execRefundSucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), "退款成功", outFinishId);
		}
		return "00";
	}

	/**
	 * 客户端支付完成
	 * @param userVip
	 * @param order
	 * @return {errcode,errmsg,payType,payTime,amount,fee}
	 * @throws Exception 
	 */
	@Override
	public JSONObject payFinish(VipBasic userVip,Order order) throws Exception {
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
		BigDecimal allAmount = new BigDecimal(oldFlow.getPayAmount() + oldFlow.getFeeAmount()).divide(new BigDecimal(100)); //总金额，元
		String sysStat = oldFlow.getStatus();  //系统状态
		String payType = oldFlow.getPayType();	//支付方式
		if("00".equals(sysStat) || "10".equals(sysStat) || "1B".equals(sysStat)){
			Long curr = System.currentTimeMillis()/1000;//秒
			if((curr - oldFlow.getCreateTime().getTime()/1000) < 15) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "系统还未收到第三方支付平台给出的您的支付成功通知，请稍后再查看！！");
				return jsonRet;	
			}
			if(payType.startsWith("2")) {	//微信支付，执行订单查询
				JSONObject queryRet = this.wXPay.queryOrder(oldFlow.getFlowId());
				if(queryRet.containsKey("total_fee")) { //已经成功
					if(queryRet.getLong("total_fee") == oldFlow.getPayAmount() + oldFlow.getFeeAmount()) {
						if(!"1B".equals(sysStat)) {
							this.execPaySucc(false, oldFlow, userVip.getVipId(), order, order.getMchtUId(), queryRet.getString("transaction_id"));
						}
						this.balanceBill(false, queryRet.getString("transaction_id"), oldFlow.getFlowId(), payType,
								"SUCCESS", allAmount.toString(), "0");
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "支付成功！");
						return jsonRet;
					}
				}
			}else if(payType.startsWith("3")) { //支付宝支付
				JSONObject queryRet = this.aliPay.queryOrder(oldFlow.getFlowId());
				if(queryRet.containsKey("total_fee")) { //已经成功
					if(queryRet.getLong("total_fee") == oldFlow.getPayAmount() + oldFlow.getFeeAmount()) {
						if(!"1B".equals(sysStat)) {
							this.execPaySucc(false, oldFlow, userVip.getVipId(), order, order.getMchtUId(), queryRet.getString("transaction_id"));
						}
						this.balanceBill(false, queryRet.getString("transaction_id"), oldFlow.getFlowId(), payType,
								"SUCCESS", allAmount.toString(), "0");//手续费后记
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
		}else if("20".equals(sysStat) || "2B".equals(sysStat)){ //退款未到账
			Long curr = System.currentTimeMillis()/1000;
			if((curr - oldFlow.getCreateTime().getTime()/1000) < 15) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "系统还未收到第三方支付平台给出的您的退款成功通知，请稍后再查看！！");
				return jsonRet;	
			}
			if(payType.startsWith("2")) {
				JSONObject queryRet = this.wXPay.queryRefund(oldFlow.getFlowId());
				if(queryRet.containsKey("errcode")) { //已经成功
					if(queryRet.getLong("errcode") == 0) {
						if(!"2B".equals(sysStat)) {
							this.execRefundSucc(false, oldFlow, userVip.getVipId(), order, order.getMchtUId(), "退款成功", queryRet.getString("settlement_refund_fee"));
						}
						this.balanceBill(true, queryRet.getString("refund_id"), oldFlow.getFlowId(), payType,
								"SUCCESS", allAmount.toString(), "0");
						jsonRet.put("errcode", 0);
						jsonRet.put("errmsg", "退款成功！");
						return jsonRet;
					}
				}
			}else if(payType.startsWith("3")) { //支付宝支付
				JSONObject queryRet = this.aliPay.queryRefund(oldFlow.getFlowId(),oldFlow.getOutTradeNo());
				if(queryRet.containsKey("errcode")) { //已经成功
					if(queryRet.getLong("errcode") == 0) {
						if(!"2B".equals(sysStat)) {
							this.execRefundSucc(false, oldFlow, userVip.getVipId(), order, order.getMchtUId(), "退款成功", oldFlow.getOutTradeNo());
						}
						this.balanceBill(true, queryRet.getString("refund_id"), oldFlow.getFlowId(), payType,
								"SUCCESS", allAmount.toString(), "0");
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
	 * 订单退款申请
	 * 1、向第三方支付申请退款，或余额退款；
	 * 2、保存退款流水；
	 * 3、更新订单售后信息；
	 * @param isMcht		是否是商家申请退款
	 * @param order		订单信息
	 * @param payFlow	最近的支付或退款流水
	 * @param userVipId	买家会员账户
	 * @param mchtVipId	卖家会员ID
	 * @param type		退款类型 ：0-买家取消，3-买家申请，卖家同意
	 * @param reason		退款理由，对于收货退款，其中包含退款方式与快递信息
	 * @return
	 * @throws Exception 
	 */
	@Override
	public synchronized JSONObject applyRefund(boolean isMcht,Order order,PayFlow payFlow,Integer userVipId,Integer mchtVipId,JSONObject reason) throws Exception {
		JSONObject jsonRet = new JSONObject();
	
		String refundFlowId = CommonUtil.genPayFlowId(payFlow.getOrderId(), payFlow.getFlowId()); //退款流水ID
		Long totalAmount = payFlow.getPayAmount() + payFlow.getFeeAmount();//支付总金额，分
		Long refundAmount = null;	//退款金额
		PayFlow refundFlow = null;
		Date currTime = new Date();
		String changeReason = ""; //资金变更流水理由前缀
		if(!isMcht) {
			changeReason = "买家取消全额退款";	//手续费由买家承担
			refundAmount = payFlow.getPayAmount();
		}else { //卖家申请退款
			changeReason = "卖家申请全额退款";
			refundAmount = payFlow.getPayAmount() + payFlow.getFeeAmount(); //手续费由卖家承担
		}
		if("1".equals(payFlow.getPayType())) {
			//余额支付退款
			refundFlow = new PayFlow();
			refundFlow.setPayType("1");
		}else if(payFlow.getPayType().startsWith("2")){
			//微信退款
			JSONObject wxRet = this.wXPay.applyRefund(refundFlowId, totalAmount,refundAmount, payFlow.getOutFinishId());
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
			JSONObject aliRet = this.aliPay.applyRefund(refundFlowId, new BigDecimal(refundAmount).divide(new BigDecimal(100)), payFlow.getOutFinishId());
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
			Aftersale aftersale = this.aftersaleService.getByID(order.getOrderId());
			if(aftersale == null) {
				aftersale = new Aftersale();
			}
			aftersale.setOrderId(order.getOrderId());
			aftersale.setGoodsId(order.getGoodsId());
			if(isMcht) { //商户申请处理退款
				String oldAsr = aftersale.getDealResult()==null ? "[]" : aftersale.getDealResult();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(0,asr);
				updOrder.setStatus("65"); //65:同意退款，资金回退中
				aftersale.setDealResult(asrArr.toJSONString());
				aftersale.setDealTime(currTime);
				this.aftersaleService.updateAF(aftersale);
			}else { //买家申请退款
				String oldAsr = aftersale.getApplyReason()==null ? "[]" : aftersale.getApplyReason();
				JSONArray asrArr = JSONArray.parseArray(oldAsr);
				asrArr.add(0,asr);
				updOrder.setStatus("D0"); //D0:资金回退中
				aftersale.setApplyReason(asrArr.toJSONString());
				aftersale.setApplyTime(currTime);
				this.aftersaleService.saveAF(aftersale);
			}
			this.orderMapper.updateByPrimaryKeySelective(updOrder);
			
			//资金冻结
			this.changeFlowService.refundApply(payFlow.getPayAmount(), mchtVipId, changeReason + "【订单号："+order.getOrderId()+"】", mchtVipId,payFlow.getOrderId());
			if("1".equals(payFlow.getPayType())) {//余额退款，执行退款成功
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
	 * @throws Exception 
	 */
	@Override
	public synchronized String outRefundSucc(String payFlowId,Long totalAmount,String outFinishId) throws Exception {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该退款流水信息！";
		}
		Long oldTotal = payFlow.getPayAmount() + payFlow.getFeeAmount(); //退款金额
		if(oldTotal != totalAmount) {
			return "退款金额不正确！";
		}
		String stat = payFlow.getStatus();
		if("20".equals(stat) || "F2".equals(stat)) {
			Order order = this.get(payFlow.getOrderId());
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
	 * @param outFinishId	外部退款单号
	 * @param fail			失败信息
	 * @return
	 * @throws Exception 
	 */
	@Override
	public synchronized String outRefundFail(String payFlowId,String outFinishId,String fail) throws Exception {
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(payFlowId);
		if(payFlow == null) {
			return "系统中没有该退款流水信息！";
		}
		String stat = payFlow.getStatus();
		if("20".equals(stat) || "F2".equals(stat)) {
			//更新支付流水
			PayFlow failFlow = new PayFlow();
			failFlow.setFlowId(payFlowId);
			failFlow.setStatus("F2");
			failFlow.setMemo(fail);
			this.payFlowMapper.updateByPrimaryKeySelective(failFlow);
			//更新订单
			Order order = this.get(payFlow.getOrderId());
			Order updOrder = new Order();
			updOrder.setOrderId(order.getOrderId());
			if("65".equals(order.getStatus())) {
				updOrder.setStatus("67");
			}else {
				updOrder.setStatus("DF");
			}
			this.orderMapper.updateByPrimaryKeySelective(updOrder);
			//更新资金流水
			this.changeFlowService.refundFail(payFlow.getPayAmount(), order.getMchtUId(), "退款失败【订单号："+order.getOrderId()+"】", order.getMchtUId(),payFlow.getOrderId());
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
	 * @throws Exception 
	 */
	private void execRefundSucc(boolean useBal,PayFlow refundFlow,Integer userVipId,Order order,Integer mchtVipId,String changeReason,String outFinishId) throws Exception {
		Long totalAmount = refundFlow.getPayAmount() + refundFlow.getFeeAmount();
		//账户退款：更新用户与商家余额
		Date currTime = new Date();
		this.changeFlowService.refundSuccess(useBal, totalAmount, 
				userVipId, changeReason + "【订单号:" + order.getOrderId() +"】", mchtVipId, mchtVipId,refundFlow.getOrderId());
		//更新会员积分
		this.vipBasicService.updScore(userVipId, (int)(totalAmount/100));
		//更新退款流水
		PayFlow updFlow = new PayFlow();
		updFlow.setFlowId(refundFlow.getFlowId());
		updFlow.setStatus("21");		//退款成功
		updFlow.setIncomeAmount(refundFlow.getPayAmount());	//入账金额，分
		updFlow.setIncomeTime(currTime);
		updFlow.setOutFinishId(outFinishId);
		updFlow.setMemo("成功");
		this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
		//更新订单
		Order newO = new Order();
		newO.setOrderId(refundFlow.getOrderId());
		if("65".equals(order.getStatus())) {
			newO.setStatus("66"); //退款成功
		}else {
			newO.setStatus("DR"); //退款成功
		}
		this.orderMapper.updateByPrimaryKeySelective(newO);
		//更新库存:增加
		List<GoodsSpec> buySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
		this.goodsService.changeSP(order.getGoodsId(), buySpec, 2, null, null,1);
		
	}
	
	
	
	/**
	 * 根据对账单数据指定订单支付对账
	 * @param isRefund	是否为退款
	 * @param outTrdaeNo	外部单号
	 * @param flowId		系统支付流水号
	 * @param payType	支付方式
	 * @param status		外部交易状态
	 * @param amount		外部交易金额，元
	 * @param fee		外部手续费，元
	 * @return
	 * @throws Exception
	 */
	public void balanceBill(boolean isRefund,String outTradeNo,String flowId,
			String payType,String status,String amount,String fee) throws Exception {
		String orderId = flowId.substring(0, 30);
		OrderBal obal = this.orderBalMapper.selectByPrimaryKey(orderId);
		if(obal != null && "SS".equals(obal.getStatus()) ) {
			//return;
		}
		Order order = this.get(orderId);
		PayFlow payFlow = this.payFlowMapper.selectByPrimaryKey(flowId);
		BigDecimal feeRate = null;
		if(payType.startsWith("3")){
			feeRate = sysParamUtil.getAliFeeRate();
		}else if(payType.startsWith("2")){
			feeRate = sysParamUtil.getWxFeeRate();
		}
		boolean isBalOK = true; 	//是否对账成功
		if(payFlow == null) {//没有支付流水信息
			BigDecimal payFee = order.getAmount().multiply(feeRate).setScale(2, BigDecimal.ROUND_CEILING); //计算手续费,元
			BigDecimal needAllAmount = order.getAmount().add(payFee);	//用户最多应支付(退款)金额，元
			payFlow = new PayFlow();
			payFlow.setFlowId(flowId);
			payFlow.setMemo("系统无，微信有，对账插入");
			if(isRefund) {//退款
				payFlow.setFlowType("2");
				if(needAllAmount.compareTo(new BigDecimal(amount)) < 0) { //多退
					payFlow.setStatus("F2");//退款失败
					isBalOK = false;
					payFlow.setMemo((payFlow.getMemo()==null?"":payFlow.getMemo()) + "金额有误：系统到账与应付金额不一致；需要手工处理");
				}else {
					payFlow.setStatus("20");
				}
			}else {
				payFlow.setFlowType("1");
				if(needAllAmount.compareTo(new BigDecimal(amount)) > 0) {//少付
					payFlow.setStatus("F1");//支付失败
					isBalOK = false;
					payFlow.setMemo("金额有误：系统到账与应付金额不一致；需要手工处理");
				}else {
					payFlow.setStatus("10");
				}
			}
			payFlow.setGoodsId(order.getGoodsId());
			payFlow.setOrderId(order.getOrderId());
			payFlow.setPayAccount(order.getUserId()+"");
			payFlow.setPayAmount(order.getAmount().multiply(new BigDecimal(100)).longValue()); //订单额
			payFlow.setUserId(order.getUserId());
			payFlow.setPayType(payType);
			payFlow.setFeeAmount((new BigDecimal(amount).subtract(order.getAmount())).multiply(new BigDecimal(100)).longValue());
			payFlow.setCreateTime(new Date());
			payFlow.setCurrencyType("CNY");
			payFlow.setIncomeAmount(new BigDecimal(amount).multiply(new BigDecimal(100)).longValue());
			payFlow.setIncomeTime(new Date());
			payFlow.setOutFinishId(outTradeNo);
			this.payFlowMapper.insert(payFlow);
			if(isRefund) {
				if(!isBalOK) {//退款失败
					this.outRefundFail(flowId, outTradeNo, "退款失败，金额有误：系统到账与应付金额不一致；需要手工处理"); //退款失败
				}else {//退款成功
					this.outRefundSucc(flowId, new BigDecimal(amount).multiply(new BigDecimal(100)).longValue(), outTradeNo);
				}
			}else {
				if(!isBalOK) {//支付失败
					this.outPayFail(flowId, outTradeNo, "支付失败，金额有误：系统到账与应付金额不一致；需要手工处理"); //退款失败
				}else {//支付成功
					this.outPaySucc(flowId, new BigDecimal(amount).multiply(new BigDecimal(100)).longValue(), outTradeNo);
				}
			}
		}else {//已有支付流水
			//金额检查、状态检查
			BigDecimal totalAmount = new BigDecimal(payFlow.getPayAmount()).divide(new BigDecimal(100));
			totalAmount = totalAmount.add(new BigDecimal(payFlow.getFeeAmount()).divide(new BigDecimal(100)));
			if(totalAmount.compareTo(order.getAmount())<0 || totalAmount.compareTo(new BigDecimal(amount)) != 0) {
				isBalOK = false;
				if(isRefund) {//退款失败，金额有误
					this.outRefundFail(flowId, outTradeNo, "退款失败，金额有误：系统到账与应付金额不一致；需要手工处理	!");
				}else {//支付失败，金额有误
					this.outPayFail(flowId, outTradeNo, "支付失败，金额有误：系统到账与应付金额不一致；需要手工处理	!");
				}
			}else {//金额检查通过：更新状态
				if(isRefund) {//退款成功
					if("20".equals(payFlow.getStatus()) || "F2".equals(payFlow.getStatus()) ) {
						this.execRefundSucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), "退款成功", outTradeNo);
					}
				}else {//支付成功
					if("00".equals(payFlow.getStatus()) || "01".equals(payFlow.getStatus()) || "10".equals(payFlow.getStatus()) || 
							"F1".equals(payFlow.getStatus()) ) {
						this.execPaySucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), outTradeNo);
					}
				}
			}
		}
		//资金流水检查并修正状态
		payFlow = this.payFlowMapper.selectByPrimaryKey(flowId);//重新获取
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("flowType", "2");
		params.put("orderId", orderId);
		int cnt = this.payFlowMapper.countAll(params);
		String cfBalRet = this.changeFlowService.balPOrderFlow(orderId, order.getMchtUId(),payFlow.getPayAmount(),payFlow.getFeeAmount(),isRefund,cnt>0);
		if(!"00".equals(cfBalRet)) {//对账失败
			isBalOK = false;
			PayFlow updFlow = new PayFlow();
			updFlow.setFlowId(flowId);
			if(isRefund) {
				updFlow.setStatus("2B");
			}else {
				updFlow.setStatus("1B");
			}
			updFlow.setMemo((payFlow.getMemo()==null?"":payFlow.getMemo()) + cfBalRet);
			this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
		}else {
			if(payFlow.getStatus().contains("B")) {
				PayFlow updFlow = new PayFlow();
				updFlow.setFlowId(flowId);
				if(isRefund) {
					updFlow.setStatus("21");
				}else {
					updFlow.setStatus("11");
				}
				updFlow.setMemo("成功");
				this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
			}
		}
		//保存对账结果信息
		this.saveOrderBill(isRefund, order, payType, 
				new BigDecimal(amount), new BigDecimal(payFlow.getFeeAmount()).divide(new BigDecimal(100)),
				new BigDecimal(fee), isBalOK, payFlow.getIncomeTime());
	}
	
	/**
	 * 余额支付订单对账
	 * @param isRefund	是否为退款
	 * @param flow		系统支付流水
	 * @return
	 * @throws Exception
	 */
	public void balanceBill(boolean isRefund,PayFlow payFlow) throws Exception {
		String flowId = payFlow.getFlowId();
		String orderId = payFlow.getFlowId().substring(0, 30);
		OrderBal obal = this.orderBalMapper.selectByPrimaryKey(orderId);
		if(obal != null && "SS".equals(obal.getStatus()) ) {
			//return;
		}
		Order order = this.get(orderId);
		boolean isBalOK = true; 	//是否对账成功
		//用户应支付金额
		BigDecimal needAllAmount = order.getAmount();	//用户最多应支付(退款)金额，元
		//金额检查、状态检查
		BigDecimal totalAmount = new BigDecimal(payFlow.getPayAmount()).divide(new BigDecimal(100));//支付流水中的支付（退款）总额
		totalAmount = totalAmount.add(new BigDecimal(payFlow.getFeeAmount()).divide(new BigDecimal(100)));
		if(totalAmount.compareTo(order.getAmount()) < 0 || totalAmount.compareTo(needAllAmount)>0) {
			isBalOK = false;
			if(isRefund) {//退款失败，金额有误
				this.outRefundFail(flowId, null, "退款失败，金额有误：系统到账与应付金额不一致；需要手工处理	!");
			}else {//支付失败，金额有误
				this.outPayFail(flowId, null, "支付失败，金额有误：系统到账与应付金额不一致；需要手工处理	!");
			}
		}else {//金额检查通过：更新状态
			if(isRefund) {//退款成功
				if("20".equals(payFlow.getStatus()) || "F2".equals(payFlow.getStatus())) {
					this.execRefundSucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), "退款成功", null);
				}
			}else {//支付成功
				if("00".equals(payFlow.getStatus()) || "01".equals(payFlow.getStatus()) || "10".equals(payFlow.getStatus()) || "F1".equals(payFlow.getStatus())) {
					this.execPaySucc(false, payFlow, order.getUserId(), order, order.getMchtUId(), null);
				}
			}
		}
		//资金流水检查并修正状态
		payFlow = this.payFlowMapper.selectByPrimaryKey(flowId);//重新获取
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("flowType", "2");//是否有退款
		params.put("orderId", orderId);
		int cnt = this.payFlowMapper.countAll(params);
		String cfBalRet = this.changeFlowService.balVOrderFlow(orderId, order.getUserId(),order.getMchtUId(),payFlow.getPayAmount(),payFlow.getFeeAmount(),isRefund,cnt>0);
		if(!"00".equals(cfBalRet)) {//对账失败
			isBalOK = false;
			PayFlow updFlow = new PayFlow();
			updFlow.setFlowId(flowId);
			if(isRefund) {
				updFlow.setStatus("2B");
			}else {
				updFlow.setStatus("1B");
			}
			updFlow.setMemo((payFlow.getMemo()==null?"":payFlow.getMemo()) + cfBalRet);
			this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
		}else {
			if(payFlow.getStatus().contains("B")) {
				PayFlow updFlow = new PayFlow();
				updFlow.setFlowId(flowId);
				if(isRefund) {
					updFlow.setStatus("21");
				}else {
					updFlow.setStatus("11");
				}
				updFlow.setMemo("成功");
				this.payFlowMapper.updateByPrimaryKeySelective(updFlow);
			}
		}
		//保存对账结果信息
		this.saveOrderBill(isRefund, order, payFlow.getPayType(), 
				new BigDecimal(payFlow.getPayAmount()).add(new BigDecimal(payFlow.getFeeAmount())).divide(new BigDecimal(100)), new BigDecimal(payFlow.getFeeAmount()).divide(new BigDecimal(100)),
				new BigDecimal(payFlow.getFeeAmount()).divide(new BigDecimal(100)), isBalOK, payFlow.getIncomeTime());
	}
	
	/**
	 * 保存对账结果信息
	 * @param isRefund	知否为退款
	 * @param order
	 * @param payType	支付方式
	 * @param payAmount	用户支付／退款的总额，包含手续费
	 * @param payFee		用户支付的手续费
	 * @param toolsFee	支付工具收取的手续费
	 * @param isBalOK
	 * @param refundTime	退款时间
	 * @throws Exception
	 */
	private void saveOrderBill(boolean isRefund,Order order,String payType,BigDecimal payAmount,BigDecimal payFee,BigDecimal toolsFee,boolean isBalOK,Date refundTime) throws Exception {
		//保存对账结果
		OrderBal obal = this.orderBalMapper.selectByPrimaryKey(order.getOrderId());
		//分润数据
		UserBasic buyUser = this.userBasicService.get(order.getUserId());
		PartnerBasic partner = this.partnerBasicService.getByID(order.getPartnerId());
		PartnerSettle settle = this.partnerBasicService.getSettle(order.getPartnerId());
		PartnerBasic sysPartner = this.partnerBasicService.getByID(this.sysParamUtil.getSysPartnerId());
		if(sysPartner == null || buyUser == null || partner == null) {
			throw new Exception("获取用户或商家信息失败！");
		}
		BigDecimal spreadUserProfit = new BigDecimal(0);
		BigDecimal spreadPartnerProfit = new BigDecimal(0);
		BigDecimal sysSrvFee = new BigDecimal(0);
		if(!isRefund) {//非退款
			//服务费费率
			BigDecimal platformServiceFeeRate = this.sysParamUtil.getDefaultServiceFeeRatio();
			if(settle != null && settle.getServiceFeeRate() != null && 
					settle.getServiceFeeRate().compareTo(this.sysParamUtil.getSysLowestServiceFeeRate())>0 &&
					settle.getServiceFeeRate().compareTo(this.sysParamUtil.getSysHighestServiceFeeRate())<0) {
				platformServiceFeeRate = settle.getServiceFeeRate();
			}
			platformServiceFeeRate = platformServiceFeeRate.divide(new BigDecimal(100));//百分比换算
			sysSrvFee = platformServiceFeeRate.multiply(order.getAmount()).setScale(2, BigDecimal.ROUND_CEILING);//元
			//推广用户分润
			if(buyUser != null && buyUser.getSenceId()!=null) {
				VipBasic spreadVip = this.vipBasicService.get(buyUser.getSenceId());
				if(spreadVip != null) {
					BigDecimal spreadUserProfitRate = this.sysParamUtil.getSpreadUserProfitRatio();
					spreadUserProfitRate = spreadUserProfitRate.divide(new BigDecimal(100));
					spreadUserProfit = spreadUserProfitRate.multiply(order.getAmount()).setScale(2, BigDecimal.ROUND_FLOOR);//元
				}
			}
			//推广商家分润
			if(partner.getUpPartnerId() != null) {
				PartnerBasic spPartner = this.partnerBasicService.getByID(partner.getUpPartnerId());
				PartnerSettle spSettle = this.partnerBasicService.getSettle(partner.getUpPartnerId());
				if(spPartner != null) {
					BigDecimal spreadMchtProfitRate = this.sysParamUtil.getDefaultPartnerProfitRatio();
					if(spSettle != null && spSettle.getShareProfitRate() != null &&
							spSettle.getShareProfitRate().compareTo(new BigDecimal(0))>0 &&
							spSettle.getShareProfitRate().compareTo(new BigDecimal(70))<0) {
						spreadMchtProfitRate = spSettle.getShareProfitRate();
					}
					spreadMchtProfitRate = spreadMchtProfitRate.divide(new BigDecimal(100));//百分数换算
					spreadPartnerProfit = spreadMchtProfitRate.multiply(sysSrvFee).setScale(2, BigDecimal.ROUND_FLOOR);//元
				}
			}
		}
		if(obal == null) {
			obal = new OrderBal();
			obal.setBalTime(new Date());
			obal.setOrderId(order.getOrderId());
			obal.setPayType(payType);
			obal.setPartnerSettle(order.getAmount().subtract(sysSrvFee));
			obal.setPayAmount(payAmount);	//支付总额
			obal.setPayFee(payFee);	//用户支付的手续费
			obal.setPtoolsFee(toolsFee); //支付工具收取的手续费
			obal.setSpreaderPSettle(spreadPartnerProfit);
			obal.setSpreaderUSettle(spreadUserProfit);
			obal.setSyssrvSettle(sysSrvFee);
			if(isRefund) {//退款不退服务费
				obal.setRefundPartnerSettle(payAmount);//申请的退款金额
				obal.setRefundTime(refundTime);
				obal.setRefundUserSettle(payAmount);//申请的退款金额
				obal.setStatus("0");
			}else {
				obal.setStatus("S");
			}
			if(!isBalOK) {
				obal.setStatus("1");
			}
			this.orderBalMapper.insert(obal);
		}else {
			obal.setBalTime(new Date());
			if(isRefund) {//退款不退服务费
				obal.setRefundPartnerSettle(payAmount);
				obal.setRefundTime(refundTime);
				obal.setRefundUserSettle(payAmount);
			}else {
				obal.setPartnerSettle(order.getAmount().subtract(sysSrvFee));
				obal.setPayAmount(payAmount);
				obal.setPayFee(payFee);
				obal.setPtoolsFee(toolsFee);
				obal.setSpreaderPSettle(spreadPartnerProfit);
				obal.setSpreaderUSettle(spreadUserProfit);
				obal.setSyssrvSettle(sysSrvFee);
			}
			if(obal.getStatus().contains("S")) {
				obal.setStatus("SS");
			}else {
				obal.setStatus("S");
			}
			if(!isBalOK) {
				obal.setStatus("1");
			}
			this.orderBalMapper.updateByPrimaryKeySelective(obal);
		}
	}
	
	public List<PayFlow> getAllPayFlow(Map<String,Object> params,PageCond pageCond){
		return this.payFlowMapper.selectAll(params, pageCond);
	}
	
	public int countPayFlow(Map<String,Object> params) {
		return this.payFlowMapper.countAll(params);
	}
	
	public OrderBal getOBal(String orderId) {
		return this.orderBalMapper.selectByPrimaryKey(orderId);
	}
	
}
