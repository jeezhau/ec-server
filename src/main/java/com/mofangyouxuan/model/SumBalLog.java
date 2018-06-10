package com.mofangyouxuan.model;

import java.util.Date;

public class SumBalLog {
    private Integer vipId;

    private String beginFlow;

    private String endFlow;

    private Long amountAddbal;

    private Long amountSubbal;

    private Long amountAddfrz;

    private Long amountSubfrz;

    private Date createTime;

    public Integer getVipId() {
        return vipId;
    }

    public void setVipId(Integer vipId) {
        this.vipId = vipId;
    }

    public String getBeginFlow() {
        return beginFlow;
    }

    public void setBeginFlow(String beginFlow) {
        this.beginFlow = beginFlow == null ? null : beginFlow.trim();
    }

    public String getEndFlow() {
        return endFlow;
    }

    public void setEndFlow(String endFlow) {
        this.endFlow = endFlow == null ? null : endFlow.trim();
    }

    public Long getAmountAddbal() {
        return amountAddbal;
    }

    public void setAmountAddbal(Long amountAddbal) {
        this.amountAddbal = amountAddbal;
    }

    public Long getAmountSubbal() {
        return amountSubbal;
    }

    public void setAmountSubbal(Long amountSubbal) {
        this.amountSubbal = amountSubbal;
    }

    public Long getAmountAddfrz() {
        return amountAddfrz;
    }

    public void setAmountAddfrz(Long amountAddfrz) {
        this.amountAddfrz = amountAddfrz;
    }

    public Long getAmountSubfrz() {
        return amountSubfrz;
    }

    public void setAmountSubfrz(Long amountSubfrz) {
        this.amountSubfrz = amountSubfrz;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}