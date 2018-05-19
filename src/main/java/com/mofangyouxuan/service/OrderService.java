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
	 */
	public JSONObject cancelOrder(Order order,Integer userVipId,Integer mchtVipId,String reason) ;
	
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
	
	/**
	 * 提交余额支付
	 * @param payFlow
	 * @param userVip
	 * @param order
	 * @param mchtVipId
	 */
	public void submitBalPay(PayFlow payFlow,VipBasic userVip,Order order,Integer mchtVipId);
	
	/**
	 * 客户端支付完成
	 * @param user
	 * @param order
	 * @param clientStatus
	 * @return
	 */
	public JSONObject payFinish(UserBasic user,Order order,String clientStatus);
	
	/**
	 * 获取指定订单的最新支付流水
	 * @param orderId
	 * @param flowType 流水类型：1-支付，2-退款
	 * @return
	 */
	public PayFlow getLastedFlow(String orderId,String flowType) ;
	
	/**
	 * 订单退款执行
	 * 1、向第三方支付申请退款，或余额退款；
	 * 2、保存退款流水；
	 * 3、更新订单售后信息；
	 * @param isMcht		是否是商家申请退款
	 * @param order		订单信息
	 * @param payFlow	支付流水
	 * @param userVipId	买家会员账户ID
	 * @param mchtVipId	卖家会员ID
	 * @param type		退款类型 ：0-买家取消，1-卖家未发货，2-买家未收到货，3-买家收货后申请卖家同意，4-其他
	 * @param reason		退款理由，对于收货退款，其中包含退款方式与快递信息
	 * @return
	 */
	public JSONObject execRefund(boolean isMcht,Order order,PayFlow payFlow,Integer userVipId,Integer mchtVipId,String type,String reason) ;
	
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
	public JSONObject appraise2User(Order order,Integer score,String content);
	
	
}


