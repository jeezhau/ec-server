package com.mofangyouxuan.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserBasic {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
    private Integer id;

    @NotNull
    @Size(min=3,max=20,message=" nickname:length range 6-20！")
    private String nickname;

    @Size(max=100,message=" email:length range 6-100! ")
    @Email(message="email：incorrent format! ")
    private String email;

    private Date birthday;

    @Pattern(regexp="^[012]$",message=" sex:value scope[0-secret，1-boy，2-gril]！")
    private String sex;

    @Size(max=20,message=" passwd:max length 20 character! ")
    private String passwd;

    @Size(max=100,message=" country:max length 100 character! ")
    private String country;
    
    @Size(max=100,message=" province:max length 100 character! ")
    private String province;

    @Size(max=100,message=" city:max length 100 character! ")
    private String city;

    @Size(max=200,message=" favourite:max length 200 character! ")
    private String favourite;

    @Size(max=100,message=" profession:max length 100 character! ")
    private String profession;

    @Size(max=600,message=" introduce:max length 600 character! ")
    private String introduce;

    private String headimgurl;

    @Size(max=20,message=" phone:max length 20 character! ")
    private String phone;

    @NotNull(message=" regist_type：not null! ")
    @Pattern(regexp="^[12]$",message=" regist_type:value scope[1-official ，2-weichat]！")
    private String registType;

    @Size(max=100,message=" openId:max length 100 character! ")
    private String openId;

    @Size(max=100,message=" unionId:max length 100 character! ")
    private String unionId;

    private Integer senceId;

    private Date registTime;

    @Null
    private Date updateTime;

    @Null
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getBirthday() {
        return birthday;
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