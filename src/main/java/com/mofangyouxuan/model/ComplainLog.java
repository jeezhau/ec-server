package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ComplainLog {
	@NotNull(message=" 投诉ID：不可为空！")
	@Min(value=0,message=" 投诉ID：最小值为0，表示新增！")
    private Integer cplanId;

	@NotNull(message="投诉人ID：不可为空！")
    private Integer oprId;
	
	private Integer oprPid;

	@NotNull(message="投诉对象类型：不可为空！")
	@Pattern(regexp="[12]",message="投诉对象类型：取值为【1-商品订单，2-合作伙伴】！")
	private String cpType;
	
    private Integer partnerId;

    private Long goodsId;

    private String orderId;

    @NotNull(message="投诉内容：不可为空！")
    @Size(min=10,max=1000,message="投诉内容：长度范围【10-1000】字符！")
    private String content;

    @NotNull(message="投诉人电话：不可为空！")
    private String phone;

    private Date createTime;

    private Date dealTime;

    private Integer dealOpr;

    private Date revisitTime;

    private Integer revisitOpr;

    private String status;

    private String dealLog;

    private String revisitLog;

    public String getDealLog() {
        return dealLog;
    }

    public void setDealLog(String dealLog) {
        this.dealLog = dealLog == null ? null : dealLog.trim();
    }

    public String getRevisitLog() {
        return revisitLog;
    }

    public void setRevisitLog(String revisitLog) {
        this.revisitLog = revisitLog == null ? null : revisitLog.trim();
    }
    
    public Integer getCplanId() {
        return cplanId;
    }

    public void setCplanId(Integer cplanId) {
        this.cplanId = cplanId;
    }

    public Integer getOprId() {
		return oprId;
	}

	public void setOprId(Integer oprId) {
		this.oprId = oprId;
	}

	public Integer getOprPid() {
		return oprPid;
	}

	public void setOprPid(Integer oprPid) {
		this.oprPid = oprPid;
	}

	public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
    		this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getCreateTime() {
    		if(createTime == null) {
    			return null;
    		}
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(createTime);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDealTime() {
	    	if(dealTime == null) {
				return null;
			}
	    return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dealTime);
    }

    public void setDealTime(Date dealTime) {
        this.dealTime = dealTime;
    }

    public Integer getDealOpr() {
        return dealOpr;
    }

    public void setDealOpr(Integer dealOpr) {
        this.dealOpr = dealOpr;
    }

    public String getRevisitTime() {
	    	if(revisitTime == null) {
				return null;
			}
	    return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(revisitTime);
    }

    public void setRevisitTime(Date revisitTime) {
        this.revisitTime = revisitTime;
    }

    public Integer getRevisitOpr() {
        return revisitOpr;
    }

    public void setRevisitOpr(Integer revisitOpr) {
        this.revisitOpr = revisitOpr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

	public String getCpType() {
		return cpType;
	}

	public void setCpType(String cpType) {
		this.cpType = cpType;
	}
    
 
}