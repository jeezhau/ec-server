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
import com.mofangyouxuan.mapper.CategoryMapper;
import com.mofangyouxuan.model.Category;
import com.mofangyouxuan.service.CategoryService;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService{
	@Autowired
	private CategoryMapper categoryMapper;
	
	@Override
	public JSONObject add(Category cat) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("parentId", cat.getParentId());
		params.put("categoryName", cat.getCategoryName());
		int hasCnt = this.categoryMapper.countAll(params);
		if(hasCnt>0) {
			jsonRet.put("errcode", ErrCodes.CATEGORY_HAS_EXISTS);
			jsonRet.put("errmsg", "系统中已有该分类信息！");
			return jsonRet;
		}
		cat.setCategoryId(null);
		cat.setUpdateTime(new Date());
		int cnt = this.categoryMapper.insert(cat);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}

	@Override
	public JSONObject update(Category cat) {
		JSONObject jsonRet = new JSONObject();
		cat.setUpdateTime(new Date());
		int cnt = this.categoryMapper.updateByPrimaryKey(cat);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}

	@Override
	public List<Category> getAll(Map<String, Object> params) {
		return this.categoryMapper.selectAll(params);
	}
	
	public Category get(Integer categoryId) {
		return this.categoryMapper.selectByPrimaryKey(categoryId);
	}

}
