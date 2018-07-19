package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Aftersale {

    private Long goodsId;

    private String orderId;

    private Date applyTime;

    private Date dealTime;

    private String applyReason;

    private String dealResult;


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

    public String getApplyTime() {
    		if(applyTime == null) {
    			return null;
    		}
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(applyTime);
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }


    public String getDealTime() {
    		if(dealTime == null) {
			return null;
		}
    		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dealTime);
    }

    public void setDealTime(Date dealTime) {
        this.dealTime = dealTime;
    }
    
    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason == null ? null : applyReason.trim();
    }

    public String getDealResult() {
        return dealResult;
    }

    public void setDealResult(String dealResult) {
        this.dealResult = dealResult == null ? null : dealResult.trim();
    }
}