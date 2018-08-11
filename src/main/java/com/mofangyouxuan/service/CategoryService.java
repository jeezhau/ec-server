package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.Category;

public interface CategoryService {
	
	public Category get(Integer categoryId) ;
	
	public JSONObject add(Category cat);
	
	public JSONObject update(Category cat);
	
	public List<Category> getAll(Map<String,Object> params);

}
