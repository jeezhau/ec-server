package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.mapper.ChangeFlowMapper;
import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.NonceStrUtil;

@Service
@Transactional
public class ChangeFlowServiceImpl implements ChangeFlowService{
	
	@Autowired
	private ChangeFlowMapper changeFlowMapper;
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	
	
	@Autowired
	private SysParamUtil sysParamUtil;
	
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
	public String refundApply(Long amount, Integer mchtVipId, String reason, Integer oprId,String orderId) throws Exception {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", orderId);
		params.put("vipId", mchtVipId);
		params.put("changeType", CashFlowTP.CFTP31.value); 
		int cnt = this.changeFlowMapper.countAll(params); //交易成功冻结记录数
		if(cnt <= 0) {
			throw new Exception("该订单没有交易成功流水信息");
		}
		params.put("changeType", CashFlowTP.CFTP14.value); 
		int cnt1 = this.changeFlowMapper.countAll(params); //资金解冻记录数
		if(cnt1 > 0) {
			String ret = this.doubleFreeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP25, reason, CashFlowTP.CFTP34,orderId);
			if(!"00".equals(ret)) {
				throw new Exception(ret);
			}
		}
		return "00";
	}
	
	/**
	 * 添加客户退款成功流水
	 * 1、买家使用余额支付，则退款时增加买家的可用余额；减少卖家的冻结余额；
	 * 2、买家使用非余额支付，则退款时减少卖家的冻结余额；
	 * 3、收回已经支付的服务费、交易分润；
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
	public String refundSuccess(boolean useVipPay,Long amount,Integer userVipId,String reason,Integer oprId,Integer mchtVipId,String orderId) throws Exception {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", orderId);
		params.put("vipId", mchtVipId);
		params.put("changeType", CashFlowTP.CFTP31.value); 
		int cnt = this.changeFlowMapper.countAll(params); //交易成功冻结记录数
		if(cnt <= 0) {
			throw new Exception("该订单没有交易成功流水信息");
		}
		if(useVipPay) {//卖家使用余额支付
			//客户增加可用余额
			String ret = this.addBal(amount, userVipId, oprId, reason, CashFlowTP.CFTP11,orderId);
			if(!"00".equals(ret)) {
				throw new Exception(ret);
			}
		}
		//商家减少解冻余额
		String ret = this.unFreeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP44,orderId);
		if(!"00".equals(ret)) {
			throw new Exception(ret);
		}
		
		UserBasic buyUser = this.userBasicService.get(userVipId);
		PartnerBasic partner = this.partnerBasicService.getByBindUser(mchtVipId);
		PartnerBasic sysPartner = this.partnerBasicService.getByID(this.sysParamUtil.getSysPartnerId());
		if(sysPartner == null || buyUser == null || partner == null) {
			throw new Exception("获取用户或商家信息失败！");
		}
		//收回交易服务费
		params.put("vipId", sysPartner.getVipId());
		params.put("changeType", CashFlowTP.CFTP10.value); 
		List<ChangeFlow> serviceList = this.changeFlowMapper.selectAll(params, new PageCond(0,1), null);
		if(serviceList != null && serviceList.size()>0) {
			ret = this.subBal(serviceList.get(0).getAmount(), sysPartner.getVipId(), oprId, reason, CashFlowTP.CFTP20, orderId);
			if(!"00".equals(ret)) {
				throw new Exception(ret);
			}
		}
		//收回推广用户分润奖励
		params.put("vipId", buyUser.getSenceId());
		params.put("changeType", CashFlowTP.CFTP12.value); 
		List<ChangeFlow> userShareList = this.changeFlowMapper.selectAll(params, new PageCond(0,1), null);
		if(userShareList != null && userShareList.size()>0) {
			ret = this.unShareProfit(sysPartner, userShareList.get(0).getAmount(), buyUser.getSenceId(), oprId, reason, orderId);
			if(!"00".equals(ret)) {
				throw new Exception(ret);
			}
		}
		//收回推广商家分润奖励
		if(partner.getUpPartnerId() != null) {
			PartnerBasic spPartner = this.partnerBasicService.getByID(partner.getUpPartnerId());
			if(spPartner != null) {
				params.put("vipId", spPartner.getVipId());
				params.put("changeType", CashFlowTP.CFTP12.value); 
				List<ChangeFlow> partnerShareList = this.changeFlowMapper.selectAll(params, new PageCond(0,1), null);
				if(partnerShareList != null && partnerShareList.size()>0) {
					ret = this.unShareProfit(sysPartner, partnerShareList.get(0).getAmount(), spPartner.getVipId(), oprId, reason, orderId);
					if(!"00".equals(ret)) {
						throw new Exception(ret);
					}
				}
			}
		}
		return "00";
	}
	
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
	public String refundFail(Long amount, Integer mchtVipId, String reason, Integer oprId,String orderId) throws Exception {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", orderId);
		params.put("vipId", mchtVipId);
		params.put("changeType", CashFlowTP.CFTP31.value); 
		int cnt = this.changeFlowMapper.countAll(params); //交易成功冻结记录数
		if(cnt <= 0) {
			throw new Exception("该订单没有交易成功流水信息");
		}
		params.put("changeType", CashFlowTP.CFTP14.value); 
		int cnt1 = this.changeFlowMapper.countAll(params); //资金解冻记录数
		if(cnt1 > 0) {
			String ret = this.doubleUnFreeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP17, reason, CashFlowTP.CFTP45,orderId);
			if(!"00".equals(ret)) {
				throw new Exception(ret);
			}
		}
		return "00";
	}
	
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
	 * @return 00-成功，其他-失败信息
	 * @throws Exception 
	 */
	public String paySuccess(boolean useVipPay,Long amount,VipBasic userVip,String reason,Integer oprId,Integer mchtVipId,String orderId) throws Exception{
		if(useVipPay) {
			//客户消费
			String ret = this.subBal(amount, userVip.getVipId(), oprId, reason, CashFlowTP.CFTP22,orderId);
			if(!"00".equals(ret)) {
				throw new Exception(ret);
			}
		}
		//商家冻结，待评价完成解冻
		String ret = this.freeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP31,orderId);
		if(!"00".equals(ret)) {
			throw new Exception(ret);
		}
		return "00";
	}
	
	/**
	 * 解冻商家
	 * 1、减少冻结余额，增加可用余额；
	 * 2、执行推广用户系统分润；
	 * 3、执行推广卖家系统分润；
	 * @param amount		交易金额
	 * @param userId		买家ID
	 * @param mchtVipId	商家会员账户
	 * @param oprId 		操作员ID
	 * @param reason		操作原因
	 * @param orderId	商品订单ID 
	 * 
	 * @return 00-成功，其他-失败信息
	 * @throws Exception 
	 */
	public String dealFinish(Long amount,Integer userId,Integer mchtVipId,Integer oprId,String reason,String orderId) throws Exception {
		UserBasic buyUser = this.userBasicService.get(userId);
		PartnerBasic partner = this.partnerBasicService.getByBindUser(mchtVipId);
		PartnerBasic sysPartner = this.partnerBasicService.getByID(this.sysParamUtil.getSysPartnerId());
		if(sysPartner == null || buyUser == null || partner == null) {
			throw new Exception("获取用户或商家信息失败！");
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", orderId);
		params.put("vipId", mchtVipId);
		params.put("changeType", CashFlowTP.CFTP31.value); 
		int cnt = this.changeFlowMapper.countAll(params); //交易成功冻结记录数
		if(cnt <= 0) {
			throw new Exception("该订单没有交易成功流水信息");
		}
		//商家卖款解冻
		String ret = this.doubleUnFreeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP14, reason, CashFlowTP.CFTP43,orderId);
		if(!"00".equals(ret)) {
			throw new Exception(ret);
		}
		//推广用户分润
		if(buyUser != null && buyUser.getSenceId()!=null) {
			VipBasic spreadVip = this.vipBasicService.get(buyUser.getSenceId());
			if(spreadVip != null) {
				BigDecimal spreadUserProfitRate = this.sysParamUtil.getSpreadUserProfitRatio();
				Long profit = spreadUserProfitRate.multiply(new BigDecimal(amount)).setScale(0, BigDecimal.ROUND_FLOOR).longValue();
				ret = this.shareProfit(sysPartner,profit, spreadVip.getVipId(), oprId, "推广用户购买商品获得奖励",orderId);
				if(!"00".equals(ret)) {
					throw new Exception(ret);
				}
			}
		}
		//推广商家分润
		if(partner.getUpPartnerId() != null) {
			PartnerBasic spPartner = this.partnerBasicService.getByID(partner.getUpPartnerId());
			if(spPartner != null) {
				BigDecimal spreadMchtProfitRate = this.sysParamUtil.getSpreadMchtProfitRatio();
				Long profit = spreadMchtProfitRate.multiply(new BigDecimal(amount)).setScale(0, BigDecimal.ROUND_FLOOR).longValue();
				ret = this.shareProfit(sysPartner,profit, spPartner.getVipId(), oprId, "推广商家出售商品获得奖励",orderId);
				if(!"00".equals(ret)) {
					throw new Exception(ret);
				}
			}
		}
		//收取服务费
		BigDecimal platformServiceFeeRate = this.sysParamUtil.getPlatformServiceFeeRatio();
		Long profit = platformServiceFeeRate.multiply(new BigDecimal(amount)).setScale(0, BigDecimal.ROUND_CEILING).longValue();
		ret = this.addBal(profit, sysPartner.getVipId(), oprId, reason, CashFlowTP.CFTP10, orderId);
		if(!"00".equals(ret)) {
			throw new Exception(ret);
		}
		
		return "00";
	}
	
	/**
	 * 交易资金解冻时添加分润流水
	 * 1、增加推广者可用余额；
	 * 2、减少系统合作伙伴用户可用余额；
	 * @param sysPartner
	 * @param amount		分润金额
	 * @param vipId		获得分润的VIP:推广买家或推广卖家
	 * @param oprId
	 * @param reason
	 * @param orderId	商品订单ID
	 * @return 00-成功，其他-失败信息
	 * @throws Exception
	 */
	private String shareProfit(PartnerBasic sysPartner,Long amount,Integer vipId,Integer oprId,String reason,String orderId) throws Exception {
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(CashFlowTP.CFTP12.getValue()+"");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt1 = this.changeFlowMapper.insert(flow1);
		
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(sysPartner.getVipId()));
		flow2.setVipId(sysPartner.getVipId());
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType(CashFlowTP.CFTP26.getValue()+"");
		flow2.setReason(reason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		flow2.setOrderId(orderId);
		int cnt2 = this.changeFlowMapper.insert(flow2);
		
		if(cnt1 >0 && cnt2 >0) {
			return "00";
		}else {
			return "添加分润流水失败";
		}
		
	}
	
	/**
	 * 退款收回分润奖励
	 * 1、减少推广者可用余额；
	 * 2、增加系统合作伙伴用户可用余额；
	 * @param sysPartner
	 * @param amount		分润金额
	 * @param vipId		获得分润的VIP:推广买家或推广卖家
	 * @param oprId
	 * @param reason
	 * @param orderId	商品订单ID
	 * @return 00-成功，其他-失败信息
	 * @throws Exception
	 */
	private String unShareProfit(PartnerBasic sysPartner,Long amount,Integer vipId,Integer oprId,String reason,String orderId) throws Exception {
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(CashFlowTP.CFTP26.getValue()+"");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt1 = this.changeFlowMapper.insert(flow1);
		
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(sysPartner.getVipId()));
		flow2.setVipId(sysPartner.getVipId());
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType(CashFlowTP.CFTP12.getValue()+"");
		flow2.setReason(reason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		flow2.setOrderId(orderId);
		int cnt2 = this.changeFlowMapper.insert(flow2);
		
		if(cnt1 >0 && cnt2 >0) {
			return "00";
		}else {
			return "添加分润流水失败";
		}
		
	}
	/**
	 * 账户可用资金单向增加
	 * 1、添加增加可用余额流水；
	 * 
	 * @param amount		变更的金额
	 * @param vip		变更的VIP账户
	 * @param oprId		操作员ID
	 * @param reason		增加可用余额的理由
	 * @param tp			增加可用余额的流水类型
	 * @param orderId	商品订单ID
	 */
	private String addBal(Long amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp,String orderId) {
		if(tp == null || tp.getValue()<10 || tp.getValue()>19) {
			return "增加可用余额流水的类型不正确！";
		}
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(tp.getValue() + "");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt = this.changeFlowMapper.insert(flow1);
		if(cnt>0 ) {
			return "00";
		}else {
			return "添加可用余额变更流水信息失败";
		}
	}
	
	/**
	 * 账户可用资金单向减少
	 * 1、添加减少可用余额流水；
	 * @param amount		变更的金额
	 * @param vip		变更的VIP账户
	 * @param oprId		操作员ID
	 * @param reason		减少冻结余额的理由
	 * @param tp			减少冻结余额的流水类型
	 * @param orderId	商品订单ID
	 */
	private String subBal(Long amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp,String orderId) {
		if(tp == null || tp.getValue()<20 || tp.getValue()>29) {
			return "减少可用余额流水的类型不正确！";
		}
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(tp.getValue() + "");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt = this.changeFlowMapper.insert(flow1);
		if(cnt>0 ) {
			return "00";
		}else {
			return "添加可用余额变更流水信息失败";
		}
	}
	
	/**
	 * 账户资金双向冻结（提现、退款）
	 * 1、添加减少可用余额流水；
	 * 2、添加增加冻结余额流水；
 	 * 
	 * @param amount		变更的金额
	 * @param vipId		变更的VIP账户
	 * @param oprId		操作人ID
	 * @param balReason	减少可用余额流水的原因
	 * @param balTp		减少可用余额余额的流水类型
	 * @param frzReason	增加冻结余额流水的原因
	 * @param frzTp		增加冻结余额流水的原因
	 * @param orderId	商品订单ID
	 */
	private String doubleFreeze(Long amount,Integer vipId,Integer oprId,String balReason,CashFlowTP balTp,String frzReason,CashFlowTP frzTp,String orderId) {
		if(balTp == null || balTp.getValue()<20 || balTp.getValue()>29) {
			return "减少可用余额流水的类型不正确！";
		}
		if(frzTp == null || frzTp.getValue()<30 || frzTp.getValue()>39) {
			return "增加冻结余额流水的类型不正确！";
		}
		//减少可用余额
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(balTp.getValue() + "");
		flow1.setReason(balReason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt1 = this.changeFlowMapper.insert(flow1);
		//添加冻结余额
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(vipId));
		flow2.setVipId(vipId);
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType(frzTp.getValue() + "");
		flow2.setReason(frzReason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		flow2.setOrderId(orderId);
		int cnt2 = this.changeFlowMapper.insert(flow2);
		if(cnt1>0 && cnt2>0) {
			return "00";
		}else {
			return "添加资金冻结流水信息失败";
		}
	}
	
	/**
	 * 账户资金双向解冻
	 * 1、添加增加可用余额流水；
	 * 2、添加减少冻结余额流水；
	 * 
	 * @param amount		变更的金额
	 * @param vipId		变更的VIP账户
	 * @param oprId		操作人ID
	 * @param balReason	增加可用余额流水的原因
	 * @param balTp		增加可用余额余额的流水类型
	 * @param frzReason	减少冻结余额流水的原因
	 * @param frzTp		减少冻结余额流水的原因
	 * @param orderId	商品订单ID
	 */
	private String doubleUnFreeze(Long amount,Integer vipId,Integer oprId,String balReason,CashFlowTP balTp,String frzReason,CashFlowTP frzTp,String orderId) {
		if(balTp == null || balTp.getValue()<10 || balTp.getValue()>19) {
			return "增加可用余额流水的类型不正确！";
		}
		if(frzTp == null || frzTp.getValue()<40 || frzTp.getValue()>49) {
			return "减少冻结余额流水的类型不正确！";
		}
		//增加可用
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(balTp.getValue() + "");
		flow1.setReason(balReason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt1 = this.changeFlowMapper.insert(flow1);
		//减少冻结
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(vipId));
		flow2.setVipId(vipId);
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType(frzTp.getValue() + "");
		flow2.setReason(frzReason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		flow2.setOrderId(orderId);
		int cnt2 = this.changeFlowMapper.insert(flow2);
		if(cnt1>0 && cnt2>0) {
			return "00";
		}else {
			return "添加资金解冻流水信息失败";
		}
	}

	/**
	 * 账户资金单向解冻
	 * 1、添加增加冻结余额流水；
	 * 
	 * @param amount		变更的金额
	 * @param vip		变更的VIP账户
	 * @param oprId		操作员ID
	 * @param reason		增加冻结余额的理由
	 * @param tp			增加冻结余额的流水类型
	 * @param orderId	商品订单ID
	 */
	private String freeze(Long amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp,String orderId) {
		if(tp == null || tp.getValue()<30 || tp.getValue()>39) {
			return "增加冻结余额流水的类型不正确！";
		}
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(tp.getValue() + "");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt = this.changeFlowMapper.insert(flow1);
		if(cnt>0 ) {
			return "00";
		}else {
			return "添加资金冻结流水信息失败";
		}
	}
	
	/**
	 * 账户资金单向解冻
	 * 1、添加减少冻结余额流水；
	 *  
	 * @param amount		变更的金额
	 * @param vip		变更的VIP账户
	 * @param oprId		操作员ID
	 * @param reason		减少冻结余额的理由
	 * @param tp			减少冻结余额的流水类型
	 * @param orderId	商品订单ID
	 */
	private String unFreeze(Long amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp,String orderId) {
		if(tp == null || tp.getValue()<40 || tp.getValue()>49) {
			return "减少冻结余额流水的类型不正确！";
		}
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vipId));
		flow1.setVipId(vipId);
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(tp.getValue() + "");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		flow1.setOrderId(orderId);
		int cnt = this.changeFlowMapper.insert(flow1);
		if(cnt>0 ) {
			return "00";
		}else {
			return "添加资金解冻流水信息失败";
		}
	}
	
	/**
	 * 删除流水
	 * @param flow
	 * @return
	 */
	public int delete(ChangeFlow flow) {
		int cnt = this.changeFlowMapper.deleteByPrimaryKey(flow.getFlowId());
		return cnt;
	}
	
	/**
	 * 根据ID获取流水
	 * @param flowId
	 * @return
	 */
	public ChangeFlow get(String flowId) {
		return this.changeFlowMapper.selectByPrimaryKey(flowId);
	}
	
	
	/**
	 * 生成32位的ID：17位时间+11位用户ID+4位随机字符串
	 * 
	 * @param userVipId	转11位
	 * @return
	 */
	private String genFlowId(Integer userVipId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");//17位时间
		String currTime = sdf.format(new Date());
		String uId = userVipId + "";
		int len = uId.length();
		for(int i=0;i<11-len;i++) {
			uId = "0" + uId;
		}
		return currTime + uId + NonceStrUtil.getNonceNum(4);
	}
	
	/**
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)
	 * @author jeekhan
	 */
	public static enum CashFlowTP{
		 CFTP10(10,"服务费"),CFTP11(11,"客户退款"),CFTP12(12,"交易分润"),CFTP13(13,"平台奖励"), CFTP14(14,"资金解冻"), CFTP15(15,"积分兑换"), CFTP16(16,"现金红包"), CFTP17(17,"退款失败"), CFTP18(18,"提现失败"), 
		 CFTP20(20,"退款服务费"),CFTP21(21,"提现申请"), CFTP22(22,"客户消费"), CFTP23(23,"资金冻结"), CFTP24(24,"投诉罚款"), CFTP25(25,"申请退款"),CFTP26(26,"交易分润"),
		 CFTP31(31,"冻结交易买卖额"), CFTP32(32,"买单投诉冻结"), CFTP33(33,"提现冻结"), CFTP34(34,"申请退款"),
		 CFTP41(41,"恢复可用余额"), CFTP42(42,"提现完成"), CFTP43(43,"交易完成"), CFTP44(44,"退款成功"),CFTP45(45,"退款失败");
		
		private Integer value;
		private String desc;
		
		private CashFlowTP(Integer value,String desc) {
			this.value = value;
			this.desc = desc;
		}
		public Integer getValue() {
			return this.value;
		}
		public String getDesc() {
			return this.desc;
		}
	}

	/**
	 * 提现申请
	 * @param amount		提现金额，包含手续费
	 * @param vipId		会员账号
	 * @param oprId		操作员ID
	 * @param reason
	 */
	@Override
	public String cashApply(Long amount, Integer vipId, Integer oprId, String reason) {
		return this.doubleFreeze(amount, vipId, oprId, reason, CashFlowTP.CFTP21, reason, CashFlowTP.CFTP33,null);
	}

	/**
	 * 提现完成
	 * @param success	是否提现成功
	 * @param amount		金额
	 * @param vipId		会员ID
	 * @param oprId
	 * @param reason
	 */
	@Override
	public String cashFinish(boolean success,Long amount, Integer vipId, Integer oprId, String reason) {
		if(success) {
			return this.unFreeze(amount, vipId, oprId, reason, CashFlowTP.CFTP42,null);
		}else {
			return this.doubleUnFreeze(amount, vipId, oprId, reason, CashFlowTP.CFTP18, reason, CashFlowTP.CFTP42,null);
		}
	}
	
}
