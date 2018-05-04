package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Goods {
	//@NotNull(message=" 商品ID：不可为空！")
    private Long goodsId;

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
    
    @NotNull(message=" 商品产地：不可为空！ ")
    @Size(min=2,max=100,message=" 商品产地：长度范围2-100字符！ ")
    private String place;
    
    @NotNull(message=" 商品生产者：不可为空！ ")
    @Size(min=2,max=100,message=" 商品生产者：长度范围2-100字符！ ")
    private String vender;

    private Integer saledCnt;

    @NotNull(message=" 规格明细：不可为空！ ")
    @Size(min=3,max=2550,message=" 规格明细：长度范围3-2550字符！ ")
    private String specDetail;
    
    private BigDecimal priceLowest;
    
    @Max(value = 999999, message = " 商品库存：最大值为 999999 ！") 
    @Min(value = 0 ,message= " 商品库存：最小值为0！" ) 
    private Integer stockSum;

    @NotNull(message=" 限购数量：不可为空！ ")
    @Max(value = 999999, message = " 限购数量：最大值为 999999 ！") 
    @Min(value= 0 ,message= " 限购数量：最小值为0！" )  
    private Integer limitedNum;

    private Date beginTime;

    private Date endTime;

    @NotNull(message=" 运费模版组ID：不可为空！ ")
    @Size(min=1,max=100,message=" 运费模版组ID：长度范围1-100字符！ ")
    private String postageIds;

    private Date updateTime;

    private String reviewResult;

    private String reviewLog;

    private Integer reviewOpr;

    private Date reviewTime;

    @NotNull(message=" 商品状态： 不可为空！ ")
    @Pattern(regexp="^[01]$",message=" 商品状态：取值为【0-待上架，1-上架】！ ")
    private String status;

    private String memo;
    
    private PartnerBasic partner;

    public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
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

    
    public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place == null ? null : place.trim();
	}

	public String getVender() {
		return vender;
	}

	public void setVender(String vender) {
		this.vender = vender == null ? null : vender.trim();
	}

	public Integer getSaledCnt() {
        return saledCnt;
    }

    public void setSaledCnt(Integer saledCnt) {
        this.saledCnt = saledCnt;
    }

    public String getSpecDetail() {
		return specDetail;
	}

	public void setSpecDetail(String specDetail) {
		this.specDetail = specDetail;
	}

	public BigDecimal getPriceLowest() {
		return priceLowest;
	}

	public void setPriceLowest(BigDecimal priceLowest) {
		this.priceLowest = priceLowest;
	}

	public Integer getStockSum() {
		return stockSum;
	}

	public void setStockSum(Integer stockSum) {
		this.stockSum = stockSum;
	}

	public Integer getLimitedNum() {
        return limitedNum;
    }

    public void setLimitedNum(Integer limitedNum) {
        this.limitedNum = limitedNum;
    }

    public String getBeginTime() {
		if(this.beginTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(beginTime);
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
		if(this.endTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(endTime);
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getPostageIds() {
        return this.postageIds;
    }

    public void setPostageIds(String postageIds) {
        this.postageIds = postageIds == null ? null : postageIds.trim();
    }

    public String getUpdateTime() {
		if(this.updateTime == null) {
			return null;
		}
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(updateTime);
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

    public String getReviewTime() {
    		if(this.reviewTime == null) {
    			return null;
    		}
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(reviewTime);
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

	public PartnerBasic getPartner() {
		return partner;
	}

	public void setPartner(PartnerBasic partner) {
		this.partner = partner;
	}
    
}