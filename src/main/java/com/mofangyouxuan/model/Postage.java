package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

public class Postage {
    private Integer id;

    private Integer partnerId;

    private String name;

    private String isCityWide;

    private Integer distLimit;

    private String provLimit;

    private String isFree;

    private Integer freeWeight;

    private BigDecimal freeAmount;

    private Integer freeDist;

    private Integer firstDist;

    private Integer firstWenght;

    private BigDecimal firstPrice;

    private Integer additionWeight;

    private Integer additionDist;

    private BigDecimal additionPrice;

    private Date updateTime;

    private Integer updateOpr;

    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
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

    public BigDecimal getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(BigDecimal firstPrice) {
        this.firstPrice = firstPrice;
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

    public BigDecimal getAdditionPrice() {
        return additionPrice;
    }

    public void setAdditionPrice(BigDecimal additionPrice) {
        this.additionPrice = additionPrice;
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