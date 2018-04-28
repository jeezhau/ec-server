package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Postage {
	
    private Long postageId;

    @NotNull(message=" 合作伙伴ID：不可为空！ ")
    private Integer partnerId;

    @NotNull(message=" 模版名称：不可为空！ ")
    @Size(min=2,max=20,message=" 模版名称：长度范围2-20字符！ ")
    private String postageName;

    @NotNull(message=" 配送范围：不可为空！ ")
    @Pattern(regexp="^[01]$",message=" 配送范围：取值范围[0-全国，1-同城]")
    private String isCityWide;

    @Min(0)
    
    private Integer distLimit;

    private String provLimit;

    @NotNull(message=" 是否免邮：不可为空！ ")
    @Pattern(regexp="^[01234]|{23|24|34|234}$",message=" 是否免邮：取值范围[]0-不免邮，1-无条件免邮，2-重量限制，3-金额限制，4-距离限制，23-重量与金额限制，24-重量与距离限制，34-金额与距离，234-重量金额距离限制")
    private String isFree;

    private Integer freeWeight;

    private BigDecimal freeAmount;

    private Integer freeDist;

    private Integer firstDist;

    private Integer firstWenght;

    private BigDecimal firstDPrice;

    private BigDecimal firstWPrice;

    private Integer additionWeight;

    private Integer additionDist;

    private BigDecimal additionDPrice;

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

    public Integer getFirstWenght() {
        return firstWenght;
    }

    public void setFirstWenght(Integer firstWenght) {
        this.firstWenght = firstWenght;
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