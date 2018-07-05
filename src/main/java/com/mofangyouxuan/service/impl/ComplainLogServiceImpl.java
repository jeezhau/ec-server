package com.mofangyouxuan.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.ComplainLogMapper;
import com.mofangyouxuan.model.ComplainLog;
import com.mofangyouxuan.service.ComplainLogService;

@Service
@Transactional
public class ComplainLogServiceImpl implements ComplainLogService{
		
	@Autowired
	private ComplainLogMapper complainLogMapper;
	
	/**
	 * 添加投诉
	 * 1、不可添加已有待处理或处理中的同订单商品投诉；
	 * @param log
	 * @return
	 */
	public JSONObject add(ComplainLog log) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("oprId", log.getOprId());
		params.put("goodsId", log.getGoodsId());
		params.put("orderId", log.getOrderId());
		params.put("status", "0,1");
		int hasCnt = this.complainLogMapper.countAll(params);
		if(hasCnt>0) {
			jsonRet.put("errcode", ErrCodes.COMPLAIN_HAS_EXISTS);
			jsonRet.put("errmsg", "您已经提交过该订单的投诉！");
			return jsonRet;
		}
		log.setCplanId(null);
		log.setCreateTime(new Date());
		int cnt = this.complainLogMapper.insert(log);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 更新投诉内容
	 * @param log
	 * @return
	 */
	public JSONObject updateContent(ComplainLog log) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("oprId", log.getOprId());
		params.put("goodsId", log.getGoodsId());
		params.put("orderId", log.getOrderId());
		params.put("status", "0,1");
		List<ComplainLog> list = this.complainLogMapper.selectAll(params, null, new PageCond(0,1));
		if(list !=null && list.size()>0) {
			if(!list.get(0).getCplanId().equals(log.getCplanId())) {
				jsonRet.put("errcode", ErrCodes.COMPLAIN_HAS_EXISTS);
				jsonRet.put("errmsg", "您已经提交过该订单的投诉！");
				return jsonRet;
			}
		}
		log.setCreateTime(new Date());
		int cnt = this.complainLogMapper.updateByPrimaryKey(log);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 更新投诉处理结果
	 * 
	 * @param old
	 * @param dealContent
	 * @param oprId
	 * @return
	 */
	public JSONObject updateDeal(ComplainLog old,String dealContent,Integer oprId) {
		JSONObject jsonRet = new JSONObject();
		String oldLCntLog = old.getDealLog();
		if(oldLCntLog == null || oldLCntLog.length()<1) {
			oldLCntLog = "[]";
		}
		JSONArray arr = JSONArray.parseArray(oldLCntLog);
		JSONObject currCnt = new JSONObject();
		Date currTime = new Date();
		currCnt.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
		currCnt.put("result", dealContent);
		currCnt.put("oprid", oprId);
		arr.add(0, currCnt);
		ComplainLog updLog = new ComplainLog();
		updLog.setCplanId(old.getCplanId());
		updLog.setDealOpr(oprId);
		updLog.setDealTime(currTime);
		updLog.setDealLog(arr.toJSONString());
		updLog.setStatus("1");
		int cnt = this.complainLogMapper.updateByPrimaryKey(updLog);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}

	/**
	 * 更新回访处理结果
	 * 
	 * @param old
	 * @param revisitContent
	 * @param oprId
	 * @return
	 */
	public JSONObject updateRevisit(ComplainLog old,String revisitContent,Integer oprId,String status) {
		JSONObject jsonRet = new JSONObject();
		String oldLCntLog = old.getRevisitLog();
		if(oldLCntLog == null || oldLCntLog.length()<1) {
			oldLCntLog = "[]";
		}
		JSONArray arr = JSONArray.parseArray(oldLCntLog);
		JSONObject currCnt = new JSONObject();
		Date currTime = new Date();
		currCnt.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
		currCnt.put("result", revisitContent);
		currCnt.put("oprid", oprId);
		arr.add(0, currCnt);
		ComplainLog updLog = new ComplainLog();
		updLog.setCplanId(old.getCplanId());
		updLog.setRevisitOpr(oprId);
		updLog.setRevisitTime(currTime);
		updLog.setRevisitLog(arr.toJSONString());
		updLog.setStatus(status);
		int cnt = this.complainLogMapper.updateByPrimaryKey(updLog);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 删除
	 * @param log
	 * @return
	 */
	public JSONObject delete(ComplainLog log) {
		JSONObject jsonRet = new JSONObject();
		int cnt = this.complainLogMapper.deleteByPrimaryKey(log.getCplanId());
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
	 * 获取所有的投诉
	 * 
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<ComplainLog> getAll(Map<String,Object> params,String sorts,PageCond pageCond){
		if(pageCond == null) {
			pageCond = new PageCond(0,30);
		}
		return this.complainLogMapper.selectAll(params, sorts, pageCond);
	}
	
	
	/**
	 * 统计所有的投诉
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params) {
		return this.complainLogMapper.countAll(params);
	}
	
	/**
	 * 获取指定ID的投诉信息
	 * @param cplainId
	 * @return
	 */
	public ComplainLog get(Integer cplainId) {
		return this.complainLogMapper.selectByPrimaryKey(cplainId);
	}

}
