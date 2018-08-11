package com.mofangyouxuan.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.Category;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.service.CategoryService;
import com.mofangyouxuan.service.impl.AuthSecret;
import com.mofangyouxuan.utils.FileFilter;

/**
 * 系统合作伙伴管理分类信息
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private SysParamUtil sysParamUtil;
	@Autowired
	private AuthSecret authSecret;
	@Value("${sys.category-img-dir}")
	private String categoryImgDir;
	
	/**
	 * 新增商品分类信息
	 * @param cat
	 * @param result
	 * @param operator
	 * @param passwd
	 * @return
	 */
	@RequestMapping("/add")
	public JSONObject addCat(@Valid Category cat,BindingResult result,
			@RequestParam(value="operator",required=true)Integer operator,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				return jsonRet;
			}
			//安全与权限检查
			jsonRet 	= this.authSecret.auth(this.sysParamUtil.getSysPartnerId(), operator, passwd,PartnerStaff.TAG.Category);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet;
			}
			//数据处理保存
			cat.setImgPath(null);
			cat.setUpdateOpr(operator);
			jsonRet = this.categoryService.add(cat);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}

	/**
	 * 修改商品分类信息
	 * @param cat
	 * @param result
	 * @param operator
	 * @param passwd
	 * @return
	 */
	@RequestMapping("/update")
	public JSONObject updCat(@Valid Category cat,BindingResult result,
			@RequestParam(value="operator",required=true)Integer operator,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				return jsonRet;
			}
			//安全与权限检查
			jsonRet 	= this.authSecret.auth(this.sysParamUtil.getSysPartnerId(), operator, passwd,PartnerStaff.TAG.Category);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet;
			}
			//数据检查
			Category old = this.categoryService.get(cat.getCategoryId());
			if(old == null) {
				jsonRet.put("errcode",ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该分类信息！");
				return jsonRet;
			}
			//数据处理保存
			cat.setImgPath(old.getImgPath());
			cat.setUpdateTime(new Date());
			jsonRet = this.categoryService.update(cat);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 分类图片上传
	 * @param image		照片,jpg格式
	 * @param operator	当前操作用户
	 * @param passwd		
	 * @return {errcode:0,errmsg:""}
	 */
	@RequestMapping("/img/upload/{catId}")
	public JSONObject uploadImg(@PathVariable("catId")Integer catId,
			@RequestParam(value="image")MultipartFile image,
			@RequestParam(value="operator",required=true)Integer operator,
			@RequestParam(value="passwd",required=true)String passwd) {
		
		JSONObject jsonRet = new JSONObject();
		try {
			//安全检查
			jsonRet 	= this.authSecret.auth(this.sysParamUtil.getSysPartnerId(), operator, passwd,PartnerStaff.TAG.Category);
			if(jsonRet.getIntValue("errcode") != 0) {
				return jsonRet;
			}
			if(image == null || image.isEmpty()) {
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "图片信息不可为空！");
				return jsonRet;
			}
			//文件类型及大小判断
			String originalFilename = image.getOriginalFilename();
			int index = image.getOriginalFilename().lastIndexOf('.');
			String imgType = originalFilename.substring(index + 1);
			originalFilename = originalFilename.substring(0, index);
			if(!"jpg".equalsIgnoreCase(imgType) && !"jpeg".equalsIgnoreCase(imgType) && !"png".equalsIgnoreCase(imgType)) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件必须是jpg,jpeg,png格式！");
				return jsonRet;
			}
			if(image.getSize() > 1024*1024) {
				jsonRet.put("errcode", ErrCodes.IMAGE_PARAM_ERROR);
				jsonRet.put("errmsg", "图片文件不可大于1M！");
				return jsonRet;
			}
			imgType = imgType.toLowerCase();
			//数据检查
			Category old = this.categoryService.get(catId);
			if(old == null) {
				jsonRet.put("errcode",ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该分类信息！");
				return jsonRet;
			}
			//数据处理保存
			String filename = catId + "." + imgType;
			Category cat = new Category();
			cat.setImgPath(filename);
			cat.setUpdateTime(new Date());
			cat.setUpdateOpr(operator);
			jsonRet = this.categoryService.update(cat);
			if(!(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") == 0)) {
				return jsonRet;
			}
			//目录检查
			File newFile = new File(this.categoryImgDir,filename);
			FileUtils.copyInputStreamToFile(image.getInputStream(), newFile);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		} catch (Exception e) {
			e.printStackTrace();
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet;
	}
	
	/**
	 * 显示分类图片
	 * @param filename
	 * @param currUserId
	 * @return
	 */
	@RequestMapping("/img/show/{imgId}")
	public void showFile(@PathVariable(value="imgId",required=true)String imgId,
			OutputStream out,HttpServletRequest request,HttpServletResponse response) {
		try {
			//文件查找
			File imgDir = new File(this.categoryImgDir);
			if(!imgDir.exists() || !imgDir.isDirectory()) {
				return;
			}
			File[] files = imgDir.listFiles(new FileFilter(imgId));
			if(files != null && files.length>0) {
				File file = files[0];
				BufferedImage image = ImageIO.read(file);
				response.setContentType("image/*");
				response.addHeader("filename", file.getName());
				OutputStream os = response.getOutputStream();  
				String type = file.getName().substring(file.getName().lastIndexOf('.')+1);
				ImageIO.write(image, type, os); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取商品分类
	 * @return {errcode:0,errmsg:"",categories:[{},{},...]}
	 */
	@RequestMapping("/getall")
	public Object getAll(String jsonSearchParams) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			if(jsonSearchParams != null && jsonSearchParams.length()>0) {
				JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
				if(jsonSearch.containsKey("status")) {
					params.put("status", jsonSearch.getString("status"));
				}
				if(jsonSearch.containsKey("partnerId")) {
					params.put("partnerId", jsonSearch.getInteger("partnerId"));
				}
				if(jsonSearch.containsKey("categoryName")) {
					params.put("categoryName", jsonSearch.getString("categoryName"));
				}
				if(jsonSearch.containsKey("isCwide")) {
					params.put("isCwide", jsonSearch.getString("isCwide"));
				}
				if(jsonSearch.containsKey("keywords")) {
					params.put("keywords", jsonSearch.getString("keywords"));
				}
			}
			List<Category> list = this.categoryService.getAll(params);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("categories", list);
			return jsonRet;
		}catch(Exception e) {
			//异常处理
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
}
