package com.mofangyouxuan.model;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class SettleAccount {
	
	@NotNull(message=" 账户ID：不可为空！")
	@Min(value=0,message="账户ID：最小值为0，标志新增！")
    private Long settleId;

    @NotNull(message=" 会员ID：不可为空！")
    private Integer vipId;

    @NotNull(message=" 提现方式：不可为空！")
    @Pattern(regexp="[123]",message=" 提现方式：取值为【1-手动，2-每周，3-每月】！")
    private String cashType;

    @NotNull(message=" 账户类型：不可为空！")
    @Pattern(regexp="[12]",message=" 账户类型：取值为【1-对私，2-对公】！")
    private String accountType;

    @NotNull(message=" 通道类型：不可为空！")
    @Pattern(regexp="[123]",message=" 通道类型：取值为【1-银行，2-微信，3-支付宝】！")
    private String channelType;

    @NotNull(message=" 身份证号码：不可为空！")
    @Pattern(regexp="[1-9]\\d{16}[0-9Xx]",message=" 身份证号码：格式不正确！")
    private String idNo;

    @NotNull(message=" 账户名：不可为空！")
    @Size(min=2,max=100,message="账户名：长度范围【2-100】！")
    private String accountName;

    @NotNull(message=" 账户号：不可为空！")
    @Size(min=3,max=100,message="账户号：长度范围【3-100】！")
    private String accountNo;

    @NotNull(message=" 开户行：不可为空！")
    @Size(min=2,max=100,message="开户行：长度范围【2-100】！")
    private String accountBank;

    private Date updateTime;

    private String status;

    public Long getSettleId() {
        return settleId;
    }

    public void setSettleId(Long settleId) {
        this.settleId = settleId;
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

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType == null ? null : channelType.trim();
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo == null ? null : idNo.trim();
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}