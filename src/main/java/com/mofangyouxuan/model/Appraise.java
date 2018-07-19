package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Appraise {
    private Integer apprId;

    private Long goodsId;

    private String orderId;

    private String object;

    private Integer scoreLogistics;

    private Integer scoreGoods;

    private Integer scoreMerchant;

    private Integer scoreUser;

    private Date updateTime;

    private String status;

    private String content;

    //补充字段，仅方便商品显示
    private Integer userId;
    private String goodsSpec;
    private String goodsName;
    private String goodsMainImgPath;
    private Integer partnerId;
    private String headimgurl;
    private String nickname;
    
    public Integer getApprId() {
        return apprId;
    }

    public void setApprId(Integer apprId) {
        this.apprId = apprId;
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

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object == null ? null : object.trim();
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

    public Integer getScoreUser() {
        return scoreUser;
    }

    public void setScoreUser(Integer scoreUser) {
        this.scoreUser = scoreUser;
    }

    public String  getUpdateTime() {
    		if(updateTime == null) {
    			return null;
    		}
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(updateTime);
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getGoodsSpec() {
		return goodsSpec;
	}

	public void setGoodsSpec(String goodsSpec) {
		this.goodsSpec = goodsSpec;
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

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
    
    
}