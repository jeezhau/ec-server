package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.mapper.SysParamMapper;
import com.mofangyouxuan.model.SysParam;
import com.mofangyouxuan.service.SysParamService;

@Service
public class SysParamServiceImpl implements SysParamService{
	@Autowired
	private SysParamMapper sysParamMapper;
	
	/**
	 * 新添加参数
	 * @param rec
	 * @return
	 */
	@Override
	public JSONObject add(SysParam rec,Integer oprId) {
		JSONObject jsonRet = new JSONObject();
		SysParam old = this.sysParamMapper.selectByKey(rec.getParamName());
		if(old != null) {
			jsonRet.put("errcode", -1);
			jsonRet.put("errmsg", "系统中已有同名参数！");
		}
		rec.setUpdateTime(new Date());
		rec.setUpdateOpr(oprId);
		int cnt = this.sysParamMapper.insert(rec);
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
	 * 更新参数内容，可不变更名称
	 * @param rec
	 * @return
	 */
	public JSONObject update(SysParam rec,Integer oprId) {
		JSONObject jsonRet = new JSONObject();
		rec.setUpdateTime(new Date());
		rec.setUpdateOpr(oprId);
		int cnt = this.sysParamMapper.insert(rec);
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
	 * 获取指定分类的参数列表
	 * @param paramTp
	 * @return
	 */
	public List<SysParam> getAllByTp(String paramTp){
		return this.sysParamMapper.selectByTp(paramTp);
	}
	
	/**
	 * 获取所有的参数
	 * @return
	 */
	public List<SysParam> getAll(){
		return this.sysParamMapper.selectAll();
	}
	
	/**
	 * 根据参数名获取指定参数
	 * @param paramName
	 * @return
	 */
	public SysParam get(String paramName) {
		return this.sysParamMapper.selectByKey(paramName);
	}

}
