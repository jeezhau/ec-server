package com.mofangyouxuan.service;

import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.VipBasic;

/**
 * 会员账户资金变动流水
 * @author jeekhan
 *
 */
public interface ChangeFlowService {

	/**
	 * 用户或商家申请退款
	 * 1、添加商户冻结余额流水；
	 * 2、减少商户可用余额流水；
	 * @param amount		退款金额
	 * @param vipId		商家VIPID
	 * @param reason		退款原因
	 * @param oprId		操作员ID
	 * @param orderId	商品订单ID
	 * 
	 * @return 00-成功，其他-失败信息 
	 * @throws Exception 
	 */
	public String refundApply(Long amount, Integer mchtVipId, String reason, Integer oprId,String orderId) throws Exception;

	/**
	 * 退款失败
	 * 1、增加商户可用余额的流水；
	 * 2、减少商户冻结余额的流水；
	 * @param amount		退款金额
	 * @param mchtVipId	商家VIPID
	 * @param reason		失败原因
	 * @param oprId		操作员ID
	 * @param orderId	商品订单ID
	 * 
	 * @return 00-成功，其他-失败信息 
	 * @throws Exception 
	 */
	public String refundFail(Long amount, Integer mchtVipId, String reason, Integer oprId,String orderId) throws Exception;
	
	/**
	 * 添加客户退款成功流水
	 * 1、买家使用余额支付，则退款时增加买家的可用余额；减少卖家的冻结余额；
	 * 2、买家使用非余额支付，则退款时减少卖家的冻结余额；
	 * 
	 * @param useVipPay	是否使用会员余额支付
	 * @param amount		交易金额
	 * @param userVipId	买家会员账户
	 * @param reason		退款原因
	 * @param oprId		操作员ID
	 * @param mchtVipId	卖家会员账户Id
	 * @param orderId	商品订单ID
	 * 
	 * @return 00-成功，其他-失败信息 
	 * @throws Exception 
	 */
	public String refundSuccess(boolean useVipPay,Long amount,Integer userVipId,String reason,Integer oprId,Integer mchtVipId,String orderId) throws Exception;
	
	/**
	 * 添加客户支付成功流水(买商家的商品)
	 * 1、客户使用余额支付，添加客户的支付流水（减少可用余额）；
	 * 2、添加商户的冻结余额；
	 * 
	 * @param useVipPay	是否使用会员余额支付
	 * @param amount		交易金额
	 * @param userVip	买家会员账户
	 * @param reason		成功明细
	 * @param oprId		操作员ID
	 * @param mchtVipId	卖家会员账户ID
	 * @param orderId	商品订单ID
	 * 
	 *  @return 00-成功，其他-失败信息
	 * @throws Exception 
	 */
	public String paySuccess(boolean useVipPay,Long amount,VipBasic userVip,String reason,Integer oprId,Integer mchtVipId,String orderId) throws Exception;
	
	/**
	 * 解冻商家
	 * 1、减少冻结余额，增加可用余额；
	 * @param amount	交易金额
	 * @param userId		买家ID 
	 * @param mchtVipId	商家会员账户
	 * @param oprId 		操作员ID
	 * @param reason		操作原因
	 * @param orderId	商品订单ID
	 * 
	 * @return 00-成功，其他-失败信息 
	 * @throws Exception 
	 */
	public String dealFinish(Long amount,Integer userId,Integer mchtVipId,Integer oprId,String reason,String orderId)throws Exception;
	

	/**
	 * 添加用户提现申请流水
 	 * 1、减少可用余额，添加冻结余额；
	 * @param amount		提现金额，包含手续费
	 * @param vipId		会员账号
	 * @param oprId		操作员ID
	 * @param reason
	 * @return "00"-成功，其他：失败信息
	 */
	public String cashApply(Long amount, Integer vipId, Integer oprId, String reason);
	
	/**
	 * 添加用户提现结束流水
	 * 1、减少冻结余额；
	 * 2、失败则添加可用余额；
	 * @param success	是否提现成功
	 * @param flow	提现的原流水
	 * @param vip	提现会员用户
	 * @param oprId	操作人ID
	 * @return "00"-成功，其他：失败信息
	 */
	public String cashFinish(boolean success,Long amount, Integer vipId, Integer oprId, String reason);
	
	
	/**
	 * 删除流水
	 * @param flow
	 * @return
	 */
	public int delete(ChangeFlow flow);
	
	/**
	 * 根据ID获取流水
	 * @param flowId
	 * @return
	 */
	public ChangeFlow get(String flowId);


}


