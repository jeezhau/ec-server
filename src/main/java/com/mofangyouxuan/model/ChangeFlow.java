package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

public class ChangeFlow {
    private String flowId;

    private Integer vipId;

    private String changeType;

    private BigDecimal amount;

    private Date createTime;

    private Integer createOpr;

    private String reason;
    
    private String sumFlag;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId == null ? null : flowId.trim();
    }

    public Integer getVipId() {
        return vipId;
    }

    public void setVipId(Integer vipId) {
        this.vipId = vipId;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType == null ? null : changeType.trim();
    }
    
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCreateOpr() {
        return createOpr;
    }

    public void setCreateOpr(Integer createOpr) {
        this.createOpr = createOpr;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

	public String getSumFlag() {
		return sumFlag;
	}

	public void setSumFlag(String sumFlag) {
		this.sumFlag = sumFlag;
	}
    
    
}