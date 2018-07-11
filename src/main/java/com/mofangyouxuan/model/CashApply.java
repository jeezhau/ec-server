package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class CashApply {
    private String applyId;
    
    @NotNull(message="会员ID：不可为空！")
    private Integer vipId;

    @NotNull(message="提现方式：不可为空！")
    @Pattern(regexp="[1]",message="提现方式：取值为【1-手动提现】！")
    private String cashType;

    @NotNull(message="账户类型：不可为空！")
    @Pattern(regexp="[12]",message="账户类型：取值为【1-对私，2-对公】！")
    private String accountType;

    @NotNull(message="身份证号：不可为空！")
    private String idNo;

    @NotNull(message="通道类型：不可为空！")
    @Pattern(regexp="[123]",message="通道类型：取值为【1-银行，2-微信，3-支付宝】！")
    private String channelType;

    @NotNull(message="账户名称：不可为空！")
    private String accountName;

    @NotNull(message="账户号：不可为空！")
    private String accountNo;

    @NotNull(message="开户行：不可为空！")
    private String accountBank;

    @NotNull(message="金额：不可为空！")
    @Min(value=500,message="金额：最小值为500分（5元）！")
    private Long cashAmount;
    
    private Long cashFee;

    private Integer applyOpr;

    private Date applyTime;

    private String status;

    private Integer updateOpr;
    
    private Date updateTime;
    
    private String memo;

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public Integer getVipId() {
        return vipId;
    }

    public void setVipId(Integer vipId) {
        this.vipId = vipId;
    }

    public String getCashType() {
        return cashType;
    }

    public void setCashType(String cashType) {
        this.cashType = cashType == null ? null : cashType.trim();
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType == null ? null : accountType.trim();
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo == null ? null : idNo.trim();
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType == null ? null : channelType.trim();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName == null ? null : accountName.trim();
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo == null ? null : accountNo.trim();
    }

    public String getAccountBank() {
        return accountBank;
    }

    public void setAccountBank(String accountBank) {
        this.accountBank = accountBank == null ? null : accountBank.trim();
    }

    public Long getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(Long cashAmount) {
        this.cashAmount = cashAmount;
    }

    public Long getCashFee() {
		return cashFee;
	}

	public void setCashFee(Long cashFee) {
		this.cashFee = cashFee;
	}

	public Integer getApplyOpr() {
        return applyOpr;
    }

    public void setApplyOpr(Integer applyOpr) {
        this.applyOpr = applyOpr;
    }

    public String getApplyTime() {
		if(applyTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(applyTime);
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getStatus() {
        return status;
    }

    
    public Integer getUpdateOpr() {
		return updateOpr;
	}

	public void setUpdateOpr(Integer updateOpr) {
		this.updateOpr = updateOpr;
	}

	public String getUpdateTime() {
		if(updateTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(updateTime);
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }
}