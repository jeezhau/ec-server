package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.ImageGallery;

public interface ImageGalleryService {
	
	public ImageGallery get(String imgId,Integer partnerId);
	
	public JSONObject add(ImageGallery image);
	
	public JSONObject rename(ImageGallery image);
	
	public JSONObject move(ImageGallery image,String targetParentImgId);
	
	public JSONObject delete(ImageGallery image);
	
	public JSONObject isUsing(String  imgId,Integer partnerId);
	
	public int getCount(Map<String,Object> params);
	
	public List<ImageGallery> getAll(Map<String,Object> params);
	
	public int updUsingCnt(Integer partnerId,String imgId,int cnt);

}
