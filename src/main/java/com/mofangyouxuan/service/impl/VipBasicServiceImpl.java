package com.mofangyouxuan.service.impl;

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
	 * 1、检查会员开通状态；
	 * 2、按日累积未统计的流水
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public JSONObject sumDayFlow() throws Exception {
		JSONObject jsonRet = new JSONObject();
		Date sumTime = new Date();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("sumFlag", "1");
		List<Map<String,Object>> list = null; 
		list = this.changeFlowMapper.sumAllGroupTD(params);
		if(list == null || list.size()<1) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			return jsonRet;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		for(Map<String,Object> rec:list) {
			Integer vipId = (Integer) rec.get("vipId");
			Date ftime = (Date) rec.get("time");
			String ctype = (String) rec.get("ctype");
			Long amount = (Long) rec.get("samount");
			SumBalLog sumLog = this.sumBalLogMapper.selectByVipAndTime(vipId, sdf.format(ftime));
			boolean noLog = false;
			if(sumLog == null) {
				noLog = true;
				sumLog = new SumBalLog();
				sumLog.setVipId(vipId);
				sumLog.setFlowTime(sdf.format(ftime));
				sumLog.setCreateTime(new Date());
				sumLog.setAmountAddbal(0l);
				sumLog.setAmountAddfrz(0l);
				sumLog.setAmountSubbal(0l);
				sumLog.setAmountSubfrz(0l);
			}
			Long addBal=0l,subBal=0l,addFreeze=0l,subFreeze=0l;
			sumLog.setCreateTime(new Date());
			if("1".equals(ctype)) {
				sumLog.setAmountAddbal(sumLog.getAmountAddbal() + amount);
				addBal += amount;
			}else if("2".equals(ctype)) {
				sumLog.setAmountAddfrz(sumLog.getAmountSubbal() + amount);
				subBal += amount;
			}else if("3".equals(ctype)) {
				sumLog.setAmountSubbal(sumLog.getAmountAddfrz() + amount);
				addFreeze += amount;
			}else if("4".equals(ctype)) {
				sumLog.setAmountSubfrz(sumLog.getAmountSubfrz() + amount);
				subFreeze += amount;
			}
			if(noLog) {
				int cnt = this.sumBalLogMapper.insert(sumLog);
				if(cnt<0) {
					throw new Exception("数据库数据保存出错！");
				}
			}else {
				int cnt = this.sumBalLogMapper.updateAmount(sumLog);
				if(cnt<0) {
					throw new Exception("数据库数据保存出错！");
				}
			}
			VipBasic vip = this.vipBasicMapper.selectByPrimaryKey(vipId);
			Long balance = vip.getBalance();
			balance = balance + addBal - subBal;
			Long freeze = vip.getFreeze();
			freeze = freeze + addFreeze - subFreeze;
			vip.setBalance(balance);
			vip.setFreeze(freeze);
			vip.setUpdateTime(sumTime);
			//开通会员
			if("0".endsWith(vip.getStatus())){
				if(vip.getScores() >= sysParamUtil.getActivateVipNeedScore()) {
					vip.setStatus("1"); //激活会员
				}
			}
			int cnt = this.vipBasicMapper.updateByPrimaryKey(vip);
			if(cnt<0) {
				throw new Exception("数据库数据保存出错！");
			}
		}
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		return jsonRet;
	}
	
	/**
	 * 根据ID获取会员
	 * @param id
	 * @return
	 */
	@Override
	public VipBasic get(Integer id) {
		return this.vipBasicMapper.selectByPrimaryKey(id);
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
