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
import com.mofangyouxuan.mapper.AppraiseMapper;
import com.mofangyouxuan.mapper.OrderMapper;
import com.mofangyouxuan.model.Appraise;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.service.AppraiseService;
import com.mofangyouxuan.service.PartnerBasicService;

@Service
@Transactional
public class AppraiseServiceImpl implements AppraiseService{
	
	@Autowired
	private AppraiseMapper appraiseMapper;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private OrderMapper orderMapper;

	@Override
	public JSONObject save(Appraise appraise) {
		JSONObject jsonRet = new JSONObject();
		Date currDate = new Date();
		
		Appraise old = this.getByOrderIdAndObj(appraise.getOrderId(), appraise.getObject());
		int cnt;
		appraise.setUpdateTime(currDate);
		if(old == null) {
			cnt = this.appraiseMapper.insert(appraise);
		}else {
			appraise.setApprId(old.getApprId());
			cnt = this.appraiseMapper.updateByPrimaryKey(appraise);
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
	public JSONObject update(Appraise appraise) {
		JSONObject jsonRet = new JSONObject();
		Date currDate = new Date();
		Appraise old = this.getByOrderIdAndObj(appraise.getOrderId(), appraise.getObject());
		if(old == null) {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库数据保存错误！");
			return jsonRet;
		}
		appraise.setUpdateTime(currDate);
		appraise.setApprId(old.getApprId());
		int cnt = this.appraiseMapper.updateByPrimaryKey(appraise);
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
	public Appraise getByID(Long Id) {
		return this.appraiseMapper.selectByPrimaryKey(Id);
	}

	@Override
	public int countAll(Map<String, Object> params) {
		return this.appraiseMapper.countAll(params);
	}

	@Override
	public List<Appraise> getAll(Map<String, Object> params, PageCond pageCond) {
		return this.appraiseMapper.selectAll(params, pageCond);
	}
	
	public Appraise getByOrderIdAndObj(String orderId,String object) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", orderId);
		params.put("object", object);
		List<Appraise> list = this.appraiseMapper.selectAll(params, new PageCond(0,1));
		if(list != null && list.size()>0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	/**
	 * 添加买家对商家的评价或者系统自动超时评价
	 * @param order	订单信息
	 * @param scoreLogistics		物流得分
	 * @param scoreMerchant	商家服务得分
	 * @param scoreGoods		商品描述得分
	 * @param content	评价内容
	 * @return
	 */
	@Override
	public JSONObject appraise2Mcht(Order order,Integer scoreLogistics,Integer scoreMerchant,
			Integer scoreGoods,String content) {
		JSONObject jsonRet = new JSONObject();
		//更新订单信息
		Date currTime = new Date();
		Order updOdr = new Order();
		if("30".equals(order.getStatus()) || "31".equals(order.getStatus()) || "40".equals(order.getStatus()) || "41".equals(order.getStatus())) {
			updOdr.setStatus("41"); //41:评价完成
		}else {
			updOdr.setStatus("57"); //57：评价完成（换货结束）
		}
		if("30".equals(order.getStatus()) || "31".equals(order.getStatus()) || "54".equals(order.getStatus()) || "55".equals(order.getStatus())){
			updOdr.setSignTime(currTime);
			updOdr.setSignUser(order.getNickname());
		}
		updOdr.setOrderId(order.getOrderId());
		Appraise appraise = this.getByOrderIdAndObj(order.getOrderId(), "1");
		if(appraise == null) {
			appraise = new Appraise();
		}
		appraise.setGoodsId(order.getGoodsId());
		appraise.setOrderId(order.getOrderId());
		appraise.setObject("1");
		appraise.setScoreGoods(scoreGoods);
		appraise.setScoreLogistics(scoreLogistics);
		appraise.setScoreMerchant(scoreMerchant);
		appraise.setUpdateTime(currTime);
		appraise.setStatus("0");
		if(content != null && content.length()>1) {
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
			asr.put("content", content);
			String oldCnt = appraise.getContent()==null ? "[]" : appraise.getContent();
			JSONArray asrArr = JSONArray.parseArray(oldCnt);
			asrArr.add(0, asr);
			appraise.setContent(asrArr.toJSONString());
		}
		//更新商户积分
		this.partnerBasicService.updScore(order.getPartnerId(), scoreLogistics, scoreMerchant, scoreGoods);
		//
		this.orderMapper.updateByPrimaryKeySelective(updOdr);
		jsonRet = this.save(appraise);
		return jsonRet;
	}
	
	/**
	 * 添加卖家对买家的评价或者系统自动超时评价
	 * @param order	订单信息
	 * @param score	得分
	 * @param content	评价内容
	 * @return
	 */
	@Override
	public JSONObject appraise2User(Order order,Integer score,String content,Integer updateOpr) {
		JSONObject jsonRet = new JSONObject();
		Date currTime = new Date();
		Appraise appraise = this.getByOrderIdAndObj(order.getOrderId(), "2");
		if(appraise == null) {
			appraise = new Appraise();
		}
		appraise.setScoreUser(score);
		appraise.setGoodsId(order.getGoodsId());
		appraise.setOrderId(order.getOrderId());
		appraise.setObject("2");
		appraise.setStatus("0");
		appraise.setUpdateTime(currTime);
		if(content != null && content.length()>1) {
			JSONObject asr = new JSONObject();
			asr.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currTime));
			asr.put("operator", updateOpr);
			asr.put("content", content);
			String oldCnt = appraise.getContent()==null ? "[]" : appraise.getContent();
			JSONArray apprCnt = JSONArray.parseArray(oldCnt);
			apprCnt.add(0,asr);
			appraise.setContent(apprCnt.toJSONString());
		}
		jsonRet = this.save(appraise);
		return jsonRet;
	}
	
	/**
	 * 记录评价审批结果
	 * @param orderId
	 * @param rewPartnerId
	 * @param oprId
	 * @param result 审批结果：1-审核通过，2-审核拒绝
	 * @param review
	 * @return
	 */
	public JSONObject review(String orderId,Integer rewPartnerId,Integer oprId,String result,String review) {
		JSONObject jsonRet = new JSONObject();
		//更新订单信息
		Appraise appraise = this.getByOrderIdAndObj(orderId, "1");
		if(appraise == null) {
			jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
			jsonRet.put("errmsg", "系统中没有该订单的用户对商家的评价信息！");
		}
		appraise.setStatus(result);
		int cnt = this.appraiseMapper.updateByPrimaryKeySelective(appraise);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据出错！");
		}
		return jsonRet;
	}
	
}
