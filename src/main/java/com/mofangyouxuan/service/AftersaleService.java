package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Aftersale;

public interface AftersaleService {
	
	public JSONObject saveAF(Aftersale aftersale);
	
	public JSONObject updateAF(Aftersale aftersale);
	
	public Aftersale getByID(String orderId);
	
	public int countAll(Map<String,Object> params);
	
	public List<Aftersale> getAll(Map<String,Object> params,PageCond pageCond);

}
