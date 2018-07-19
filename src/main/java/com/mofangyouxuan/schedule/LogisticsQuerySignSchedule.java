package com.mofangyouxuan.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.Aftersale;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.service.AftersaleService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.utils.HttpUtils;

/**
 * 物流查询与签收
 * 1、超过时间限制的订单系统自动签收；
 * 2、获取快递物流信息；
 * @author jeekhan
 *
 */
@Component
public class LogisticsQuerySignSchedule {
	private static Logger log = LoggerFactory.getLogger(LogisticsQuerySignSchedule.class);
	@Autowired
	private OrderService orderService;
	
	@Value("${logistics.alicloudapi-appcode}")
	private String appcode;
	
	@Autowired
	private AftersaleService aftersaleService;

	@Autowired
	private SysParamUtil sysParamUtil;
	
	/**
	 * 物流查询与签收
	 */
	@Scheduled(cron="0 0 3-9/3 * * ?")
	public void queryAndSign() {
		JSONObject jsonSearch = new JSONObject();
		jsonSearch.put("status", "22,30,55"); //待签收
		//jsonSearch.put("dispatchMode", "3"); //快递配送
		JSONObject jsonShow = new JSONObject();
		jsonShow.put("needReceiver", true);
		jsonShow.put("needLogistics", true);
		JSONObject jsonSorts = new JSONObject();
		jsonSorts.put("sendTime", "1#1");
		PageCond pageCond = new PageCond(0,100);
		int cnt = this.orderService.countAll(jsonSearch);
		pageCond.setCount(cnt);
		if(cnt<=0) {
			return;
		}
		int loopCnt = cnt/100+1;
		if(loopCnt>100) {
			loopCnt = 100;
		}
		for(int n=0;n<loopCnt;n++) {
			List<Order> list = this.orderService.getAll(jsonShow,jsonSearch, jsonSorts, pageCond);
			if(list == null || list.size()<=0) {
				return;
			}
			for(Order order:list) {
				try {
					String orderStatus = order.getStatus();
					String dispatchMode = order.getDispatchMode();
					Order updOrder = new Order();
					updOrder.setOrderId(order.getOrderId());
					//超时签收
					Date sendTime = null;
					if("55".equals(orderStatus)) {
						Aftersale aftersale = this.aftersaleService.getByID(order.getOrderId());
						sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aftersale.getDealTime());
					}else {
						sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(order.getSendTime());
					}
					long gapDays = (new Date().getTime() - sendTime.getTime())/1000/3600/24; //单位天
					int dd = this.sysParamUtil.getNoSignDates4Refund() + (order.getSignProlong() == null ? 3:order.getSignProlong());
					if(gapDays > dd) { //超时
						if(!"55".equals(orderStatus)) {
							updOrder.setSignTime(new Date());
							updOrder.setSignUser("系统超时自动签收");
							updOrder.setStatus("40"); //待评价
						}else {
							updOrder.setStatus("56");
						}
						this.orderService.update(updOrder);
						continue;
					}
					//快递查询签收
					if(!"3".equals(dispatchMode) || "55".equals(orderStatus)) {
						continue;
					}
					String oldLogs = order.getLogistics();
					String logisticsNo = order.getLogisticsNo();
					if(logisticsNo == null) {
						continue;
					}
					if(oldLogs != null) {
						JSONObject old = JSONObject.parseObject(oldLogs);
						String oldStat = old.getString("status");
						if(!"0".equals(oldStat) && !"205".equals(oldStat)) {
							continue;
						}
					}
					JSONObject jsonRet = query(logisticsNo,""); 
					if(jsonRet == null) {
						continue;
					}
					//查询有返回
					String status = jsonRet.getString("status");
					if("0".equals(status)) {//得到正确结果
						JSONObject result = jsonRet.getJSONObject("result");
						String issign = result.getString("issign");
						if("1".equals(issign)) {
							Date signTime = new Date();
							if(result.getJSONArray("list") != null && result.getJSONArray("list").size()>0) {
								String time = result.getJSONArray("list").getJSONObject(0).getString("time");
								signTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
							}
							updOrder.setSignTime(signTime);
							updOrder.setStatus("40"); //待评价
						}
					}
					updOrder.setLogistics(jsonRet.toJSONString());
					this.orderService.update(updOrder);
				}catch(Exception e) {
					log.info("物流查询，系统异常：" + e.getMessage());
				}
			}
		}
	}
	

	/**
	 * 
	 * @param logicticsNo
	 * @param logisticsType
	 * @return {
		"status": "0",
		"msg": "ok",
		"result": {
			"number": "780098068058",
			"type": "zto",
			"list": [{"time": "2018-03-09 11:59:26","status": "【石家庄市】 快件已在 【长安三部】 签收,签收人: 本人, 感谢使用中通快递,期待再次为您服务!"}...],
			"deliverystatus": "3",          1.在途中 2.正在派件 3.已签收 4.派送失败
			"issign": "1",                  1.是否签收
			"expName": "中通快递",
			"expSite": "www.zto.com",
			"expPhone": "95311"
		 }
		}
	 */
	public JSONObject query(String logicticsNo,String logisticsType) {
		String url = "http://wuliu.market.alicloudapi.com/kdi";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode); //格式为:Authorization:APPCODE 83359fd73fe11248385f570e3c139xxx
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("no", logicticsNo);// !!! 请求参数
        params.put("type", logisticsType);// !!! 请求参数
        try {
        		String ret = HttpUtils.doGet(url, headers,params);
        		JSONObject jsonRet = JSONObject.parseObject(ret);
        		if(!jsonRet.containsKey("status")) {  //查询成功
        			return null;
        		}
        		return jsonRet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}
	
}


