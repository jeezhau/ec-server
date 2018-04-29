package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Postage {
	
	//@NotNull(message=" 模版ID：不可为空！ ")
    private Long postageId;

    @NotNull(message=" 合作伙伴ID：不可为空！ ")
    private Integer partnerId;

    @NotNull(message=" 模版名称：不可为空！ ")
    @Size(min=2,max=20,message=" 模版名称：长度范围2-20字符！ ")
    private String postageName;

    @NotNull(message=" 配送范围：不可为空！ ")
    @Pattern(regexp="^[01]$",message=" 配送范围：取值范围[0-全国，1-同城]！")
    private String isCityWide;

    @Min(value=0,message=" 配送距离：最小值为0 ！")
    @Max(value=999,message=" 配送距离：最大值为999！")
    private Integer distLimit;

    @Size(max=1000,message=" 配送省份：长度最大为1000字符！")
    private String provLimit;

    @NotNull(message=" 是否免邮：不可为空！ ")
    @Pattern(regexp="^([01234])|(23|24|34|234)$",message=" 是否免邮：取值范围[0-不免邮，1-无条件免邮，2-重量限制，3-金额限制，4-距离限制，23-重量与金额限制，24-重量与距离限制，34-金额与距离，234-重量金额距离限制]！")
    private String isFree;

    @Min(value=1,message=" 免邮重量：最小值为1！")
    @Max(value=99999999,message=" 免邮重量：最大值为99999999！")
    private Integer freeWeight;

    @Min(value=1,message=" 免邮金额：最小值为1！")
    @Max(value=99999999,message=" 免邮金额：最大值为99999999！")
    private BigDecimal freeAmount;

    @Min(value=1,message=" 免邮距离：最小值为1！")
    @Max(value=999,message=" 免邮距离：最大值为999！")
    private Integer freeDist;

    @Min(value=1,message=" 首距：最小值为1！")
    @Max(value=999,message=" 首距：最大值为999！")
    private Integer firstDist;

    @Min(value=0,message=" 首距价格：最小值为0！")
    @Max(value=99999999,message=" 首距价格：最大值为99999999！")
    private BigDecimal firstDPrice;

    @Min(value=1,message=" 首重：最小值为1！")
    @Max(value=99999999,message=" 首重：最大值为99999999！")
    private Integer firstWeight;

    @Min(value=0,message=" 首重价格：最小值为0！")
    @Max(value=99999999,message=" 首重价格：最大值为99999999！")
    private BigDecimal firstWPrice;

    @Min(value=1,message=" 续重：最小值为1！")
    @Max(value=99999999,message=" 续重：最大值为99999999！")
    private Integer additionWeight;

    @Min(value=1,message=" 续距：最小值为1！")
    @Max(value=999,message=" 续距：最大值为999！")
    private Integer additionDist;

    @Min(value=0,message=" 续距价格：最小值为0！")
    @Max(value=99999999,message=" 续距价格：最大值为99999999！")
    private BigDecimal additionDPrice;

    @Min(value=0,message=" 续重价格：最小值为0！")
    @Max(value=99999999,message=" 续重价格：最大值为99999999！")
    private BigDecimal additionWPrice;

    private Date updateTime;

    private Integer updateOpr;

    private String status;

    public Long getPostageId() {
        return postageId;
    }

    public void setPostageId(Long postageId) {
        this.postageId = postageId;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public String getPostageName() {
        return postageName;
    }

    public void setPostageName(String postageName) {
        this.postageName = postageName == null ? null : postageName.trim();
    }

    public String getIsCityWide() {
        return isCityWide;
    }

    public void setIsCityWide(String isCityWide) {
        this.isCityWide = isCityWide == null ? null : isCityWide.trim();
    }

    public Integer getDistLimit() {
        return distLimit;
    }

    public void setDistLimit(Integer distLimit) {
        this.distLimit = distLimit;
    }

    public String getProvLimit() {
        return provLimit;
    }

    public void setProvLimit(String provLimit) {
        this.provLimit = provLimit == null ? null : provLimit.trim();
    }

    public String getIsFree() {
        return isFree;
    }

    public void setIsFree(String isFree) {
        this.isFree = isFree == null ? null : isFree.trim();
    }

    public Integer getFreeWeight() {
        return freeWeight;
    }

    public void setFreeWeight(Integer freeWeight) {
        this.freeWeight = freeWeight;
    }

    public BigDecimal getFreeAmount() {
        return freeAmount;
    }

    public void setFreeAmount(BigDecimal freeAmount) {
        this.freeAmount = freeAmount;
    }

    public Integer getFreeDist() {
        return freeDist;
    }

    public void setFreeDist(Integer freeDist) {
        this.freeDist = freeDist;
    }

    public Integer getFirstDist() {
        return firstDist;
    }

    public void setFirstDist(Integer firstDist) {
        this.firstDist = firstDist;
    }


    public Integer getFirstWeight() {
		return firstWeight;
	}

	public void setFirstWeight(Integer firstWeight) {
		this.firstWeight = firstWeight;
	}

	public BigDecimal getFirstDPrice() {
        return firstDPrice;
    }

    public void setFirstDPrice(BigDecimal firstDPrice) {
        this.firstDPrice = firstDPrice;
    }

    public BigDecimal getFirstWPrice() {
        return firstWPrice;
    }

    public void setFirstWPrice(BigDecimal firstWPrice) {
        this.firstWPrice = firstWPrice;
    }

    public Integer getAdditionWeight() {
        return additionWeight;
    }

    public void setAdditionWeight(Integer additionWeight) {
        this.additionWeight = additionWeight;
    }

    public Integer getAdditionDist() {
        return additionDist;
    }

    public void setAdditionDist(Integer additionDist) {
        this.additionDist = additionDist;
    }

    public BigDecimal getAdditionDPrice() {
        return additionDPrice;
    }

    public void setAdditionDPrice(BigDecimal additionDPrice) {
        this.additionDPrice = additionDPrice;
    }

    public BigDecimal getAdditionWPrice() {
        return additionWPrice;
    }

    public void setAdditionWPrice(BigDecimal additionWPrice) {
        this.additionWPrice = additionWPrice;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getUpdateOpr() {
        return updateOpr;
    }

    public void setUpdateOpr(Integer updateOpr) {
        this.updateOpr = updateOpr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}