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
	 * 添加客户退款成功流水
	 * 1、买家使用余额支付，则退款时增加买家的可用余额；减少卖家的冻结余额；
	 * 2、买家使用非余额支付，则退款时减少卖家的冻结余额；
	 * 
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)':
	 * 	11-客户退款完成、12-系统分润、13-平台奖励、14-资金解冻、15-积分兑换、16-现金红包、17-其他
	 * 	21-提现申请、22-消费、23-资金冻结、24-投诉罚款、25-其他
	 * 	31-冻结交易买卖额、32-买单投诉冻结、33-提现冻结、34-其他
 	 * 	41-恢复可用余额、42-提现完成、43-交易完成、44-买家退款完成
 	 * 
	 * @return
	 */
	@Override
	public void refundSuccess(boolean useVip,BigDecimal amount,Integer userVipId,String reason,Integer oprId,Integer mchtVipId) {
		if(useVip) {//卖家使用余额支付
			//客户退款
			ChangeFlow flow1 = new ChangeFlow();
			flow1.setFlowId(this.genFlowId(userVipId));
			flow1.setVipId(userVipId);
			flow1.setCreateTime(new Date());
			flow1.setAmount(amount);
			flow1.setChangeType("11");
			flow1.setReason(reason);
			flow1.setCreateOpr(oprId);
			flow1.setSumFlag("0");
			this.changeFlowMapper.insert(flow1);
		}
		//商家解冻
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(userVipId));
		flow2.setVipId(mchtVipId);
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType("44");
		flow2.setReason(reason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		this.changeFlowMapper.insert(flow2);
	}
	
	/**
	 * 添加客户支付成功流水(买商家的商品)
	 * 1、客户使用余额消费，添加客户的消费流水；
	 * 2、添加商户的交易流水；
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)':
	 * 	11-客户退款、12-系统分润、13-平台奖励、14-资金解冻、15-积分兑换、16-现金红包、17-其他
	 * 	21-提现申请、22-客户消费、23-资金冻结、24-投诉罚款、25-其他
	 * 	31-冻结交易买卖额、32-买单投诉冻结、33-提现冻结、34-其他
 	 * 	41-恢复可用余额、42-提现完成、43-交易完成、44-买家退款
	 * @param flow
	 * @return
	 */
	public void paySuccess(boolean useVip,BigDecimal amount,VipBasic userVip,String reason,Integer oprId,Integer mchtVipId) {
		if(useVip) {
			//客户消费
			ChangeFlow flow1 = new ChangeFlow();
			flow1.setFlowId(this.genFlowId(userVip.getVipId()));
			flow1.setVipId(userVip.getVipId());
			flow1.setCreateTime(new Date());
			flow1.setAmount(amount);
			flow1.setChangeType("22");
			flow1.setReason(reason);
			flow1.setCreateOpr(oprId);
			flow1.setSumFlag("0");
			this.changeFlowMapper.insert(flow1);
		}
		//商家冻结，待评价完成解冻
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(userVip.getVipId()));
		flow2.setVipId(mchtVipId);
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType("31");
		flow2.setReason(reason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		this.changeFlowMapper.insert(flow2);
	}
	
	/**
	 * 交易完成，解冻商家
	 * 变更会员账户信息
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)':
	 * 	11-客户退款、12-系统分润、13-平台奖励、14-资金解冻、15-积分兑换、16-现金红包、17-其他
	 * 	21-提现申请、22-客户消费、23-资金冻结、24-投诉罚款、25-其他
	 * 	31-冻结交易买卖额、32-买单投诉冻结、33-提现冻结、34-其他
 	 * 	41-恢复可用余额、42-提现完成、43-交易完成、44-买家退款
	 * @param flow
	 * @return
	 */
	public void apprFinish(BigDecimal amount,VipBasic mchtVip,Integer oprId,String reason) {
		//减少冻结额
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(mchtVip.getVipId()));
		flow1.setVipId(mchtVip.getVipId());
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType("43");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		this.changeFlowMapper.insert(flow1);
		//添加可用余额
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(mchtVip.getVipId()));
		flow2.setVipId(mchtVip.getVipId());
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType("14");
		flow2.setReason(reason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		this.changeFlowMapper.insert(flow2);
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
	 * 添加用户提现申请流水
	 * 
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)':
	 * 	11-客户退款、12-系统分润、13-平台奖励、14-资金解冻、15-积分兑换、16-现金红包、17-其他
	 * 	21-提现申请、22-客户消费、23-资金冻结、24-投诉罚款、25-其他
	 * 	31-冻结交易买卖额、32-买单投诉冻结、33-提现冻结、34-其他
 	 * 	41-恢复可用余额、42-提现完成、43-交易完成、44-买家退款
	 * @param flow
	 * @return
	 */
	public void cashApply(BigDecimal amount,VipBasic vip,Integer oprId,String reason) {
		//减少可用余额
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vip.getVipId()));
		flow1.setVipId(vip.getVipId());
		flow1.setCreateTime(new Date());
		flow1.setAmount(amount);
		flow1.setChangeType("21");
		flow1.setReason(reason);
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		this.changeFlowMapper.insert(flow1);
		//添加冻结余额
		ChangeFlow flow2 = new ChangeFlow();
		flow2.setFlowId(this.genFlowId(vip.getVipId()));
		flow2.setVipId(vip.getVipId());
		flow2.setCreateTime(new Date());
		flow2.setAmount(amount);
		flow2.setChangeType("33");
		flow2.setReason(reason);
		flow2.setCreateOpr(oprId);
		flow2.setSumFlag("0");
		this.changeFlowMapper.insert(flow2);
		
	}
	
	/**
	 * 添加用户提现结束流水
	 * 
	 * 资金变动类型(1-可用余额来源，2-可用余额去向，3-冻结金额来源，4-解冻金额去向)':
	 * 	11-客户退款、12-系统分润、13-平台奖励、14-资金解冻、15-积分兑换、16-现金红包、17-其他
	 * 	21-提现申请、22-客户消费、23-资金冻结、24-投诉罚款、25-其他
	 * 	31-冻结交易买卖额、32-买单投诉冻结、33-提现冻结、34-其他
 	 * 	41-恢复可用余额、42-提现结束、43-交易完成、44-买家退款
	 * @param flow
	 * @return
	 */
	public void cashFinish(boolean success,ChangeFlow flow,VipBasic vip,Integer oprId) {
		//减少冻结
		ChangeFlow flow1 = new ChangeFlow();
		flow1.setFlowId(this.genFlowId(vip.getVipId()));
		flow1.setVipId(vip.getVipId());
		flow1.setCreateTime(new Date());
		flow1.setAmount(flow.getAmount());
		flow1.setChangeType("42");
		flow1.setReason("提现结束，减少冻结余额");
		flow1.setCreateOpr(oprId);
		flow1.setSumFlag("0");
		this.changeFlowMapper.insert(flow1);
		if(!success) {//提现失败，恢复可用额
			ChangeFlow flow2 = new ChangeFlow();
			flow2.setFlowId(this.genFlowId(vip.getVipId()));
			flow2.setVipId(vip.getVipId());
			flow2.setCreateTime(new Date());
			flow2.setAmount(flow.getAmount());
			flow2.setChangeType("14");
			flow2.setReason("提现失败，恢复可用额");
			flow2.setCreateOpr(oprId);
			flow2.setSumFlag("0");
			this.changeFlowMapper.insert(flow2);
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
	 * 生成36位的ID：17位时间+16位用户ID+3位随机字符串
	 * 
	 * @param userVipId	转16位
	 * @return
	 */
	public String genFlowId(Integer userVipId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");//17位时间
		String currTime = sdf.format(new Date());
		String uId = userVipId + "";
		for(int i=0;i<16-uId.length();i++) {
			uId = "0" + uId;
		}
		return currTime + uId + NonceStrUtil.getNonceStr(3);
	}
	
}
