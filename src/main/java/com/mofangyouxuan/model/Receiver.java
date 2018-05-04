package com.mofangyouxuan.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Receiver {
	
	@NotNull(message=" 收货信息ID：不可为空！")
	@Min(value=0,message=" 收货信息ID：取值正整数！")
    private Long recvId;

	@NotNull(message=" 拥有者ID：不可为空！")
    private Integer userId;

	@NotNull(message=" 国家：不可为空！")
	@Size(min=2,max=100,message=" 国家：长度为2-100字符！")
    private String country;

	@NotNull(message=" 省份：不可为空！")
	@Size(min=2,max=100,message=" 省份：长度为2-100字符！")
    private String province;

	@NotNull(message=" 市：不可为空！")
	@Size(min=2,max=100,message=" 市：长度为2-100字符！")
    private String city;

	@NotNull(message=" 区县：不可为空！")
	@Size(min=2,max=100,message=" 区县：长度为2-100字符！")
    private String area;

	@NotNull(message=" 详细地址：不可为空！")
	@Size(min=2,max=255,message=" 详细地址：长度为2-255字符！")
    private String addr;

    private String locationX;

    private String locationY;

	@NotNull(message=" 收货人：不可为空！")
	@Size(min=2,max=50,message=" 收货人：长度为2-50字符！")
    private String receiver;

	@NotNull(message=" 联系电话：不可为空！")
	@Size(min=6,max=20,message=" 联系电话：长度为6-20字符！")
    private String phone;

	@NotNull(message=" 是否设为默认：不可为空！")
	@Pattern(regexp="^[01]$",message=" 是否设为默认：取值范围[0-否，1-是]！")
    private String isDefault;

    public Long getRecvId() {
        return recvId;
    }

    public void setRecvId(Long recvId) {
        this.recvId = recvId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area == null ? null : area.trim();
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr == null ? null : addr.trim();
    }

    public String getLocationX() {
        return locationX;
    }

    public void setLocationX(String locationX) {
        this.locationX = locationX == null ? null : locationX.trim();
    }

    public String getLocationY() {
        return locationY;
    }

    public void setLocationY(String locationY) {
        this.locationY = locationY == null ? null : locationY.trim();
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver == null ? null : receiver.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault == null ? null : isDefault.trim();
    }
}