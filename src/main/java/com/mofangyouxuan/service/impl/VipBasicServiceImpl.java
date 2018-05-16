package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.ChangeFlowMapper;
import com.mofangyouxuan.mapper.VipBasicMapper;
import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.VipBasicService;

@Service
@Transactional
public class VipBasicServiceImpl implements VipBasicService{
	
	@Autowired
	private VipBasicMapper vipBasicMapper;
	@Autowired
	private ChangeFlowMapper changeFlowMapper;
	
	/**
	 * 添加新会员
	 * @param vipBasic
	 * @return 新用户ID或null
	 */
	@Override
	public Integer add(VipBasic vipBasic) {
		vipBasic.setCreateTime(new Date());
		vipBasic.setBalance(new BigDecimal("0"));
		vipBasic.setFreeze(new BigDecimal("0"));
		vipBasic.setScores(0);
		vipBasic.setStatus("0");//未开通
		int cnt = this.vipBasicMapper.insert(vipBasic);
		if(cnt>0) {
			return vipBasic.getVipId();
		}
		return null;
	}
	
	/**
	 * 更新会员信息
	 * @param vipBasic
	 * @return 更新记录数
	 */
	@Override
	public int update(VipBasic vipBasic) {
		int cnt = this.vipBasicMapper.updateByPrimaryKey(vipBasic);
		return cnt;
	}
	
	/**
	 * 累积账户信息
	 * @param id
	 * @return
	 */
	public VipBasic updAccount(Integer id) {
		VipBasic vip = this.vipBasicMapper.selectByPrimaryKey(id);
		if(vip == null) {
			return null;
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("vipId", id);
		params.put("sumFlag", "0");
		PageCond pageCond = new PageCond(0,1000);
		String sorts = " order by create_time ";
		Date sumTime = new Date();
		BigDecimal addBal = new BigDecimal(0);
		BigDecimal subBal = new BigDecimal(0);
		BigDecimal addFreeze = new BigDecimal(0);
		BigDecimal subFreeze = new BigDecimal(0);
		List<ChangeFlow> list = null; 
		
		params.put("changeType", "1"); //增加可用余额
		list = this.changeFlowMapper.selectAll(params, pageCond, sorts);
		if(list != null) {
			for(ChangeFlow flow:list) {
				addBal = addBal.add(flow.getAmount());
				flow.setSumFlag("1");
				flow.setSumTime(sumTime);
				this.changeFlowMapper.updateByPrimaryKeySelective(flow);
			}
		}
		
		params.put("changeType", "2"); //减少可用余额
		list = this.changeFlowMapper.selectAll(params, pageCond, sorts);
		if(list != null) {
			for(ChangeFlow flow:list) {
				subBal = subBal.add(flow.getAmount());
				flow.setSumFlag("1");
				flow.setSumTime(sumTime);
				this.changeFlowMapper.updateByPrimaryKeySelective(flow);
			}
		}
	
		params.put("changeType", "3"); //增加冻结余额
		list = this.changeFlowMapper.selectAll(params, pageCond, sorts);
		if(list != null) {
			for(ChangeFlow flow:list) {
				addFreeze = addFreeze.add(flow.getAmount());
				flow.setSumFlag("1");
				flow.setSumTime(sumTime);
				this.changeFlowMapper.updateByPrimaryKeySelective(flow);
			}
		}
		
		params.put("changeType", "4"); //减少冻结余额
		list = this.changeFlowMapper.selectAll(params, pageCond, sorts);
		if(list != null) {
			for(ChangeFlow flow:list) {
				subFreeze = subFreeze.add(flow.getAmount());
				flow.setSumFlag("1");
				flow.setSumTime(sumTime);
				this.changeFlowMapper.updateByPrimaryKeySelective(flow);
			}
		}
		
		BigDecimal balance = vip.getBalance();
		balance = balance.add(addBal).subtract(subBal);
		BigDecimal freeze = vip.getFreeze();
		freeze = freeze.add(addFreeze).subtract(subFreeze);
		vip.setBalance(balance);
		vip.setFreeze(freeze);
		vip.setUpdateTime(sumTime);
		this.vipBasicMapper.updateByPrimaryKey(vip);
		return vip;
	}
	
	/**
	 * 根据ID获取会员
	 * @param id
	 * @return
	 */
	@Override
	public VipBasic get(Integer id) {
		return this.updAccount(id);
	}
	
	/**
	 * 根据指定查询条件、分页信息获取变更流水信息
	 * @param jsonParams
	 * @param pageCond
	 * @return
	 */
	@Override
	public List<ChangeFlow> getAll(JSONObject jsonParams,PageCond pageCond){
		String sorts = " order by create_time desc ";
		Map<String,Object> params = getSearParamsMap(jsonParams);
		return this.changeFlowMapper.selectAll(params, pageCond, sorts);
	}

	/**
	 * 根据条件统计变更流水数量
	 */
	@Override
	public int countAll(JSONObject jsonParams) {
		Map<String,Object> params = getSearParamsMap(jsonParams);
		return this.changeFlowMapper.countAll(params);
	}
	
	/**
	 * 解析查询条件
	 * @param jsonParams
	 * @return
	 */
	public Map<String,Object> getSearParamsMap(JSONObject jsonParams){
		Map<String,Object> params = new HashMap<String,Object>();
 
		if(jsonParams.containsKey("vipId")) { //会员ID
			params.put("vipId", jsonParams.getInteger("vipId"));
		}
		if(jsonParams.containsKey("changeType")) {//变更类型
			params.put("changeType", jsonParams.getString("changeType"));
		}
		if(jsonParams.containsKey("amountDown")) { //金额下限
			params.put("amountDown", jsonParams.getDouble("amountDown"));
		}
		if(jsonParams.containsKey("amountUp")) {//金额上限
			params.put("amountUp", jsonParams.getDouble("amountUp"));
		}
		if(jsonParams.containsKey("beginCrtTime")) { //创建开始时间
			params.put("beginCrtTime", jsonParams.getString("beginCrtTime"));
		}
		if(jsonParams.containsKey("endCrtTime")) { //创建结束时间
			params.put("endCrtTime", jsonParams.getString("endCrtTime"));
		}
		if(jsonParams.containsKey("beginSumTime")) { //累积开始时间
			params.put("beginSumTime", jsonParams.getString("beginSumTime"));
		}
		if(jsonParams.containsKey("endSumTime")) { //累积结束时间
			params.put("endSumTime", jsonParams.getString("endSumTime"));
		}
		if(jsonParams.containsKey("createOpr")) { //创建人
			params.put("createOpr", jsonParams.getInteger("createOpr"));
		}
		if(jsonParams.containsKey("reason")) { //理由
			params.put("reason", jsonParams.getString("reason"));
		}
		if(jsonParams.containsKey("sumFlag")) { //累积标志
			params.put("sumFlag", jsonParams.getString("sumFlag"));
		}

		return params;
	}
	
	/**
	 * 更新资金密码
	 * @param vipId
	 * @param passwd
	 * @return
	 */
	@Override
	public int updPwd(Integer vipId,String passwd) {
		int cnt = this.vipBasicMapper.updPasswd(vipId, passwd);
		return cnt;
	}
	
	/**
	 * 更新提现账户信息
	 * @param vipId
	 * @param accountName
	 * @param accountNo
	 * @param accountBank
	 * @return
	 */
	public int updAccount(Integer vipId,String accountName,String accountNo,String accountBank) {
		int cnt = this.vipBasicMapper.updAccount(vipId, accountName, accountNo, accountBank);
		return cnt;
	}
	
}
