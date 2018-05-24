package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.mapper.ChangeFlowMapper;
import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.utils.NonceStrUtil;

@Service
@Transactional
public class ChangeFlowServiceImpl implements ChangeFlowService{
	
	@Autowired
	private ChangeFlowMapper changeFlowMapper;
	
	/**
	 * 用户或商家申请退款
	 * 1、添加商户冻结余额流水；
	 * 2、减少商户可用余额流水；
	 * @param amount
	 * @param vipId
	 * @param reason
	 * @param oprId
	 */
	@Override
	public void refundApply(BigDecimal amount,Integer vipId,String reason,Integer oprId) {
		this.doubleFreeze(amount, vipId, oprId, reason, CashFlowTP.CFTP25, reason, CashFlowTP.CFTP34);
	}
	
	/**
	 * 添加客户退款成功流水
	 * 1、买家使用余额支付，则退款时增加买家的可用余额；减少卖家的冻结余额；
	 * 2、买家使用非余额支付，则退款时减少卖家的冻结余额；
 	 * 
	 * @return
	 */
	@Override
	public void refundSuccess(boolean useVip,BigDecimal amount,Integer userVipId,String reason,Integer oprId,Integer mchtVipId) {
		if(useVip) {//卖家使用余额支付
			//客户退款
			this.addBal(amount, userVipId, oprId, reason, CashFlowTP.CFTP11);
		}
		//商家解冻
		this.unFreeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP44);
	}
	
	/**
	 * 退款失败
	 * 1、添加增加可用余额的流水；
	 * 2、添加减少冻结余额的流水；
	 * @param amount
	 * @param vipId
	 * @param reason
	 * @param oprId
	 */
	@Override
	public void refundFail(BigDecimal amount,Integer vipId,String reason,Integer oprId) {
		this.doubleUnFreeze(amount, vipId, oprId, reason, CashFlowTP.CFTP17, reason, CashFlowTP.CFTP45);
	}
	
	/**
	 * 客户支付成功(买商家的商品)
	 * 1、余额支付则添加减少可用余额流水；
	 * 2、添加商家冻结余额流水；
	 * @param flow
	 * @return
	 */
	@Override
	public void paySuccess(boolean useBal,BigDecimal amount,VipBasic userVip,String reason,Integer oprId,Integer mchtVipId) {
		if(useBal) {
			//客户消费
			this.subBal(amount, userVip, oprId, reason, CashFlowTP.CFTP22);
		}
		//商家冻结，待评价完成解冻
		this.freeze(amount, mchtVipId, oprId, reason, CashFlowTP.CFTP31);
	}
	
	/**
	 * 交易完成，解冻商家
	 * 1、减少冻结额
	 * 2、添加可用余额
	 * @param flow
	 * @return
	 */
	@Override
	public void dealFinish(BigDecimal amount,Integer vipId,Integer oprId,String reason) {
		this.doubleUnFreeze(amount, vipId, oprId, reason, CashFlowTP.CFTP14, reason, CashFlowTP.CFTP43);
	}
	
	/**
	 * 添加分润流水
	 * 变更会员账户信息
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)':
	 * 	11-客户退款、12-系统分润、13-平台奖励、14-资金解冻、15-积分兑换、16-现金红包、17-其他
	 * 	21-提现申请、22-客户消费、23-资金冻结、24-投诉罚款、25-其他
	 * 	31-冻结交易买卖额、32-买单投诉冻结、33-提现冻结、34-其他
 	 * 	41-恢复可用余额、42-提现完成、43-交易完成、44-买家退款
	 * @param flow
	 * @return
	 */
	@Override
	public int shareProfit(BigDecimal amount,VipBasic vip,Integer oprId,String reason) {
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vip.getVipId()));
		flow1.setVipId(vip.getVipId());
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType("12");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		int cnt = this.changeFlowMapper.insert(flow1);
		return cnt;
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
	 */
	private String addBal(BigDecimal amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp) {
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
		this.changeFlowMapper.insert(flow1);
		return "00";
	}
	
	/**
	 * 账户可用资金单向减少
	 * 1、添加减少可用余额流水；
	 * @param amount		变更的金额
	 * @param vip		变更的VIP账户
	 * @param oprId		操作员ID
	 * @param reason		减少冻结余额的理由
	 * @param tp			减少冻结余额的流水类型
	 */
	private String subBal(BigDecimal amount,VipBasic vip,Integer oprId,String reason,CashFlowTP tp) {
		if(tp == null || tp.getValue()<20 || tp.getValue()>29) {
			return "减少可用余额流水的类型不正确！";
		}
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vip.getVipId()));
		flow1.setVipId(vip.getVipId());
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType(tp.getValue() + "");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		this.changeFlowMapper.insert(flow1);
		return "00";
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
	 */
	private String doubleFreeze(BigDecimal amount,Integer vipId,Integer oprId,String balReason,CashFlowTP balTp,String frzReason,CashFlowTP frzTp) {
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
		this.changeFlowMapper.insert(flow1);
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
		this.changeFlowMapper.insert(flow2);
		return "00";
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
	 */
	private String doubleUnFreeze(BigDecimal amount,Integer vipId,Integer oprId,String balReason,CashFlowTP balTp,String frzReason,CashFlowTP frzTp) {
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
		this.changeFlowMapper.insert(flow1);
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
		this.changeFlowMapper.insert(flow2);
		return "00";
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
	 */
	private String freeze(BigDecimal amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp) {
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
		this.changeFlowMapper.insert(flow1);
		return "00";
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
	 */
	private String unFreeze(BigDecimal amount,Integer vipId,Integer oprId,String reason,CashFlowTP tp) {
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
		this.changeFlowMapper.insert(flow1);
		return "00";
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
		 CFTP11(11,"客户退款"),CFTP12(12,"系统分润"),CFTP13(13,"平台奖励"), CFTP14(14,"资金解冻"), CFTP15(15,"积分兑换"), CFTP16(16,"现金红包"), CFTP17(17,"退款失败"),  
		 CFTP21(21,"提现申请"), CFTP22(22,"客户消费"), CFTP23(23,"资金冻结"), CFTP24(24,"投诉罚款"), CFTP25(25,"申请退款"),
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

	@Override
	public void cashApply(BigDecimal amount, VipBasic vip, Integer oprId, String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cashFinish(boolean success, ChangeFlow flow, VipBasic vip, Integer oprId) {
		// TODO Auto-generated method stub
		
	}
	
}
