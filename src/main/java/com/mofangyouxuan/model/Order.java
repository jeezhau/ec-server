package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class Order {
    private BigInteger orderId;

    private Integer userId;

    private Long goodsId;

    private String goodsSpec;

    private BigDecimal carrage;

    private BigDecimal amount;

    private Date createTime;

    private String remark;

    private String status;

    private String dispatchMode;

    private Long postageId;
    
    private Long recvId;

    private String recvName;

    private String recvPhone;

    private String recvCountry;

    private String recvProvince;

    private String recvCity;

    private String recvArea;

    private String recvAddr;

    private String logisticsComp;

    private String logisticsNo;

    private Date sendTime;

    private String signUser;

    private Date signTime;

    private Integer scoreLogistics;

    private Integer scoreGoods;

    private Integer scoreMerchant;

    private String appraiseStatus;

    private Date appraiseTime;

    private Date aftersalesApplyTime;

    private Date aftersalesDealTime;
    
    private String appraiseInfo;

    private String aftersalesReason;

    private String aftersalesResult;
    
    //补充字段，仅方便商品显示
    private String goodsName;
    
    private String goodsMainImgPath;

    private Integer partnerId;
    
    private String partnerBusiName;
    
    private String userPhone;
    
    private String headimgurl;
    
    private String nickname;

    public BigInteger getOrderId() {
        return orderId;
    }

    public void setOrderId(BigInteger orderId) {
        this.orderId = orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsSpec() {
        return goodsSpec;
    }

    public void setGoodsSpec(String goodsSpec) {
        this.goodsSpec = goodsSpec == null ? null : goodsSpec.trim();
    }

    public BigDecimal getCarrage() {
        return carrage;
    }

    public void setCarrage(BigDecimal carrage) {
        this.carrage = carrage;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getDispatchMode() {
        return dispatchMode;
    }

    public void setDispatchMode(String dispatchMode) {
        this.dispatchMode = dispatchMode == null ? null : dispatchMode.trim();
    }

    public Long getPostageId() {
        return postageId;
    }

    public void setPostageId(Long postageId) {
        this.postageId = postageId;
    }

    public String getRecvName() {
        return recvName;
    }

    public void setRecvName(String recvName) {
        this.recvName = recvName == null ? null : recvName.trim();
    }

    public String getRecvPhone() {
        return recvPhone;
    }

    public void setRecvPhone(String recvPhone) {
        this.recvPhone = recvPhone == null ? null : recvPhone.trim();
    }

    public String getRecvCountry() {
        return recvCountry;
    }

    public void setRecvCountry(String recvCountry) {
        this.recvCountry = recvCountry == null ? null : recvCountry.trim();
    }

    public String getRecvProvince() {
        return recvProvince;
    }

    public void setRecvProvince(String recvProvince) {
        this.recvProvince = recvProvince == null ? null : recvProvince.trim();
    }

    public String getRecvCity() {
        return recvCity;
    }

    public void setRecvCity(String recvCity) {
        this.recvCity = recvCity == null ? null : recvCity.trim();
    }

    public String getRecvArea() {
        return recvArea;
    }

    public void setRecvArea(String recvArea) {
        this.recvArea = recvArea == null ? null : recvArea.trim();
    }

    public String getRecvAddr() {
        return recvAddr;
    }

    public void setRecvAddr(String recvAddr) {
        this.recvAddr = recvAddr == null ? null : recvAddr.trim();
    }

    public String getLogisticsComp() {
        return logisticsComp;
    }

    public void setLogisticsComp(String logisticsComp) {
        this.logisticsComp = logisticsComp == null ? null : logisticsComp.trim();
    }

    public String getLogisticsNo() {
        return logisticsNo;
    }

    public void setLogisticsNo(String logisticsNo) {
        this.logisticsNo = logisticsNo == null ? null : logisticsNo.trim();
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getSignUser() {
        return signUser;
    }

    public void setSignUser(String signUser) {
        this.signUser = signUser == null ? null : signUser.trim();
    }

    public Date getSignTime() {
        return signTime;
    }

    public void setSignTime(Date signTime) {
        this.signTime = signTime;
    }

    public Integer getScoreLogistics() {
        return scoreLogistics;
    }

    public void setScoreLogistics(Integer scoreLogistics) {
        this.scoreLogistics = scoreLogistics;
    }

    public Integer getScoreGoods() {
        return scoreGoods;
    }

    public void setScoreGoods(Integer scoreGoods) {
        this.scoreGoods = scoreGoods;
    }

    public Integer getScoreMerchant() {
        return scoreMerchant;
    }

    public void setScoreMerchant(Integer scoreMerchant) {
        this.scoreMerchant = scoreMerchant;
    }

    public String getAppraiseStatus() {
        return appraiseStatus;
    }

    public void setAppraiseStatus(String appraiseStatus) {
        this.appraiseStatus = appraiseStatus == null ? null : appraiseStatus.trim();
    }

    public Date getAppraiseTime() {
        return appraiseTime;
    }

    public void setAppraiseTime(Date appraiseTime) {
        this.appraiseTime = appraiseTime;
    }

    public Date getAftersalesApplyTime() {
        return aftersalesApplyTime;
    }

    public void setAftersalesApplyTime(Date aftersalesApplyTime) {
        this.aftersalesApplyTime = aftersalesApplyTime;
    }

    public Date getAftersalesDealTime() {
        return aftersalesDealTime;
    }

    public void setAftersalesDealTime(Date aftersalesDealTime) {
        this.aftersalesDealTime = aftersalesDealTime;
    }
    

    public String getAppraiseInfo() {
        return appraiseInfo;
    }

    public void setAppraiseInfo(String appraiseInfo) {
        this.appraiseInfo = appraiseInfo == null ? null : appraiseInfo.trim();
    }

    public String getAftersalesReason() {
        return aftersalesReason;
    }

    public void setAftersalesReason(String aftersalesReason) {
        this.aftersalesReason = aftersalesReason == null ? null : aftersalesReason.trim();
    }

    public String getAftersalesResult() {
        return aftersalesResult;
    }

    public void setAftersalesResult(String aftersalesResult) {
        this.aftersalesResult = aftersalesResult == null ? null : aftersalesResult.trim();
    }

	public Long getRecvId() {
		return recvId;
	}

	public void setRecvId(Long recvId) {
		this.recvId = recvId;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getGoodsMainImgPath() {
		return goodsMainImgPath;
	}

	public void setGoodsMainImgPath(String goodsMainImgPath) {
		this.goodsMainImgPath = goodsMainImgPath;
	}

	public Integer getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Integer partnerId) {
		this.partnerId = partnerId;
	}

	public String getPartnerBusiName() {
		return partnerBusiName;
	}

	public void setPartnerBusiName(String partnerBusiName) {
		this.partnerBusiName = partnerBusiName;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgpath(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
    
	
}
