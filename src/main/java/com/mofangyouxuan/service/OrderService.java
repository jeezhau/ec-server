package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.PayFlow;
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
	 * @param params 需要显示哪些分类字段：needReceiver,needLogistics,needAppr,needAfterSales,needGoodsAndUser
     * @param orderId
	 * @return
	 */
	public Order get(Boolean needReceiver,Boolean needLogistics,Boolean needAppr,Boolean needAfterSales,Boolean needGoodsAndUser,String orderId);
	
	/**
	 * 根据指定查询条件订单数量
	 * @param params
	 * @return
	 */
	public int countAll(JSONObject jsonParams);
	
	/**
	 * 根据指定查询条件、排序条件、分页信息获取订单信息
	 * @param jsonShowGroups	需要显示的字段分组
	 * @param jsonParams
	 * @param jsonSorts
	 * @param pageCond
	 * @return
	 */
	public List<Order> getAll(JSONObject jsonShowGroups,JSONObject jsonParams,JSONObject jsonSorts,PageCond pageCond);
	
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
	 * @param userVipId	买家会员账户ID
	 * @param mchtVipId	卖家会员账户ID
	 * @param reason		取消理由
	 * @return
	 * @throws Exception 
	 */
	public JSONObject cancelOrder(Order order,Integer userVipId,Integer mchtVipId,String reason) throws Exception ;
	
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
	public JSONObject createPrePay(UserBasic user,VipBasic userVip,Order order,Integer mchtVipId,String payType,String ip);
	
	/**
	 * 执行支付处理
	 * @param useBal		是否使用余额支付
	 * @param payFlow
	 * @param userVipId
	 * @param order
	 * @param mchtVipId
	 * @throws Exception 
	 */
	public void execPaySucc(boolean useBal,PayFlow payFlow,Integer userVipId,Order order,Integer mchtVipId,String outFinishId) throws Exception;
	
	/**
	 * 客户端支付完成
	 * @param userVip
	 * @param order
	 * @return
	 * @throws Exception 
	 */
	public JSONObject payFinish(VipBasic userVip,Order order) throws Exception ;
	
	String outPaySucc(String payFlowId, Long totalAmount, String outFinishId) throws Exception;

	String outPayFail(String payFlowId, String outFinishId, String fail);

	/**
	 * 获取指定订单的最新支付流水
	 * @param orderId
	 * @param flowType 流水类型：1-支付，2-退款
	 * @return
	 */
	public PayFlow getLastedFlow(String orderId,String flowType) ;
	
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
	 * 订单退款申请
	 * 1、向第三方支付申请退款，或余额退款；
	 * 2、保存退款流水；
	 * 3、更新订单售后信息；
	 * @param isMcht		是否是商家申请退款
	 * @param order		订单信息
	 * @param payFlow	支付流水
	 * @param userVipId	买家会员账户ID
	 * @param mchtVipId	卖家会员ID
	 * @param reason		退款理由，对于收货退款，其中包含退款方式与快递信息
	 * @return
	 * @throws Exception 
	 */
	public JSONObject applyRefund(boolean isMcht,Order order,PayFlow payFlow,Integer userVipId,Integer mchtVipId,JSONObject reason) throws Exception ;

	String outRefundSucc(String payFlowId, Long totalAmount, String outFinishId) throws Exception;

	String outRefundFail(String payFlowId,  String outFinishId, String fail) throws Exception;

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
	public String closePay(String payFlowId,Long totalAmount,String outFinishId) throws Exception;

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
			String payType,String status,String amount,String fee) throws Exception;
	/**
	 * 余额支付订单对账
	 * @param isRefund	是否为退款
	 * @param flow		系统支付流水
	 * @return
	 * @throws Exception
	 */
	public void balanceBill(boolean isRefund,PayFlow payFlow) throws Exception;
	
	public List<PayFlow> getAllPayFlow(Map<String,Object> params,PageCond pageCond);
	
	public int countPayFlow(Map<String,Object> params);
	
}


