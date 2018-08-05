package com.mofangyouxuan.schedule;

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
import com.mofangyouxuan.model.Aftersale;
import com.mofangyouxuan.model.Appraise;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.OrderBal;
import com.mofangyouxuan.service.AftersaleService;
import com.mofangyouxuan.service.AppraiseService;
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
	
	@Autowired
	private AftersaleService aftersaleService;
	@Autowired
	private AppraiseService appraiseService;
	
	/**
	 * 解冻资金
	 */
	@Scheduled(cron="0 0 4-8/4 * * ?")
	//@Scheduled(cron="0 */3 * * * ?") //测试
	public void unFreezeAmount() {
		JSONObject jsonSearch = new JSONObject();
		jsonSearch.put("status", "41,54,57,58,66,67,68,DS,DR,DF"); //评价完成
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
					Aftersale aftersale = this.aftersaleService.getByID(order.getOrderId());
					Appraise appraise = this.appraiseService.getByOrderIdAndObj(order.getOrderId(), "1");
					if("41".equals(order.getStatus())) {
						apprTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(appraise.getUpdateTime());
					}else if(order.getStatus().startsWith("5") || order.getStatus().startsWith("6")){
						apprTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aftersale.getDealTime());
					}else {
						apprTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aftersale.getApplyTime());
					}
					long gapDays = (new Date().getTime() - apprTime.getTime())/1000/3600/24; //单位天
					if(gapDays > this.uFreezeDays) { //超时
						OrderBal orderBal = this.orderService.getOBal(order.getOrderId());
						if(orderBal != null && (("SS".equals(orderBal.getStatus()) && orderBal.getRefundTime() !=null) ||
								"S".equals(orderBal.getStatus()) && orderBal.getRefundTime() ==null)) {
							this.changeFlowService.dealFinish(orderBal, order.getUserId(), order.getMchtUId(), 1, "商家卖款资金解冻【订单号：" + order.getOrderId() + "】", order.getOrderId());
						}
					}
				}catch(Exception e) {
					log.info("系统资金解冻与分润，系统异常：" + e.getMessage());
				}
			}
		}
	}
}


