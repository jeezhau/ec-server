package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Collection {
	@NotNull(message=" 用户ID：不可为空！")
    private Integer userId;
	
	@NotNull(message=" 收藏类型：不可为空！")
	@Pattern(regexp="[12]",message=" 收藏类型：取值只可为【1-商家，2-商品】！")
    private String collType;
	
	@NotNull(message=" 收藏目标ID：不可为空！")
    private Integer relId;

    private Date createTime;
    
    //便于显示的商品信息
    private Integer goodsVipId;
    
    private Integer goodsPartnerId;
    
    private String goodsMainImg;
    
    private String goodsName;
    
    private String partnerIntroduce;
    
    private String partnerBusiName;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCollType() {
        return collType;
    }

    public void setCollType(String collType) {
        this.collType = collType == null ? null : collType.trim();
    }

    public Integer getRelId() {
        return relId;
    }

    public void setRelId(Integer relId) {
        this.relId = relId;
    }

    public String getCreateTime() {
    		if(this.createTime == null) {
    			return null;
    		}
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(createTime);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

	public Integer getGoodsVipId() {
		return goodsVipId;
	}

	public void setGoodsVipId(Integer goodsVipId) {
		this.goodsVipId = goodsVipId;
	}

	public Integer getGoodsPartnerId() {
		return goodsPartnerId;
	}

	public void setGoodsPartnerId(Integer goodsPartnerId) {
		this.goodsPartnerId = goodsPartnerId;
	}

	public String getGoodsMainImg() {
		return goodsMainImg;
	}

	public void setGoodsMainImg(String goodsMainImg) {
		this.goodsMainImg = goodsMainImg;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getPartnerIntroduce() {
		return partnerIntroduce;
	}

	public void setPartnerIntroduce(String partnerIntroduce) {
		this.partnerIntroduce = partnerIntroduce;
	}

	public String getPartnerBusiName() {
		return partnerBusiName;
	}

	public void setPartnerBusiName(String partnerBusiName) {
		this.partnerBusiName = partnerBusiName;
	}

	
    
}