package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.ImageGallery;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.service.ImageGalleryService;
import com.mofangyouxuan.service.impl.AuthSecret;
import com.mofangyouxuan.utils.CommonUtil;

/**
 * 系统图库管理
 * 1、文件保存目录：主图目录/PARTNERID_[ID]/最近目录ID/文件名ID.后缀
 * 2、每个目录最多N个文件；
 * 3、每个会员最多可添加P张照片；
 * 4、目录的层级结构存与数据库中，文件系统中仅保存一个层级目录；
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/pimage")
public class PartnerImageController {	
	@Autowired
	private SysParamUtil sysParamUtil;	
	@Autowired
	private ImageGalleryService imageGalleryService;	
	@Autowired
	private AuthSecret authSecret;

	
	/**
	 * 图片上传
	 * @param folderImgId	所属文件夹ID
	 * @param image			照片,jpg格式
	 * @param currUserId		当前操作会员用户
	 * @return {errcode:0,errmsg:""}
	 */
	@RequestMapping("/{partnerId}/file/upload")
	public String uploadFile(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="folderImgId",required=true)String folderImgId,
			@RequestParam(value="image")MultipartFile image,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片信息不可为空！");
				return jsonRet.toString();
			}
			//文件类型及大小判断
			String originalFilename = image.getOriginalFilename();
			int index = image.getOriginalFilename().lastIndexOf('.');
			String imgType = originalFilename.substring(index + 1);
			originalFilename = originalFilename.substring(0, index);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件必须是jpg,jpeg,png格式！");
				return jsonRet.toString();
			}
			if(image.getSize() > 1024*1024) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件不可大于1M！");
				return jsonRet.toString();
			}
			imgType = imgType.toLowerCase();
			if(folderImgId != null && folderImgId.trim().length()>0 && !"Home".equals(folderImgId.trim())) {
				ImageGallery folder = this.imageGalleryService.get(folderImgId, partnerId);
				if(folder == null || !"1".equals(folder.getIsDir())) {
					jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
					jsonRet.put("errmsg", "系统中没有该文件夹！");
					return jsonRet.toString();
				}
			}
			//数据保存处理
			ImageGallery imgG = new ImageGallery();
			imgG.setImgId(CommonUtil.genImageId());
			imgG.setParentId(folderImgId);
			imgG.setFileName(originalFilename);
			imgG.setImgType(imgType);
			imgG.setIsDir("0");
			imgG.setPartnerId(partnerId);
			imgG.setUpdateOpr(currUserId);
			imgG.setUsingCnt(0);
			jsonRet = this.imageGalleryService.add(imgG);
			if(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") == 0) {
				;
			}else {
				return jsonRet.toJSONString();
			}
			//目录检查
			File fileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId + "/" + ("Home".equals(imgG.getParentId())?"":imgG.getParentId()));
			if(!fileDir.exists()) {
				jsonRet.put("errcode", ErrCodes.IMAGE_DIR_NO_EXISTS);
				jsonRet.put("errmsg", "该文件归属目录不存在！");
				return jsonRet.toString();
			}
			String filename = imgG.getImgId() + "." + imgType;
			File newFile = new File(fileDir,filename);
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("image", imgG);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 图片替换
	 * @param imgId		文件ID
	 * @param image		照片,jpg格式
	 * @param currUserId	当前操作会员用户
	 * @return {errcode:0,errmsg:"",filename:''}
	 */
	@RequestMapping("/{partnerId}/file/replace")
	public String replaceFile(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="imgId",required=true)String imgId,
			@RequestParam(value="image")MultipartFile image,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片信息不可为空！");
				return jsonRet.toString();
			}
			//文件类型及大小判断
			String originalFilename = image.getOriginalFilename();
			int index = image.getOriginalFilename().lastIndexOf('.');
			String imgType = originalFilename.substring(index + 1);
			originalFilename = originalFilename.substring(0, index);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件必须是jpg,jpeg,png格式！");
				return jsonRet.toString();
			}
			if(image.getSize() > 1024*1024) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件不可大于1M！");
				return jsonRet.toString();
			}
			imgType = imgType.toLowerCase();
			
			//数据保存处理
			ImageGallery old = this.imageGalleryService.get(imgId,partnerId);
			if(old == null || !"0".equals(old.getIsDir())) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该原文件！" );
				return jsonRet.toString();
			}
			if(!old.getPartnerId().equals(partnerId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限执行该操作！" );
				return jsonRet.toString();
			}
			old.setFileName(originalFilename);
			String oldImgType = old.getImgType();
			old.setImgType(imgType);
			jsonRet = this.imageGalleryService.rename(old);
			if(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") == 0) {
				;
			}else {
				return jsonRet.toJSONString();
			}
			File fileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId + "/" + ("Home".equals(old.getParentId())?"":old.getParentId()));
			File oldFile = new File(fileDir, old.getImgId() + "." + oldImgType);
			oldFile.delete();
			File newFile = new File(fileDir, old.getImgId() + "." + imgType);
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("image", old);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 新建文件夹
	 * @param upFolderImgId	上级文件路径
	 * @param folderName		新建文件夹名称
	 * @param currUserId
	 * @return
	 */
	@RequestMapping("/{partnerId}/folder/create")
	public Object createFolder(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="upFolderImgId",required=false)String upFolderImgId,
			@RequestParam(value="folderName",required=true)String folderName,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd){
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			//数据处理
			folderName = folderName.trim();
			if(!folderName.matches("^[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}$")) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "新建文件夹名称长度为2-10个子母、数字、汉字字符" );
				return jsonRet.toString();
			}
			if("Home".equals(folderName)) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "新建文件夹名称不可是保留词汇：Home ！" );
				return jsonRet.toString();
			}
			if(upFolderImgId != null && upFolderImgId.trim().length()>0 && !"Home".equals(upFolderImgId)) {
				ImageGallery folder = this.imageGalleryService.get(upFolderImgId, partnerId);
				if(folder == null || !"1".equals(folder.getIsDir())) {
					jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
					jsonRet.put("errmsg", "系统中没有该文件夹！");
					return jsonRet.toString();
				}
			}
			//数据保存处理
			ImageGallery image = new ImageGallery();
			image.setImgId(CommonUtil.genImageId());
			image.setParentId(upFolderImgId);
			image.setFileName(folderName);
			image.setImgType(null);
			image.setIsDir("1");
			image.setPartnerId(partnerId);
			image.setUpdateOpr(currUserId);
			image.setUsingCnt(0);
			jsonRet = this.imageGalleryService.add(image);
			if(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") == 0) {
				;
			}else {
				return jsonRet;
			}
			//目录检查
			File upDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId );
			if(!upDir.exists()) {
				upDir.mkdirs();
			}
			File newFolder = new File(upDir,image.getImgId());
			if(!newFolder.exists()) {
				newFolder.mkdir();
			}
			jsonRet.put("image", image);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 列出文件夹下的所有文件
	 * @param folderImgId
	 * @param currUserId
	 * @return {errcode:0,errmsg:'ok',files:[]}
	 */
	@RequestMapping("/{partnerId}/folder/list")
	public Object listFiles(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="folderImgId",required=false)String folderImgId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			
			//目录检查
			if(folderImgId != null && folderImgId.trim().length()>0 && !"Home".equals(folderImgId.trim())) {
				File fileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId + "/" + folderImgId);
				if(!fileDir.exists()) {
					jsonRet.put("errcode", ErrCodes.IMAGE_DIR_NO_EXISTS);
					jsonRet.put("errmsg", "该文件目录不存在！");
					return jsonRet.toString();
				}
			}
			//获取数据
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("partnerId", partnerId);
			if(folderImgId != null && folderImgId.trim().length()>0 ) {
				params.put("parentId", folderImgId.trim());
			}else {
				params.put("parentId", "Home");
			}
			List<ImageGallery> files = this.imageGalleryService.getAll(params);
			if(files != null && files.size()>0) {
				jsonRet.put("files", files);
			}
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 文件移动
	 * @param srcImgId	原文件ID
	 * @param targetParentId	目标文件夹ID
	 * @param currUserId
	 * @return
	 */
	@RequestMapping("/{partnerId}/move")
	public Object move(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="srcImgId",required=true)String srcImgId,
			@RequestParam(value="targetParentId",required=true)String targetParentId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd){
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			//数据检查
			ImageGallery srcImg = this.imageGalleryService.get(srcImgId, partnerId);
			ImageGallery targetParent = this.imageGalleryService.get(targetParentId, partnerId);
			if(srcImg == null) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "原文件:不存在！");
				return jsonRet.toJSONString();
			}
			if(!"Home".equals(targetParentId) && (targetParent == null || !"1".equals(targetParent.getIsDir()))) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "目标文件夹:不存在！");
				return jsonRet.toJSONString();
			}
			if(srcImg.getParentId().equals(targetParentId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "目标文件夹:不可与原文件的父文件夹相同！");
				return jsonRet.toJSONString();
			}
			if(srcImg.getImgId().equals(targetParentId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "目标文件夹:不可与原文件相同！");
				return jsonRet.toJSONString();
			}
			if(!srcImg.getPartnerId().equals(partnerId) || 
					!"Home".equals(targetParentId) && !targetParent.getPartnerId().equals(partnerId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限执行该操作！");
				return jsonRet.toJSONString();
			}
			//数据保存处理
			jsonRet = this.imageGalleryService.move(srcImg, targetParentId);
			if(!(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") == 0)) {
				return jsonRet.toJSONString();
			}
			if("0".equals(srcImg.getIsDir())) {
				//迁移文件
				File oldFileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId + "/" + ("Home".equals(srcImg.getParentId())?"":srcImg.getParentId()));
				File oldFile = new File(oldFileDir, srcImg.getImgId() + "." + srcImg.getImgType());
				File newFileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId + "/" + targetParentId);
				File newFile = new File(newFileDir, srcImg.getImgId() + "." + srcImg.getImgType());
				FileUtils.moveFile(oldFile, newFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 重命名
	 * @param imgId	文件ID
	 * @param fileName	新文件名称
	 * @param currUserId
	 * @return
	 */
	@RequestMapping("/{partnerId}/rename")
	public Object rename(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="imgId",required=true)String imgId,
			@RequestParam(value="fileName",required=true)String fileName,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd){
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			//数据处理
			fileName = fileName.trim();
			if(!fileName.matches("^[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}$")) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "新文件名称长度为2-10个子母、数字、汉字字符" );
				return jsonRet.toString();
			}
			if("Home".equals(fileName)) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "新文件名称不可是保留词汇：Home ！" );
				return jsonRet.toString();
			}
			
			//数据保存处理
			ImageGallery old = this.imageGalleryService.get(imgId,partnerId);
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该原文件！" );
				return jsonRet.toString();
			}
			if(!old.getPartnerId().equals(partnerId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限执行该操作！" );
				return jsonRet.toString();
			}
			old.setFileName(fileName);
			jsonRet = this.imageGalleryService.rename(old);
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 删除文件
	 * @param imgId		文件ID
	 * @param currUserId	当前操作会员用户
	 * @return {errcode:0,errmsg:""}
	 */
	@RequestMapping("/{partnerId}/delete")
	public String delete(@PathVariable("partnerId")Integer partnerId,
			@RequestParam(value="imgId",required=true)String imgId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(partnerId, currUserId, passwd,PartnerStaff.TAG.pimage);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet.toJSONString();
			}
			//数据保存处理
			ImageGallery old = this.imageGalleryService.get(imgId,partnerId);
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该原文件！" );
				return jsonRet.toString();
			}
			if(!old.getPartnerId().equals(partnerId)) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限执行该操作！" );
				return jsonRet.toString();
			}
			//获取数据
			if("1".equals(old.getIsDir())) {
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("partnerId", partnerId);
				params.put("parentId", old.getImgId());
				int cnt = this.imageGalleryService.getCount(params);
				if(cnt > 0) {
					jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
					jsonRet.put("errmsg", "该文件夹下还有文件，不可删除！" );
					return jsonRet.toString();
				}
			}
			jsonRet = this.imageGalleryService.delete(old);
			if(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") == 0) {
				;
			}else {
				return jsonRet.toJSONString();
			}
			//文件处理
			File fileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId + "/" + ("Home".equals(old.getParentId())?"":old.getParentId()));
			String filename = old.getImgId();
			if("0".equals(old.getIsDir())) {
				filename	 += old.getImgType();
			}
			File file = new File(fileDir,filename);
			file.delete();
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 显示用户图片
	 * @param filename
	 * @param currUserId
	 * @return
	 */
	@RequestMapping("/{partnerId}/file/show/{imgId}")
	public void showFile(@PathVariable(value="imgId",required=true)String imgId,
			@PathVariable(value="partnerId",required=true)Integer partnerId,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) {
		try {
			//文件查找
			File fileDir = new File(sysParamUtil.getImageGalleryDir() + "PARTNERID_" + partnerId);
			if(!fileDir.exists()) {
				return;
			}
			ImageGallery imgG = this.imageGalleryService.get(imgId,partnerId);
			if(imgG == null ) {
				return;
			}
			File file = new File(fileDir,("Home".equals(imgG.getParentId())?"":imgG.getParentId()) + "/" + imgG.getImgId() + "." + imgG.getImgType());
			if(!file.exists()) {
				return;
			}
			BufferedImage image = ImageIO.read(file);
			response.setContentType("image/*");
			response.addHeader("filename", file.getName());
			OutputStream os = response.getOutputStream();  
			String type = file.getName().substring(file.getName().lastIndexOf('.')+1);
			ImageIO.write(image, type, os); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
