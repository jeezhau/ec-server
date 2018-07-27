package com.mofangyouxuan.schedule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mofangyouxuan.pay.AliPay;
import com.mofangyouxuan.pay.WXPay;

/**
 * 下载对账单
 * 每天早上10.10下载前一天的交易账单
 * @author jeekhan
 *
 */
@Component
public class DownloadBillSchedule {
	private static Logger logger = LoggerFactory.getLogger(DownloadBillSchedule.class);
	
	@Value("${sys.pay-bills-dir}")
	public String payBillsDir;	//支付账单保存路径
	
	@Autowired
	private WXPay wXPay;
	@Autowired
	private AliPay aliPay;
	
	/**
	 * 下载对账单
	 */
	@Scheduled(cron="10 10 4,8,11 * * ?")
	//@Scheduled(cron="0 11 21 * * ?")
	public void download() {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, -1);
			Date billDate = cal.getTime();
			String strBillDate = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
			//String strBillDate = "20180711";
			billDate = new SimpleDateFormat("yyyyMMdd").parse(strBillDate);
			//下载微信账单
			String wxpayBillFilename = "wxpay{strBillDate}_1.gzip";
			wxpayBillFilename = wxpayBillFilename.replace("{strBillDate}",strBillDate);
			File wxFile = new File(this.payBillsDir,wxpayBillFilename);
			if(!wxFile.exists()) {
				this.wXPay.downloadBill(billDate);
			}
			//下载支付宝账单
			String alipayBillFilename = "alipay{strBillDate}_1.csv.zip";
			alipayBillFilename = alipayBillFilename.replace("{strBillDate}",strBillDate);
			File aliFile = new File(this.payBillsDir,alipayBillFilename);
			if(!aliFile.exists()) {
				this.aliPay.downloadBill(billDate);
			}
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("系统服务【下载对账单】，出现异常，异常信息：" + e.getMessage());
		}
	}
}


