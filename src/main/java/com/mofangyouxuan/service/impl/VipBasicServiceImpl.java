package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.mapper.ChangeFlowMapper;
import com.mofangyouxuan.mapper.SumBalLogMapper;
import com.mofangyouxuan.mapper.VipBasicMapper;
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
	 * 更新会员积分并激活会员
	 * 
	 * @param vipId
	 * @param subScore 需要增加的积分
	 * @return
	 * @throws Exception 
	 */
	public int updScore(Integer vipId,Integer subScore) throws Exception {
		int cnt = this.vipBasicMapper.updateScores(vipId, subScore);
		VipBasic vip = this.get(vipId);
		if(vip != null) {
			if("0".equals(vip.getStatus()) && vip.getScores()>=this.sysParamUtil.getActivateVipNeedScore()) {
				VipBasic updVip = new VipBasic();
				updVip.setVipId(vipId);
				updVip.setStatus("1");
				this.vipBasicMapper.updateByPrimaryKeySelective(updVip);
			}
		}
		return cnt;
	}
	
	/**
	 * 更新会员开通与余额信息
	 * 1、检查会员开通状态；
	 * 2、按日累积未统计的流水
	 * @param date 统计的日期
	 * @param id	   会员ID，可为空
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public JSONObject sumDetailFlowByDay(Date date,Integer vipId) throws Exception {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("sumFlag", "1,S");
		if(date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			params.put("beginCrtTime", sdf.format(date));
			params.put("endCrtTime", sdf.format(date));
		}
		if(vipId != null) {
			params.put("vipId", vipId);
		}
		List<Map<String,Object>> list = null; 
		list = this.changeFlowMapper.sumAllByVipDateCt(params);
		if(list == null || list.size()<1) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			return jsonRet;
		}
		for(Map<String,Object> rec:list) {
			Integer vId = (Integer) rec.get("vip_id");
			String ftime = (String) rec.get("time");
			String ctype = (String) rec.get("ctype");
			BigDecimal amount = (BigDecimal) rec.get("samount");
			SumBalLog sumLog = this.sumBalLogMapper.selectByVipAndTime(vId, ftime);
			boolean noLog = false;
			if(sumLog == null) {
				noLog = true;
				sumLog = new SumBalLog();
				sumLog.setVipId(vId);
				sumLog.setFlowTime(ftime);
				sumLog.setCreateTime(new Date());
				sumLog.setAmountAddbal(0l);
				sumLog.setAmountAddfrz(0l);
				sumLog.setAmountSubbal(0l);
				sumLog.setAmountSubfrz(0l);
			}
			sumLog.setCreateTime(new Date());
			if("1".equals(ctype)) {
				sumLog.setAmountAddbal( amount.longValue());
			}else if("2".equals(ctype)) {
				sumLog.setAmountSubbal( amount.longValue());
			}else if("3".equals(ctype)) {
				sumLog.setAmountAddfrz(amount.longValue());
			}else if("4".equals(ctype)) {
				sumLog.setAmountSubfrz( amount.longValue());
			}
			if(noLog) {
				int cnt = this.sumBalLogMapper.insert(sumLog);
			}else {
				int cnt = this.sumBalLogMapper.updateAmount(sumLog);
			}
		}
		this.changeFlowMapper.updateFlag(params, "S");
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		return jsonRet;
	}
	
	/**
	 * 按月统计资金流水
	 * 1、从统计表中整理月份统计，同时清除日统计记录；
	 * 2、
	 * @param isMohtn 	是否月份统计，否则按年统计
	 * @param time	月份:yyyyMM，年:yyyy
	 * @param vipId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public JSONObject sumFlowByTime(boolean isMonth,String time,Integer vipId) throws Exception {
		JSONObject jsonRet = new JSONObject();
		List<SumBalLog> list = null;
		if(isMonth) {
			list = this.sumBalLogMapper.sumByVIPMonth(vipId, time);
		}else {
			list = this.sumBalLogMapper.sumByVIPYear(vipId, time);
		}
		if(list == null || list.size()<1) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			return jsonRet;
		}
		for(SumBalLog rec:list) {
			SumBalLog oldLog = this.sumBalLogMapper.selectByVipAndTime(rec.getVipId(), rec.getFlowTime());
			SumBalLog monthLog = new SumBalLog();
			monthLog.setVipId(rec.getVipId());
			monthLog.setFlowTime(rec.getFlowTime());
			monthLog.setCreateTime(new Date());
			monthLog.setAmountAddbal(rec.getAmountAddbal());
			monthLog.setAmountAddfrz(rec.getAmountAddfrz());
			monthLog.setAmountSubbal(rec.getAmountSubbal());
			monthLog.setAmountSubfrz(rec.getAmountSubfrz());
			
			this.sumBalLogMapper.deleteFlows(rec.getVipId(), rec.getFlowTime());
			
			int cnt = this.sumBalLogMapper.insert(monthLog);
		}
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		return jsonRet;
	}
	
	/**
	 * 获取会员余额
	 * @param vipId
	 * @return
	 * @throws Exception
	 */
	public VipBasic getVipBal(Integer vipId) throws Exception{
		this.sumDetailFlowByDay(new Date(), vipId); //重新统计当日
		SumBalLog sumAll = this.sumBalLogMapper.sumAllByVIP(vipId);
		if(sumAll != null && sumAll.getVipId() != null) {
			Long balance = sumAll.getAmountAddbal()-sumAll.getAmountSubbal();
			Long freeze = sumAll.getAmountAddfrz()-sumAll.getAmountSubfrz();
			VipBasic updVip = new VipBasic();
			updVip.setVipId(vipId);
			updVip.setBalance(balance);
			updVip.setFreeze(freeze);
			this.vipBasicMapper.updateByPrimaryKeySelective(updVip);
		}
		VipBasic vip = this.vipBasicMapper.selectByPrimaryKey(vipId);
		return vip;
	}
	
	
	/**
	 * 根据ID获取会员
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	@Override
	public VipBasic get(Integer vipId) throws Exception {
		VipBasic vip = this.vipBasicMapper.selectByPrimaryKey(vipId);
		return vip;
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
	
	
}
