package com.mofangyouxuan.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class LoginLog {
	
	@NotNull(message=" 用户标志：用户ID、电话、openid、邮箱不可为空！")
    private String userId;
    
    private Integer partnerId;

    @Size(max=50,message=" 源IP：字符超长！")
    private String ip;

    @Size(max=255,message=" 引用URL：字符超长！")
    private String referer;

    private Date crtTime;

    @Pattern(regexp="[123]",message=" 终端类型：取值不正确！")
    private String source;

    private String isSucc;
    
    @Size(max=100,message=" SESIIONID：字符超长！")
    private String sessionid;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Integer partnerId) {
		this.partnerId = partnerId;
	}

	public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer == null ? null : referer.trim();
    }

    public Date getCrtTime() {
        return crtTime;
    }

    public void setCrtTime(Date crtTime) {
        this.crtTime = crtTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getIsSucc() {
        return isSucc;
    }

    public void setIsSucc(String isSucc) {
        this.isSucc = isSucc;
    }

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}
    
    
}