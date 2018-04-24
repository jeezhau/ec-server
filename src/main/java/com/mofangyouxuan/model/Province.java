package com.mofangyouxuan.model;

public class Province {
    private String provCode;

    private String provName;

    public String getProvCode() {
        return provCode;
    }

    public void setProvCode(String provCode) {
        this.provCode = provCode == null ? null : provCode.trim();
    }

    public String getProvName() {
        return provName;
    }

    public void setProvName(String provName) {
        this.provName = provName == null ? null : provName.trim();
    }
}