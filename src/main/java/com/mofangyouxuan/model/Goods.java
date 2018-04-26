package com.mofangyouxuan.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Goods {
    private Long id;

    @NotNull(message=" 合作伙伴ID：不可为空！ ")
    private Integer partnerId;

    @NotNull(message=" 商品分类ID：不可为空！ ")
    private Integer categoryId;

    @NotNull(message=" 商品名称：不可为空！ ")
    @Size(min=3,max=100,message=" 商品名称：长度范围2-100字符！ ")
    private String goodsName;

    @NotNull(message=" 商品图文描述：不可为空！ ")
    @Size(min=10,max=10000,message=" 商品图文描述：长度范围10-10000字符！ ")
    private String goodsDesc;

    @NotNull(message=" 商品主图路径：不可为空！ ")
    @Size(min=10,max=250,message=" 商品主图路径：长度范围3-255字符！ ")
    private String mainImgPath;

    @NotNull(message=" 商品轮播图路径组：不可为空！ ")
    @Size(min=3,max=2550,message=" 商品轮播图路径组：长度范围3-2550字符！ ")
    private String carouselImgPaths;

    private Integer saledCnt;

    private Integer stock;

    @NotNull(message=" 限购数量：不可为空！ ")
    private Integer limitedNum;

    private Date beginTime;

    private Date endTime;

    @NotNull(message=" 配送方式：不可为空！ ")
    @Pattern(regexp="^[1234]$",message=" 配送方式：取值为【1-官方统一配送、2-商家自行配送、3-快递配送、4-客户自取】！ ")
    private String dispatchMode;

    @NotNull(message=" 是否同城销售：不可为空！ ")
    @Pattern(regexp="^[01]$",message=" 是否同城销售：取值为【0-全国，1-同城】！ ")
    private String isCityWide;

    private Integer distLimit;

    private String provLimit;

    @NotNull(message=" 运费模版组：不可为空！ ")
    private String distrIds;

    private Date updateTime;

    private String reviewResult;

    private String reviewLog;

    private Integer reviewOpr;

    private Date reviewTime;

    @NotNull(message=" 商品状态： 不可为空！ ")
    @Pattern(regexp="^[01]$",message=" 商品状态：取值为【0-待上架，1-上架】！ ")
    private String status;

    private String memo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName == null ? null : goodsName.trim();
    }

    public String getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(String goodsDesc) {
        this.goodsDesc = goodsDesc == null ? null : goodsDesc.trim();
    }

    public String getMainImgPath() {
        return mainImgPath;
    }

    public void setMainImgPath(String mainImgPath) {
        this.mainImgPath = mainImgPath == null ? null : mainImgPath.trim();
    }

    public String getCarouselImgPaths() {
        return carouselImgPaths;
    }

    public void setCarouselImgPaths(String carouselImgPaths) {
        this.carouselImgPaths = carouselImgPaths == null ? null : carouselImgPaths.trim();
    }

    public Integer getSaledCnt() {
        return saledCnt;
    }

    public void setSaledCnt(Integer saledCnt) {
        this.saledCnt = saledCnt;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getLimitedNum() {
        return limitedNum;
    }

    public void setLimitedNum(Integer limitedNum) {
        this.limitedNum = limitedNum;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getDispatchMode() {
        return dispatchMode;
    }

    public void setDispatchMode(String dispatchMode) {
        this.dispatchMode = dispatchMode == null ? null : dispatchMode.trim();
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

    public String getDistrIds() {
        return distrIds;
    }

    public void setDistrIds(String distrIds) {
        this.distrIds = distrIds == null ? null : distrIds.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(String reviewResult) {
        this.reviewResult = reviewResult == null ? null : reviewResult.trim();
    }

    public String getReviewLog() {
        return reviewLog;
    }

    public void setReviewLog(String reviewLog) {
        this.reviewLog = reviewLog == null ? null : reviewLog.trim();
    }

    public Integer getReviewOpr() {
        return reviewOpr;
    }

    public void setReviewOpr(Integer reviewOpr) {
        this.reviewOpr = reviewOpr;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }
}