package com.mofangyouxuan.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Category {
    private Integer categoryId;

    @Size(min=2,max=20,message=" 分类名称：长度为2-20个字符！")
    private String categoryName;
    
    @Pattern(regexp="[1D]",message=" 状态：取值不正确！")
    private String status;
    
    @NotNull(message=" 上级分类ID：不可为空！")
    private Integer parentId;
    
    @Pattern(regexp="[01]",message=" 是否仅针对同城：取值不正确！")
    private String isCwide;
    
    @Size(min=0,max=200,message=" 关键描述：长度0-200个字符！")
    private String keyDesc;
    
    private String imgPath;
    
    private Date updateTime;
    
    private Integer updateOpr;

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer id) {
        this.categoryId = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String name) {
        this.categoryName = name == null ? null : name.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getIsCwide() {
		return isCwide;
	}

	public void setIsCwide(String isCwide) {
		this.isCwide = isCwide;
	}

	public String getKeyDesc() {
		return keyDesc;
	}

	public void setKeyDesc(String keyDesc) {
		this.keyDesc = keyDesc;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
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