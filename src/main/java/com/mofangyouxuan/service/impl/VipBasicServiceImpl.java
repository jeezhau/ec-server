package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.mapper.ChangeFlowMapper;
import com.mofangyouxuan.mapper.SumBalLogMapper;
import com.mofangyouxuan.mapper.VipBasicMapper;
import com.mofangyouxuan.model.ChangeFlow;
import com.mofangyouxuan.model.SumBalLog;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.service.VipBasicService;

@Service
@Transactional
public class VipBasicServiceImpl implements VipBasicService{
	
	@Autowired
	private VipBasicMapper vipBasicMapper;
	@Autowired
	private ChangeFlowMapper changeFlowMapper;
	@Autowired
	private SumBalLogMapper sumBalLogMapper;
	
	@Autowired
	private SysParamUtil sysParamUtil;
	
	/**
	 * 添加新会员
	 * @param vipBasic
	 * @return 新用户ID或null
	 */
	@Override
	public Integer add(VipBasic vipBasic) {
		vipBasic.setCreateTime(new Date());
		vipBasic.setBalance(0L);
		vipBasic.setFreeze(0L);
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
	 * 更新会员积分
	 * @param vipId
	 * @param subScore 需要增加的积分
	 * @return
	 */
	public int updScore(Integer vipId,Integer subScore) {
		int cnt = this.vipBasicMapper.updateScores(vipId, subScore);
		return cnt;
	}
	
	/**
	 * 更新会员开通与余额信息
	 * 1、累积账户余额与积分信息 1000 条流水
	 * 2、检查会员开通状态；
	 * @param id
	 * @return
	 */
	private VipBasic sumBalance(Integer id) {
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
		Long addBal = 0l;
		Long subBal = 0l;
		Long addFreeze = 0l;
		Long subFreeze = 0l;
		List<ChangeFlow> list = null; 
		
		list = this.changeFlowMapper.selectAll(params, pageCond, sorts);
		if(list != null && list.size()>0) {
			String beginFlow = list.get(0).getFlowId();
			String endFlow = list.get(list.size()-1).getFlowId();
			for(ChangeFlow flow:list) {
				if(flow.getChangeType().startsWith("1")) {
					addBal += flow.getAmount();	//增加可用余额
				}else if(flow.getChangeType().startsWith("2")) {
					subBal += flow.getAmount();	//减少可用余额
				}else if(flow.getChangeType().startsWith("3")) {
					addFreeze += flow.getAmount();	//增加冻结余额
				}else if(flow.getChangeType().startsWith("4")) {
					subFreeze += flow.getAmount();	//增加可用余额
				}
				flow.setSumFlag("1");
				flow.setSumTime(sumTime);
				this.changeFlowMapper.updateByPrimaryKeySelective(flow);
			}
			SumBalLog sumLog = new SumBalLog();
			sumLog.setVipId(id);
			sumLog.setBeginFlow(beginFlow);
			sumLog.setEndFlow(endFlow);
			sumLog.setCreateTime(sumTime);
			sumLog.setAmountAddbal(addBal);
			sumLog.setAmountSubbal(subBal);
			sumLog.setAmountAddfrz(addFreeze);
			sumLog.setAmountSubfrz(subFreeze);
			this.sumBalLogMapper.insert(sumLog);
		}
		Long balance = vip.getBalance();
		balance = balance + addBal - subBal;
		Long freeze = vip.getFreeze();
		freeze = freeze + addFreeze - subFreeze;
		vip.setBalance(balance);
		vip.setFreeze(freeze);
		vip.setUpdateTime(sumTime);
		if("0".endsWith(vip.getStatus())){
			if(vip.getScores() >= sysParamUtil.getActivateVipNeedScore()) {
				vip.setStatus("1"); //激活会员
			}
		}
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
		return this.sumBalance(id);
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
		VipBasic vip = new VipBasic();
		vip.setVipId(vipId);
		vip.setPasswd(passwd);
		int cnt = this.vipBasicMapper.updateByPrimaryKeySelective(vip);
		return cnt;
	}
	
	
	/**
	 * 更新手机号
	 * @param vipId
	 * @param phone
	 * @return
	 */
	public int updPhone(Integer vipId,String phone) {
		VipBasic vip = new VipBasic();
		vip.setVipId(vipId);
		vip.setPhone(phone);
		int cnt = this.vipBasicMapper.updateByPrimaryKeySelective(vip);
		return cnt;
	}
	
	/**
	 * 更新邮箱
	 * @param vipId
	 * @param email
	 * @return
	 */
	public int updEmail(Integer vipId,String email) {
		VipBasic vip = new VipBasic();
		vip.setVipId(vipId);
		vip.setEmail(email);
		int cnt = this.vipBasicMapper.updateByPrimaryKeySelective(vip);
		return cnt;
	}
	
}
