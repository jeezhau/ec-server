package com.mofangyouxuan.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

public class PartnerBasic {
    private Integer id;

    @NotNull(message=" 绑定用户：不可为空！")
    private Integer userId;

    @NotNull(message=" 国家：不可为空！")
    @Size(min=2,max=50,message=" 国家：长度范围2-50字符 ！")
    private String country;

    @NotNull(message=" 省份：不可为空！")
    @Size(min=2,max=100,message=" 省份：长度范围2-100字符 ！")
    private String province;

    @NotNull(message=" 地级市：不可为空！")
    @Size(min=2,max=100,message=" 地级市：长度范围2-100字符 ！")
    private String city;

    @NotNull(message=" 县：不可为空！")
    @Size(min=2,max=100,message=" 县：长度范围2-100字符 ！")
    private String area;

    @NotNull(message=" 详细地址：不可为空！")
    @Size(min=2,max=100,message=" 详细地址：长度范围2-255字符 ！")
    private String addr;

    @NotNull(message=" 经营名称：不可为空！")
    @Size(min=3,max=30,message=" 经营名称：长度范围3-30字符 ！")
    private String busiName;

    @NotNull(message=" 法人姓名：不可为空！")
    @Size(min=2,max=100,message=" 法人姓名：长度范围2-100字符 ！")
    private String legalPername;

    @NotNull(message=" 法人省份证：不可为空！")
    @Size(min=18,max=18,message=" 法人省份证：长度18字符 ！")
    private String legalPeridno;

    @NotNull(message=" 公司名称：不可为空！")
    @Size(min=2,max=100,message=" 公司名称：长度范围2-100字符 ！")
    private String compName;

    
    private String licenceNo;

    @Null
    private String certDir;

    @NotNull(message=" 联系电话：不可为空！")
    @Size(min=11,max=11,message=" 联系电话：长度11字符 ！")
    private String phone;

    @NotNull(message=" 经营地纬度：不可为空！")
    private BigDecimal locationX;

    @NotNull(message=" 经营地经度：不可为空！")
    private BigDecimal locationY;

    @Null
    private String status;

    @Null
    private Date updateTime;

    private String reviewLog;

    private Integer reviewOpr;

    @Null
    private Date reviewTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getBusiName() {
        return busiName;
    }

    public void setBusiName(String busiName) {
        this.busiName = busiName == null ? null : busiName.trim();
    }

    public String getLegalPername() {
        return legalPername;
    }

    public void setLegalPername(String legalPername) {
        this.legalPername = legalPername == null ? null : legalPername.trim();
    }

    public String getLegalPeridno() {
        return legalPeridno;
    }

    public void setLegalPeridno(String legalPeridno) {
        this.legalPeridno = legalPeridno == null ? null : legalPeridno.trim();
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName == null ? null : compName.trim();
    }

    public String getLicenceNo() {
        return licenceNo;
    }

    public void setLicenceNo(String licenceNo) {
        this.licenceNo = licenceNo == null ? null : licenceNo.trim();
    }

    public String getCertDir() {
        return certDir;
    }

    public void setCertDir(String certDir) {
        this.certDir = certDir == null ? null : certDir.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public BigDecimal getLocationX() {
        return locationX;
    }

    public void setLocationX(BigDecimal locationX) {
        this.locationX = locationX;
    }

    public BigDecimal getLocationY() {
        return locationY;
    }

    public void setLocationY(BigDecimal locationY) {
        this.locationY = locationY;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
}
