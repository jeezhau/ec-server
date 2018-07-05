package com.mofangyouxuan.model;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class PartnerStaff {
	
	@NotNull(message="记录ID：不可为空！")
	@Min(value=0,message=" 记录ID：最小值为0，标志新增！")
    private Long recId;

	@NotNull(message="用户ID：不可为空！")
    private Integer userId;

	@NotNull(message="合作伙伴ID：不可为空！")
    private Integer partnerId;

	@Size(max=30,message="内部员工ID：最长30个字符！")
    private String staffId;

	@Size(max=20,message="员工昵称：最长20个字符！")
    private String nickname;

	@Size(max=100,message="员工邮箱：最长100个字符！")
    private String email;

    private String passwd;

    @Size(max=100,message="员工介绍：最长600个字符！")
    private String introduce;

    @Null
    private String headimgurl;

    @Size(max=20,message="电话号码：最长100个字符！")
    private String phone;

    @Pattern(regexp="[01]",message="是否客服：取值为【0-否，1-是】！")
    private String isKf;

    @Null
    private String kfQrcodeUrl;

    @Size(max=96,message="标签ID列表：最长96个字符！")
    private String tagList;

    private Date updateTime;
    
    @NotNull(message="更新人员：不可为空！")
    private Integer updateOpr;

    private String status;

    public Long getRecId() {
        return recId;
    }

    public void setRecId(Long recId) {
        this.recId = recId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
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

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd == null ? null : passwd.trim();
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

    public String getIsKf() {
        return isKf;
    }

    public void setIsKf(String isKf) {
        this.isKf = isKf == null ? null : isKf.trim();
    }

    public String getKfQrcodeUrl() {
        return kfQrcodeUrl;
    }

    public void setKfQrcodeUrl(String kfQrcodeUrl) {
        this.kfQrcodeUrl = kfQrcodeUrl == null ? null : kfQrcodeUrl.trim();
    }

    public String getTagList() {
        return tagList;
    }

    public void setTagList(String tagList) {
        this.tagList = tagList == null ? null : tagList.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
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

	public Integer getUpdateOpr() {
		return updateOpr;
	}

	public void setUpdateOpr(Integer updateOpr) {
		this.updateOpr = updateOpr;
	}
    
	public static enum TAG{
		pimage("pimage","图库管理"),
		goods("goods","商品管理"),
		basic("basic","合作伙伴基本信息管理"),
		postage("postage","邮费模板管理"),
		saleorder("saleorder","销售订单管理"),
		aftersale("aftersale","售后管理"),
		mypartners("mypartners","下级合作伙伴管理"),
		reviewgds("reviewgds","商品审核"),
		reviewappr("reviewappr","评价审核"),
		complain4p("reviewappr","投诉上级"),
		
		ComplainDeal("ComplainDeal","投诉处理"),
		ComplainRevisit("ComplainRevisit","投诉回访"),
		;
		private String value;
		private String desc;
		
		private TAG(String value,String desc) {
			this.value = value;
			this.desc = desc;
		}
		public String getValue() {
			return this.value;
		}
		public String getDesc() {
			return this.desc;
		}
	}
}