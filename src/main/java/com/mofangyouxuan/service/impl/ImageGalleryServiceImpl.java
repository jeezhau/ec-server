package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.mapper.ImageGalleryMapper;
import com.mofangyouxuan.model.ImageGallery;
import com.mofangyouxuan.service.ImageGalleryService;

@Service
@Transactional
public class ImageGalleryServiceImpl implements ImageGalleryService{
	
	@Autowired
	private ImageGalleryMapper imageGalleryMapper;
	@Autowired
	private SysParamUtil sysParamUtil;
	
	@Override
	public ImageGallery get(String imgId,Integer partnerId) {
		return this.imageGalleryMapper.selectByPrimaryKey(imgId,partnerId);
	}
	
	@Override
	public JSONObject add(ImageGallery image) {
		JSONObject jsonRet = new JSONObject();
		//上级文件夹检查
		if(!"".equals(image.getParentId()) && image.getParentId() != null && !"Home".equals(image.getParentId())) {
			Map<String,Object> params4 = new HashMap<String,Object>();
			params4.put("partnerId", image.getPartnerId());
			params4.put("imgId", image.getParentId());
			params4.put("isDir", "1");
			int cnt4 = this.imageGalleryMapper.countAll(params4);
			if(cnt4 <= 0) {
				jsonRet.put("errcode", ErrCodes.IMAGE_DIR_NO_EXISTS);
				jsonRet.put("errmsg", "该上级文件夹不存在！");
				return jsonRet;
			}
		}else {
			image.setParentId("Home");
		}
		//同名文件检查
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partnerId", image.getPartnerId());
		params.put("parentId", image.getParentId());
		params.put("fileName", image.getFileName());
		List<ImageGallery> list1 = this.imageGalleryMapper.selectAll(params);
		if(list1 != null && list1.size()>0) {
			jsonRet.put("errcode", ErrCodes.CASH_APPLY_HAS_REC);
			jsonRet.put("errmsg", "您已有该名称的文件！");
			return jsonRet;
		}
		//总数量检查
		Map<String,Object> params2 = new HashMap<String,Object>();
		params2.put("partnerId", image.getPartnerId());
		int cnt2 = this.imageGalleryMapper.countAll(params2);
		if(cnt2 >= this.sysParamUtil.getImageFileAllLimit()) {
			jsonRet.put("errcode", ErrCodes.IMAGE_ALL_FILE_LIMIT);
			jsonRet.put("errmsg", "您的文件数量已经达到限制值，如需继续请充值扩容！");
			return jsonRet;
		}
		//单文件夹文件个数检查
		Map<String,Object> params3 = new HashMap<String,Object>();
		params3.put("partnerId", image.getPartnerId());
		params3.put("parentId", image.getParentId());
		int cnt3 = this.imageGalleryMapper.countAll(params3);
		if(cnt3 >= this.sysParamUtil.getImageFolderFileLimit()) {
			jsonRet.put("errcode", ErrCodes.IMAGE_ALL_FILE_LIMIT);
			jsonRet.put("errmsg", "您的该文件夹下的文件数量已经达到限制值(" + this.sysParamUtil.getImageFolderFileLimit() + ")，请使用其他的文件夹进行操作或归类整理！");
			return jsonRet;
		}
		image.setUdpateTime(new Date());
		image.setUsingCnt(0);
		int cnt = this.imageGalleryMapper.insert(image);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}

	/**
	 * 文件重命名
	 */
	@Override
	public JSONObject rename(ImageGallery image) {
		JSONObject jsonRet = new JSONObject();
		//同名文件检查
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partnerId", image.getPartnerId());
		params.put("parentId", image.getParentId());
		params.put("fileName", image.getFileName());
		List<ImageGallery> list1 = this.imageGalleryMapper.selectAll(params);
		if(list1 != null && list1.size()>0) {
			jsonRet.put("errcode", ErrCodes.CASH_APPLY_HAS_REC);
			jsonRet.put("errmsg", "您已有该名称的文件！");
			return jsonRet;
		}
		ImageGallery updImage = new ImageGallery();
		updImage.setImgId(image.getImgId());
		updImage.setPartnerId(image.getPartnerId());
		updImage.setFileName(image.getFileName());
		updImage.setImgType(image.getImgType());
		updImage.setUdpateTime(new Date());
		int cnt = this.imageGalleryMapper.updateByPrimaryKeySelective(updImage);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 文件移动
	 */
	@Override
	public JSONObject move(ImageGallery image,String targetParentImgId) {
		JSONObject jsonRet = new JSONObject();
		//同名文件检查
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partnerId", image.getPartnerId());
		params.put("parentId", targetParentImgId);
		params.put("fileName", image.getFileName());
		List<ImageGallery> list1 = this.imageGalleryMapper.selectAll(params);
		if(list1 != null && list1.size()>0) {
			jsonRet.put("errcode", ErrCodes.CASH_APPLY_HAS_REC);
			jsonRet.put("errmsg", "您已有该名称的文件！");
			return jsonRet;
		}
		//单文件夹文件个数检查
		Map<String,Object> params3 = new HashMap<String,Object>();
		params3.put("partnerId", image.getPartnerId());
		params3.put("parentId", targetParentImgId);
		int cnt2 = this.imageGalleryMapper.countAll(params3);
		if(cnt2 >= this.sysParamUtil.getImageFolderFileLimit()) {
			jsonRet.put("errcode", ErrCodes.IMAGE_ALL_FILE_LIMIT);
			jsonRet.put("errmsg", "您的该文件夹下的文件数量已经达到限制值(" + this.sysParamUtil.getImageFolderFileLimit() + ")，请使用其他的文件夹进行操作或归类整理！");
			return jsonRet;
		}
		
		ImageGallery updImage = new ImageGallery();
		updImage.setImgId(image.getImgId());
		updImage.setPartnerId(image.getPartnerId());
		updImage.setParentId(targetParentImgId);
		updImage.setUdpateTime(new Date());
		int cnt = this.imageGalleryMapper.updateByPrimaryKeySelective(updImage);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}

	@Override
	public JSONObject delete(ImageGallery image) {
		JSONObject jsonRet = new JSONObject();
		int cnt = this.imageGalleryMapper.deleteByPrimaryKey(image.getImgId(),image.getPartnerId());
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}

	@Override
	public JSONObject isUsing(String imgId,Integer partnerId) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("imgId", imgId);
		params.put("partnerId", partnerId);
		params.put("isUsing", 1);
		int cnt  = this.imageGalleryMapper.countAll(params);
		if(cnt > 0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "没有正在使用！");
		}
		return jsonRet;
	}

	@Override
	public int getCount(Map<String, Object> params) {
		return this.imageGalleryMapper.countAll(params);
	}

	@Override
	public List<ImageGallery> getAll(Map<String, Object> params) {
		return this.imageGalleryMapper.selectAll(params);
	}

	@Override
	public int updUsingCnt(Integer partnerId, String imgId, int cnt) {
		if(cnt == 0) {
			return 0;
		}
		return this.imageGalleryMapper.updUsingCnt(partnerId, imgId, cnt);
	}

}
