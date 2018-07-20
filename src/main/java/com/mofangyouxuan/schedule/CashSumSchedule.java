package com.mofangyouxuan.schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mofangyouxuan.service.VipBasicService;

/**
 * 资金流水统计
 * @author jeekhan
 *
 */
@Component
public class CashSumSchedule {
	private static Logger logger = LoggerFactory.getLogger(CashSumSchedule.class);
	@Autowired
	private VipBasicService vipBasicService;

	/**
	 * 资金流水统计
	 * 1、每日统计
	 */
	@Scheduled(cron="0 0 12,20 * * ?")
	//@Scheduled(cron="0 22 22 * * ?")
	public void dateSum() {
		try {
			this.vipBasicService.sumDetailFlowByDay(null, null);
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("系统定时服务【每日资金流水统计】，系统异常，异常信息：" + e.getMessage());
		}
	}
	
	/**
	 * 资金流水统计
	 * 1、每月3号6点统计上一月
	 */
	@Scheduled(cron="0 0 6 3 * ?")
	//@Scheduled(cron="0 24 22 * * ?")
	public void monthSum() {
		try {
			Calendar cal = Calendar.getInstance();
			//cal.add(Calendar.MONTH, -1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
			this.vipBasicService.sumFlowByTime(true, sdf.format(cal.getTime()), null);
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("系统定时服务【每月资金流水统计】，系统异常，异常信息：" + e.getMessage());
		}
	}
	
	/**
	 * 资金流水统计
	 * 1、每年1月10号5点统计上一年
	 */
	@Scheduled(cron="0 0 5 10 1 ?")
	//@Scheduled(cron="0 21 21 * * ?")
	public void yearSum() {
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			this.vipBasicService.sumFlowByTime(false, sdf.format(cal.getTime()), null);
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("系统定时服务【每年资金流水统计】，系统异常，异常信息：" + e.getMessage());
		}
	}
}


