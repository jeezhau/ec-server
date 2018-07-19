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
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.AftersaleMapper;
import com.mofangyouxuan.model.Aftersale;
import com.mofangyouxuan.service.AftersaleService;

@Service
@Transactional
public class AftersaleServiceImpl implements AftersaleService {
	
	@Autowired
	private AftersaleMapper aftersaleMapper;
	
	@Override
	public JSONObject saveAF(Aftersale aftersale) {
		JSONObject jsonRet = new JSONObject();
		Date currDate = new Date();
		aftersale.setApplyTime(currDate);
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", aftersale.getOrderId());
		int hasCnt1 = this.aftersaleMapper.countAll(params);
		int cnt;
		if(hasCnt1 <= 0) {
			cnt = this.aftersaleMapper.insert(aftersale);
		}else {
			cnt = this.aftersaleMapper.updateByPrimaryKey(aftersale);
		}
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}

	@Override
	public JSONObject updateAF(Aftersale aftersale) {
		JSONObject jsonRet = new JSONObject();
		Date currDate = new Date();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", aftersale.getOrderId());
		int hasCnt1 = this.aftersaleMapper.countAll(params);
		if(hasCnt1 < 0) {
			jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
			jsonRet.put("errmsg", "系统中没有该订单的售后申请信息！");
			return jsonRet;
		}
		aftersale.setApplyTime(currDate);
		int cnt = this.aftersaleMapper.updateByPrimaryKey(aftersale);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
		}
		return jsonRet;
	}
	
	@Override
	public Aftersale getByID(String orderId) {
		return this.aftersaleMapper.selectByPrimaryKey(orderId);
	}

	@Override
	public int countAll(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Aftersale> getAll(Map<String, Object> params, PageCond pageCond) {
		// TODO Auto-generated method stub
		return null;
	}

}
