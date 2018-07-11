package com.mofangyouxuan.schedule;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.service.ChangeFlowService;
import com.mofangyouxuan.service.OrderService;

/**
 * 商家卖款资金解冻与推广分润
 * @author jeekhan
 *
 */
@Component
public class UnFreezeAndProfitSchedule {
	private static Logger log = LoggerFactory.getLogger(UnFreezeAndProfitSchedule.class);
	@Autowired
	private OrderService orderService;

	@Autowired
	private ChangeFlowService changeFlowService;
	
	@Value("${sys.order-amount-unfreeze-days}")
	private Integer uFreezeDays;	//自从评价以后
	
	/**
	 * 解冻资金
	 */
	@Scheduled(cron="0 0 4-8/4 * * ?")
	//@Scheduled(cron="0 * * * * ?")
	public void unFreezeAmount() {
		JSONObject jsonSearch = new JSONObject();
		jsonSearch.put("status", "41,57"); //评价完成
		JSONObject jsonShow = new JSONObject();
		jsonShow.put("needAppr", true);
		jsonShow.put("needGoodsAndUser", true);
		JSONObject jsonSorts = new JSONObject();
		jsonSorts.put("appraiseTime", "1#1");
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
					Date apprTime = null;
					if("41".equals(order.getStatus())) {
						apprTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(order.getAppraiseTime());
					}else {
						apprTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(order.getAftersalesDealTime());
					}
					long gapDays = (new Date().getTime() - apprTime.getTime())/1000/3600/24; //单位天
					if(gapDays > this.uFreezeDays) { //超时
						Long amount = order.getAmount().multiply(new BigDecimal(100)).longValue();
						//this.changeFlowService.dealFinish(amount, order.getUserId(), order.getMchtUId(), 1, "商家卖款资金解冻【订单号：" + order.getOrderId() + "】", order.getOrderId());
						
						Order updOrder = new Order();
						updOrder.setOrderId(order.getOrderId());
						updOrder.setStatus("CM");
						this.orderService.update(updOrder);
					}
				}catch(Exception e) {
					log.info("系统资金解冻与分润，系统异常：" + e.getMessage());
				}
			}
		}
	}
}


