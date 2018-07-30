package com.mofangyouxuan.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class PartnerSettle {
    private Integer partnerId;

    @NotNull(message="是否退支付手续费：不可为空！")
    @Pattern(regexp="[01]",message="是否退支付手续费：取值【0-否，1-是】！")
    private String isRetfee;

    private BigDecimal serviceFeeRate;

    private BigDecimal shareProfitRate;

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public String getIsRetfee() {
        return isRetfee;
    }

    public void setIsRetfee(String isRetfee) {
        this.isRetfee = isRetfee == null ? null : isRetfee.trim();
    }

    public BigDecimal getServiceFeeRate() {
        return serviceFeeRate;
    }

    public void setServiceFeeRate(BigDecimal serviceFeeRate) {
        this.serviceFeeRate = serviceFeeRate;
    }

    public BigDecimal getShareProfitRate() {
        return shareProfitRate;
    }

    public void setShareProfitRate(BigDecimal shareProfitRate) {
        this.shareProfitRate = shareProfitRate;
    }
}