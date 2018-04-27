package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.VipBasicService;

/**
 * 系统图库管理
 * 1、文件保存目录：主图目录/VIPID_[ID]/自定义目录/文件名
 * 2、每个目录最多N1层，每个目录下最多M张图片；
 * 3、每个会员最多可添加P张照片；
 * 4、文件使用UUID重命名，返回URL中不包含目录信息；
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/image")
public class ImageGalleryAction {
	
	@Value("${sys.image-gallery-dir}")
	private String imageGalleryDir;
	@Value("${sys.image-folder-level-limit}")
	private int imageFolderLevelLimit;
	@Value("${sys.image-file-all-limit}")
	private int imageFileAllLimit;
	@Value("${sys.image-folder-file-limit}")
	private int imageFolderFileLimit;
	

	@Autowired
	private VipBasicService vipBasicService ;
	
	/**
	 * 图片上传
	 * @param folderPath		所属文件夹 Home/.....
	 * @param image		照片,jpg格式
	 * @param currUserId	当前操作会员用户
	 * @return {errcode:0,errmsg:"",filename:''}
	 */
	@RequestMapping("/file/upload")
	public String uploadFile(@RequestParam(value="folderPath",required=true)String folderPath,
			@RequestParam(value="image")MultipartFile image,
			@RequestParam(value="currUserId",required=true)Integer currUserId) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片信息不可为空！");
				return jsonRet.toString();
			}
			//文件类型判断
			String imgType = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf('.')+1);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件必须是jpg,jpeg,png格式！");
				return jsonRet.toString();
			}
			if(!folderPath.startsWith("Home")) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "文件归属目录须是 Home/.... ！");
				return jsonRet.toString();
			}
			folderPath = folderPath.substring(4).trim();
			if("/".equals(folderPath)) {
				folderPath = "";
			}
			//数据检查
			VipBasic vip = this.vipBasicService.get(currUserId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			
			//目录检查
			File fileDir = new File(this.imageGalleryDir + "VIPID_" + currUserId + folderPath);
			if(!fileDir.exists()) {
				jsonRet.put("errcode", ErrCodes.IMAGE_DIR_NO_EXISTS);
				jsonRet.put("errmsg", "该文件归属目录不存在！");
				return jsonRet.toString();
			}
			//文件数量检查
			int cnt = fileDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.startsWith(".")) {
						return false;
					}
					return true;
				}
			}).length;
			if(cnt >= this.imageFolderFileLimit) {
				jsonRet.put("errcode", ErrCodes.IMAGE_FOLDER_FILE_LIMIT);
				jsonRet.put("errmsg", "该文件夹下的文件数量已经达到上限" + this.imageFolderFileLimit +"个！");
				return jsonRet.toString();
			}
			File userFolder = new File(this.imageGalleryDir + "VIPID_" + currUserId);
			int cntAll = countFileCnt(userFolder)-1;
			if(cntAll>= this.imageFileAllLimit) {
				jsonRet.put("errcode", ErrCodes.IMAGE_FOLDER_FILE_LIMIT);
				jsonRet.put("errmsg", "您的文件数量已经达到上限" + this.imageFileAllLimit +"个！");
				return jsonRet.toString();
			}
			String filename = System.currentTimeMillis() + "." + imgType.toLowerCase();
			File newFile = new File(fileDir,filename);
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("filename", filename);
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
	 * @param upFolder	上级文件路径
	 * @param folderName	新建文件夹名称
	 * @param currUserId
	 * @return
	 */
	@RequestMapping("/folder/create")
	public Object createFolder(@RequestParam(value="upFolderPath",required=true)String upFolderPath,
			@RequestParam(value="folderName",required=true)String folderName,
			@RequestParam(value="currUserId",required=true)Integer currUserId){
		JSONObject jsonRet = new JSONObject();
		try {
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
			if(!upFolderPath.startsWith("Home")) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "文件夹归属目录(上级目录)须是 Home/.... ！");
				return jsonRet.toString();
			}
			upFolderPath = upFolderPath.substring(4).trim();
			if("/".equals(upFolderPath)) {
				upFolderPath = "";
			}
			//数据检查
			VipBasic vip = this.vipBasicService.get(currUserId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			//目录检查
			File upDir = new File(this.imageGalleryDir + "VIPID_" + currUserId + "/" + upFolderPath);
			if(upFolderPath.length() == 0) {
				upDir.mkdirs();
			}else {
				if(!upDir.exists()) {
					jsonRet.put("errcode", ErrCodes.IMAGE_DIR_NO_EXISTS);
					jsonRet.put("errmsg", "该文件归属目录不存在！");
					return jsonRet.toString();
				}
			}
			if(upFolderPath.startsWith("/")) {
				upFolderPath = upFolderPath.substring(1);
			}
			if(upFolderPath.endsWith("/")) {
				upFolderPath = upFolderPath.substring(0, upFolderPath.length()-1);
			}
			//文件数量检查
			int level = upFolderPath.split("/").length;
			if(level >= this.imageFolderLevelLimit) {
				jsonRet.put("errcode", ErrCodes.IMAGE_FOLDER_LEVEL_LIMIT);
				jsonRet.put("errmsg", "该上级文件夹的层级已经达到上限" + this.imageFolderLevelLimit +"层！");
				return jsonRet.toString();
			}
			int cnt = upDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.startsWith(".")) {
						return false;
					}
					return true;
				}
			}).length;
			if(cnt >= this.imageFolderFileLimit) {
				jsonRet.put("errcode", ErrCodes.IMAGE_FOLDER_FILE_LIMIT);
				jsonRet.put("errmsg", "该文件夹下的文件数量已经达到上限" + this.imageFolderFileLimit +"个！");
				return jsonRet.toString();
			}
			File userFolder = new File(this.imageGalleryDir + "VIPID_" + currUserId);
			int cntAll = countFileCnt(userFolder)-1;
			if(cntAll>= this.imageFileAllLimit) {
				jsonRet.put("errcode", ErrCodes.IMAGE_FOLDER_FILE_LIMIT);
				jsonRet.put("errmsg", "您的文件数量已经达到上限" + this.imageFileAllLimit +"个！");
				return jsonRet.toString();
			}
			File newFolder = new File(upDir,folderName);
			if(!newFolder.exists()) {
				newFolder.mkdir();
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
	 * 列出文件夹下的所有文件
	 * @param folderName
	 * @param currUserId
	 * @return {errcode:0,errmsg:'ok',files:[]}
	 */
	@RequestMapping("/folder/list")
	public Object listFiles(@RequestParam(value="folderPath",required=true)String folderPath,
			@RequestParam(value="currUserId",required=true)Integer currUserId) {
		JSONObject jsonRet = new JSONObject();
		try {
			if(!folderPath.startsWith("Home")) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "文件夹目录须是 Home/.... ！");
				return jsonRet.toString();
			}
			folderPath = folderPath.substring(4).trim();
			if("/".equals(folderPath)) {
				folderPath = "";
			}
			//数据检查
			VipBasic vip = this.vipBasicService.get(currUserId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.VIP_NO_USER);
				jsonRet.put("errmsg", "系统中没有该会员或未激活！");
				return jsonRet.toString();
			}
			//目录检查
			File fileDir = new File(this.imageGalleryDir + "VIPID_" + currUserId + folderPath);
			if(!fileDir.exists()) {
				jsonRet.put("errcode", ErrCodes.IMAGE_DIR_NO_EXISTS);
				jsonRet.put("errmsg", "该文件目录不存在！");
				return jsonRet.toString();
			}
			String[] files = fileDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.startsWith(".")) {
						return false;
					}
					return true;
				}
				
			});
			jsonRet.put("files", files);
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
	@RequestMapping("/file/show/{currUserId}/{filename}")
	public void showFile(@PathVariable(value="filename",required=true)String filename,
			@PathVariable(value="currUserId",required=true)Integer currUserId,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) {
		try {
			//数据检查
			VipBasic vip = this.vipBasicService.get(currUserId);
			if(vip == null || !"1".equals(vip.getStatus()) ) {
				return;
			}
			//文件查找
			File fileDir = new File(this.imageGalleryDir + "VIPID_" + currUserId);
			if(!fileDir.exists()) {
				return;
			}
			File file = findFile(fileDir,filename);
			if(file == null) {
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
	
	
	/**
	 * 统计该文件夹下的文件数量,排除隐藏文件
	 * @param folder
	 * @return
	 */
	private int countFileCnt(File file) {
		int cnt = 0;
		if(!file.exists()) {//文件不存在
			return 0;
		}
		if(!file.isDirectory()) {//普通文件
			if(file.getName().startsWith(".")) {
				return 0;
			}
			return 1;
		}
		File[] files = file.listFiles();
		if(files == null || files.length<1) {//空目录
			return 1;
		}else {
			cnt += 1; //非空目录
		}
		//迭代目录下的文件
		
		for(File f:files) {
			int c = countFileCnt(f);
			//System.out.println(f.getName() + ":" + c);
			cnt += c;
		}
		return cnt;
	}
	
	/**
	 * 从指定文件夹下查找指定文件
	 * @param folder
	 * @param filename
	 * @return
	 */
	private File findFile(File file,String filename) {
		File target = null;
		if(file == null) {
			return null;
		}
		if(!file.exists()) {
			return null;
		}
		if(!file.isDirectory()) {//非目录
			if(file.getName().equals(filename)) {
				return file;
			}
		}
		File[] files = file.listFiles();
		if(files == null || files.length<1) {
			return null;
		}
		for(File f:files) {
			target = findFile(f,filename);
			if(target != null) {
				return target;
			}
		}
		return target;
	}
	
	public static void main(String[] args) {
		String path = "/Users/jeekhan/mfyx/image-gallery/VIPID_100008";
		File file = new File(path);
		int cnt = new ImageGalleryAction().countFileCnt(file);
		System.out.println(cnt);
	}
}
