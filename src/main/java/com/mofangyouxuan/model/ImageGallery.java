package com.mofangyouxuan.model;

import java.util.Date;

public class ImageGallery {
    private String imgId;

    private String imgType;

    private String isDir;

    private Integer partnerId;

    private String parentId;

    private String fileName;

    private Integer usingCnt;

    private Date udpateTime;

    private Integer updateOpr;

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId == null ? null : imgId.trim();
    }

    public String getImgType() {
        return imgType;
    }

    public void setImgType(String imgType) {
        this.imgType = imgType == null ? null : imgType.trim();
    }

    public String getIsDir() {
        return isDir;
    }

    public void setIsDir(String isDir) {
        this.isDir = isDir == null ? null : isDir.trim();
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public Integer getUsingCnt() {
        return usingCnt;
    }

    public void setUsingCnt(Integer usingCnt) {
        this.usingCnt = usingCnt;
    }

    public Date getUdpateTime() {
        return udpateTime;
    }

    public void setUdpateTime(Date udpateTime) {
        this.udpateTime = udpateTime;
    }

    public Integer getUpdateOpr() {
        return updateOpr;
    }

    public void setUpdateOpr(Integer updateOpr) {
        this.updateOpr = updateOpr;
    }
}
