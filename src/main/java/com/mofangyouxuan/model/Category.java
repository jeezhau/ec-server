package com.mofangyouxuan.model;

public class Category {
    private Integer categoryId;

    private String categoryName;

    private String status;

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
}