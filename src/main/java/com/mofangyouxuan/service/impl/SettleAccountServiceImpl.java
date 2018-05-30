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
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.mapper.SettleAccountMapper;
import com.mofangyouxuan.model.SettleAccount;
import com.mofangyouxuan.service.SettleAccountService;

@Service
@Transactional
public class SettleAccountServiceImpl implements SettleAccountService{
	
	@Autowired
	private SettleAccountMapper settleAccountMapper;
	@Autowired
	private SysParamUtil sysParamUtil;
	/**
	 * 添加账户
	 * @param account
	 * @return
	 */
	public JSONObject add(SettleAccount account) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("vip_id", account.getVipId());
		int hasCnt = this.settleAccountMapper.countAll(params);
		if(hasCnt >= sysParamUtil.getVipAccountCntLimit()) {
			jsonRet.put("errcode", ErrCodes.VIP_ACCOUNT_CNT_LIMIT);
			jsonRet.put("errmsg", "您已经拥有达到最大数量限制的账户数！");
			return jsonRet;
		}
		params.put("accountNo", account.getAccountNo());
		hasCnt = this.settleAccountMapper.countAll(params);
		if(hasCnt >= 0) {
			jsonRet.put("errcode", ErrCodes.VIP_ACCOUNT_SAME_NO);
			jsonRet.put("errmsg", "已有同账号账户！");
			return jsonRet;
		}
		account.setSettleId(null);
		account.setUpdateTime(new Date());
		int cnt = this.settleAccountMapper.insert(account);
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
	 * 更新账户
	 * @param account
	 * @return
	 */
	public JSONObject update(SettleAccount account) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("vip_id", account.getVipId());
		params.put("accountNo", account.getAccountNo());
		List<SettleAccount> list = this.settleAccountMapper.selectAll(params);
		if(list != null && list.size()>0) {
			if(!list.get(0).getSettleId().equals(account.getSettleId())) {
				jsonRet.put("errcode", ErrCodes.VIP_ACCOUNT_SAME_NO);
				jsonRet.put("errmsg", "已有同账号账户！");
				return jsonRet;
			}
		}
		account.setUpdateTime(new Date());
		int cnt = this.settleAccountMapper.updateByPrimaryKeySelective(account);
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
	 * 删除账户
	 * @param account
	 * @return
	 */
	public JSONObject delete(SettleAccount account) {
		JSONObject jsonRet = new JSONObject();
		int cnt = this.settleAccountMapper.deleteByPrimaryKey(account.getSettleId());
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
	 * 获取指定账户
	 * @param settleId
	 * @return
	 */
	public SettleAccount get(Long settleId) {
		return this.settleAccountMapper.selectByPrimaryKey(settleId);
	}
	
	/**
	 * 获取所有账户
	 * @param params
	 * @return
	 */
	public List<SettleAccount> getAll(Map<String,Object> params){
		if(params == null) {
			params = new HashMap<String,Object>();
		}
		return this.settleAccountMapper.selectAll(params);
	}
	
	/**
	 * 统计所有账户
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params) {
		if(params == null) {
			params = new HashMap<String,Object>();
		}
		return this.settleAccountMapper.countAll(params);
	}

}
