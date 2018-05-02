package com.mofangyouxuan.model;

import java.math.BigDecimal;

/**
 * 商品规格信息
 * @author jeekhan
 *
 */
public class GoodsSpec {
	
	private String name;
	
	private Integer val;
	
	private String unit;
	
	private BigDecimal price;
	
	private Integer stock;

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

	
}
