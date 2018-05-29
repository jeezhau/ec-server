package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.model.SettleAccount;

public interface SettleAccountService {
	
	/**
	 * 添加账户
	 * @param account
	 * @return
	 */
	public JSONObject add(SettleAccount account);
	
	/**
	 * 更新账户
	 * @param account
	 * @return
	 */
	public JSONObject update(SettleAccount account);
	
	/**
	 * 删除账户
	 * @param account
	 * @return
	 */
	public JSONObject delete(SettleAccount account);
	
	/**
	 * 获取指定账户
	 * @param settleId
	 * @return
	 */
	public SettleAccount get(Long settleId);
	
	/**
	 * 获取所有账户
	 * @param params
	 * @return
	 */
	public List<SettleAccount> getAll(Map<String,Object> params);
	
	/**
	 * 统计所有账户
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params);

}
