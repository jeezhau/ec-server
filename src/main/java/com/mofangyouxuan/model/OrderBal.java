package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

public class OrderBal {
    private String orderId;

    private BigDecimal payAmount;

    private BigDecimal payFee;

    private String payType;

    private BigDecimal partnerSettle;

    private BigDecimal syssrvSettle;

    private BigDecimal spreaderUSettle;

    private BigDecimal spreaderPSettle;

    private BigDecimal ptoolsFee;

    private Date refundTime;

    private BigDecimal refundUserSettle;

    private BigDecimal refundPartnerSettle;

    private Date balTime;

    private String status;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public BigDecimal getPayFee() {
        return payFee;
    }

    public void setPayFee(BigDecimal payFee) {
        this.payFee = payFee;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType == null ? null : payType.trim();
    }

    public BigDecimal getPartnerSettle() {
        return partnerSettle;
    }

    public void setPartnerSettle(BigDecimal partnerSettle) {
        this.partnerSettle = partnerSettle;
    }

    public BigDecimal getSyssrvSettle() {
        return syssrvSettle;
    }

    public void setSyssrvSettle(BigDecimal syssrvSettle) {
        this.syssrvSettle = syssrvSettle;
    }

    public BigDecimal getSpreaderUSettle() {
        return spreaderUSettle;
    }

    public void setSpreaderUSettle(BigDecimal spreaderUSettle) {
        this.spreaderUSettle = spreaderUSettle;
    }

    public BigDecimal getSpreaderPSettle() {
        return spreaderPSettle;
    }

    public void setSpreaderPSettle(BigDecimal spreaderPSettle) {
        this.spreaderPSettle = spreaderPSettle;
    }

    public BigDecimal getPtoolsFee() {
        return ptoolsFee;
    }

    public void setPtoolsFee(BigDecimal ptoolsFee) {
        this.ptoolsFee = ptoolsFee;
    }

    public Date getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Date refundTime) {
        this.refundTime = refundTime;
    }

    public BigDecimal getRefundUserSettle() {
        return refundUserSettle;
    }

    public void setRefundUserSettle(BigDecimal refundUserSettle) {
        this.refundUserSettle = refundUserSettle;
    }

    public BigDecimal getRefundPartnerSettle() {
        return refundPartnerSettle;
    }

    public void setRefundPartnerSettle(BigDecimal refundPartnerSettle) {
        this.refundPartnerSettle = refundPartnerSettle;
    }

    public Date getBalTime() {
        return balTime;
    }

    public void setBalTime(Date balTime) {
        this.balTime = balTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}