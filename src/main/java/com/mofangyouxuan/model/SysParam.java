package com.mofangyouxuan.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SysParam {
	
	@NotNull(message=" 参数分类：不可为空！")
	@Size(min=2,max=20,message="参数分类：长度范围【2-20】字符！")
    private String paramTp;
	
	@NotNull(message=" 参数名称：不可为空！")
	@Size(min=2,max=100,message="参数名称：长度范围【2-100】字符！")
    private String paramName;
	
	@NotNull(message=" 参数值：不可为空！")
	@Size(min=1,max=600,message="参数值：长度范围【1-600】字符！")
    private String paramValue;

	@Size(max=600,message="参数描述：长度范围【0-600】字符！")
    private String paramDesc;

    private Date updateTime;
    @NotNull(message=" 更新人：不可为空！")
    private Integer updateOpr;

    private String status;

    public String getParamTp() {
        return paramTp;
    }

    public void setParamTp(String paramTp) {
        this.paramTp = paramTp == null ? null : paramTp.trim();
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName == null ? null : paramName.trim();
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue == null ? null : paramValue.trim();
    }

    public String getParamDesc() {
        return paramDesc;
    }

    public void setParamDesc(String paramDesc) {
        this.paramDesc = paramDesc == null ? null : paramDesc.trim();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}