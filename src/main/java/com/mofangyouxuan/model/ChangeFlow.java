package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChangeFlow {
    private String flowId;

    private Integer vipId;

    private String changeType;

    private Long amount;

    private Date createTime;

    private Integer createOpr;

    private String reason;
    
    private String sumFlag;
    
    private Date sumTime;
    
    private String orderId;

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
    
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCreateTime() {
    		if( createTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime);
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

	public String getSumTime() {
		if( sumTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sumTime);
	}

	public void setSumTime(Date sumTime) {
		this.sumTime = sumTime;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
    
}