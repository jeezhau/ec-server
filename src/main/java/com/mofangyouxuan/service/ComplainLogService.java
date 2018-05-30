package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.ComplainLog;

public interface ComplainLogService {
	
	/**
	 * 添加投诉
	 * @param log
	 * @return
	 */
	public JSONObject add(ComplainLog log);
	
	/**
	 * 更新投诉内容
	 * @param log
	 * @return
	 */
	public JSONObject updateContent(ComplainLog log);
	
	/**
	 * 更新投诉处理结果
	 * 
	 * @param old
	 * @param dealContent
	 * @param oprId
	 * @return
	 */
	public JSONObject updateDeal(ComplainLog old,String dealContent,Integer oprId) ;

	/**
	 * 更新回访处理结果
	 * 
	 * @param old
	 * @param revisitContent
	 * @param oprId
	 * @return
	 */
	public JSONObject updateRevisit(ComplainLog old,String revisitContent,Integer oprId,String status);
	
	/**
	 * 
	 * @param log
	 * @return
	 */
	public JSONObject delete(ComplainLog log) ;
	
	/**
	 * 获取所有的投诉
	 * 
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<ComplainLog> getAll(Map<String,Object> params,String sorts,PageCond pageCond);
	
	
	/**
	 * 统计所有的投诉
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params);
	
	/**
	 * 获取指定ID的投诉信息
	 * @param cplainId
	 * @return
	 */
	public ComplainLog get(Integer cplainId);

}

