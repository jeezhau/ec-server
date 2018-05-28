package com.mofangyouxuan.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.SysParam;

public interface SysParamService {
	
	/**
	 * 新添加参数
	 * @param rec
	 * @return
	 */
	public JSONObject add(SysParam rec,Integer oprId);
	
	/**
	 * 更新参数内容，可不变更名称
	 * @param rec
	 * @return
	 */
	public JSONObject update(SysParam rec,Integer oprId);
	
	
	/**
	 * 获取指定分类的参数列表
	 * @param paramTp
	 * @return
	 */
	public List<SysParam> getAllByTp(String paramTp);
	
	/**
	 * 获取所有的参数
	 * @return
	 */
	public List<SysParam> getAll();
	
	/**
	 * 根据参数名获取指定参数
	 * @param paramName
	 * @return
	 */
	public SysParam get(String paramName);

}
