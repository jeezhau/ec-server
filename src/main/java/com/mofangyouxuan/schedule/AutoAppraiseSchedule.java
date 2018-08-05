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
import com.mofangyouxuan.service.AftersaleService;
import com.mofangyouxuan.service.AppraiseService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;

/**
 * 对没有主动评价的订单进行自动评价
 * @author jeekhan
 *
 */
@Component
public class AutoAppraiseSchedule {
	private static Logger log = LoggerFactory.getLogger(AutoAppraiseSchedule.class);
	@Autowired
	private OrderService orderService;

	@Autowired
	private PartnerBasicService partnerBasicService;
	
	@Autowired
	private AppraiseService appraiseService;
	
	@Autowired
	private AftersaleService aftersaleService;
	
	@Value("${sys.order-auto-appr-days}")
	private Integer autoApprDays;
	
	/**
	 * 自动评价
	 */
	@Scheduled(cron="0 0 4-8/4 * * ?")
	//@Scheduled(cron="0 * * * * ?")  //测试
	public void autoAppraise() {
		JSONObject jsonSearch = new JSONObject();
		jsonSearch.put("status", "40,56"); //待评价
		JSONObject jsonShow = new JSONObject();
		jsonShow.put("needAppr", true);
		jsonShow.put("needGoodsAndUser", true);
		JSONObject jsonSorts = new JSONObject();
		jsonSorts.put("signTime", "1#1");
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
					Order updOrder = new Order();
					updOrder.setOrderId(order.getOrderId());
					Date signTime = null;
					if("40".equals(order.getStatus())) {
						signTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(order.getSignTime());
					}else {
						Aftersale afterale = this.aftersaleService.getByID(order.getOrderId());
						signTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(afterale.getDealTime());
					}
					long gapDays = (new Date().getTime() - signTime.getTime())/1000/3600/24; //单位天
					if(gapDays > this.autoApprDays) { //超时
						Appraise appr1 = this.appraiseService.getByOrderIdAndObj(order.getOrderId(), "1");
						Appraise appr2 = this.appraiseService.getByOrderIdAndObj(order.getOrderId(), "2");
						if(appr1 == null || appr2 == null) {
							Integer scoreLogis =10, scoreServ =10, scoreGoods=10;
							if(appr1 == null) {
								appr1 = new Appraise();
								appr1.setGoodsId(order.getGoodsId());
								appr1.setOrderId(order.getOrderId());
								appr1.setScoreGoods(scoreGoods);
								appr1.setScoreLogistics(scoreLogis);
								appr1.setScoreMerchant(scoreServ);
								appr1.setStatus("S");
								this.appraiseService.save(appr1);
							}
							if(appr2 == null) {
								appr2 = new Appraise();
								appr2.setGoodsId(order.getGoodsId());
								appr2.setOrderId(order.getOrderId());
								appr2.setScoreUser(10);
								appr2.setStatus("S");
								this.appraiseService.save(appr2);
							}
							if("40".equals(order.getStatus())) {
								updOrder.setStatus("41");
							}else {
								updOrder.setStatus("57");
							}
							this.orderService.update(updOrder);
							Integer partnerId = order.getPartnerId();
							this.partnerBasicService.updScore(partnerId, scoreLogis, scoreServ, scoreGoods);
							continue;
						}
					}
				}catch(Exception e) {
					log.info("系统自动评价，系统异常：" + e.getMessage());
				}
			}
		}
	}
}


