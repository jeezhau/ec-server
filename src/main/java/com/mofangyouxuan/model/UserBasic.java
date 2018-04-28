package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserBasic {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
    private Integer userId;

    @NotNull(message=" 昵称： 不可为空！")
    @Size(min=3,max=20,message=" 昵称:长度范围 6-20字符！")
    private String nickname;

    @Size(max=100,message=" 邮箱：长度范围 6-100字符! ")
    @Email(message=" 邮箱：格式不正确! ")
    private String email;

    private Date birthday;

    @Pattern(regexp="^[012]$",message=" 性别：取值范围[0-保密，1-男，2-女]！")
    private String sex;

    @Size(max=20,message=" 密码： 最长20个字符! ")
    private String passwd;

    @Size(max=100,message=" 国家：最长 100 字符! ")
    private String country;
    
    @Size(max=100,message=" 省份： 最长 100 字符! ")
    private String province;

    @Size(max=100,message=" 城市：最长 100 字符! ")
    private String city;

    @Size(max=200,message=" 兴趣爱好： 最长 200 字符! ")
    private String favourite;

    @Size(max=100,message=" 职业：最长 100 字符! ")
    private String profession;

    @Size(max=600,message=" 个人简介： 最长 600 字符! ")
    private String introduce;

    private String headimgurl;

    @Size(max=20,message=" 移动电话： 最长 20 字符! ")
    private String phone;

    @NotNull(message=" 注册方式：不可为空! ")
    @Pattern(regexp="^[12]$",message=" 注册方式：取值范围[1-官方 ，2-微信]！")
    private String registType;

    private String openId;

    private String unionId;

    private Integer senceId;

    private Date registTime;

    private Date updateTime;

    private String status;

    public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getBirthday() {
    		if(this.birthday != null) {
			return new SimpleDateFormat("yyyy-MM-dd").format(birthday);
		}else {
			return null;
		}
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd == null ? null : passwd.trim();
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

    public String getFavourite() {
        return favourite;
    }

    public void setFavourite(String favourite) {
        this.favourite = favourite == null ? null : favourite.trim();
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession == null ? null : profession.trim();
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce == null ? null : introduce.trim();
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl == null ? null : headimgurl.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getRegistType() {
        return registType;
    }

    public void setRegistType(String registType) {
        this.registType = registType == null ? null : registType.trim();
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId == null ? null : unionId.trim();
    }

    public Integer getSenceId() {
        return senceId;
    }

    public void setSenceId(Integer senceId) {
        this.senceId = senceId;
    }

    public String getRegistTime() {
    		if(this.registTime != null) {
    			return sdf.format(registTime);
    		}else {
    			return null;
    		}
    }

    public void setRegistTime(Date registTime) {
        this.registTime = registTime;
    }

    public String getUpdateTime() {
    	if(this.updateTime != null) {
			return sdf.format(this.updateTime);
		}else {
			return null;
		}
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
}