package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品规格信息
 * @author jeekhan
 *
 */
public class GoodsSpec {
	
	private Long goodsId;

	private String name;
	
	private Integer val;
	
	private String unit;
	
	private Integer grossWeight; //带包装重量
	
	private BigDecimal price;
	
	private Integer stock; //库存

	private Integer buyNum; //购买数量
	
	private Date updateTime;

	private Integer updateOpr;

	
	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = (name == null ? "" : name.trim());
	}

	public Integer getVal() {
		return val;
	}

	public void setVal(Integer val) {
		this.val = val;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = (unit == null ? "" : unit.trim());
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = (price == null?null:price.setScale(2,BigDecimal.ROUND_DOWN));
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Integer getGrossWeight() {
		return grossWeight;
	}

	public void setGrossWeight(Integer grossWeight) {
		this.grossWeight = grossWeight;
	}

	public Integer getBuyNum() {
		return buyNum;
	}

	public void setBuyNum(Integer buyNum) {
		this.buyNum = buyNum;
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
	
}
