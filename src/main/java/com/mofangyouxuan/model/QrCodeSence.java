package com.mofangyouxuan.model;

import java.util.Date;

public class QrCodeSence {
    private Integer userId;

    private Integer senceId;

    private Integer expireSeconds;

    private String ticket;

    private String url;

    private Date createTime;
    
    private String wxmpPicnane;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSenceId() {
        return senceId;
    }

    public void setSenceId(Integer senceId) {
        this.senceId = senceId;
    }

    public Integer getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket == null ? null : ticket.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

	public String getWxmpPicnane() {
		return wxmpPicnane;
	}

	public void setWxmpPicnane(String wxmpPicnane) {
		this.wxmpPicnane = wxmpPicnane;
	}
    
    
}