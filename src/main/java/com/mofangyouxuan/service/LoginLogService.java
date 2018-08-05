package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.LoginLog;

public interface LoginLogService {
	
	public JSONObject add(LoginLog log);
	
	public int countAll(Map<String,Object> params);
	
	public List<LoginLog> getAll(Map<String,Object> params,PageCond pageCond);

}
