package com.mofangyouxuan.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
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

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.pay.WXPay;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.utils.CHZipUtils;
import com.mofangyouxuan.utils.FileFilter;

/**
 * 订单对账
 * 1、根据下载的系统对账单文件进行订单对账；
 * 2、每天早上11:30分执行对账；
 * @author jeekhan
 *
 */
@Component
public class BalanceBillSchedule {
	private static Logger logger = LoggerFactory.getLogger(BalanceBillSchedule.class);
	@Autowired
	private OrderService orderService;

	@Autowired
	private WXPay wXpay;
	
	@Value("${sys.pay-bills-dir}")
	public String payBillsDir="/Users/jeekhan/mfyx/paybills/";	//支付账单保存路径
	
	/**
	 * 执行订单支付对账
	 */
	@SuppressWarnings("unused")
	@Scheduled(cron="0 42 18 * * ?")
	public void balanceBill() {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, -1);
			//String strBillDate = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
			String strBillDate = "20180711";
			//微信账单
			String wxpayBillFilename = "wxpay{strBillDate}_1.gzip";
			wxpayBillFilename = wxpayBillFilename.replace("{strBillDate}",strBillDate);
			File wxFile = new File(this.payBillsDir,wxpayBillFilename);
			if(!wxFile.exists()) {
				logger.info("系统服务【订单对账】，没有微信的对账数据，交易日【" + strBillDate +"】！");
			}else {
				CHZipUtils.unGzip(this.payBillsDir+wxpayBillFilename, this.payBillsDir+wxpayBillFilename.replace(".gzip", ""));
				wxFile = new File(this.payBillsDir,wxpayBillFilename.replaceAll(".gzip", ""));//解压后文件
				FileInputStream fis = new FileInputStream(wxFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String header = br.readLine();//文件头
				String countHeader = null;
				String countData = null;
				//交易时间,公众账号ID,商户号,子商户号,设备号,微信订单号,商户订单号,用户标识,交易类型,交易状态,付款银行,货币种类,总金额,企业红包金额,微信退款单号,商户退款单号,退款金额,企业红包退款金额,退款类型,退款状态,商品名称,商户数据包,手续费,费率
				String cntHeader = "总交易单数,总交易额,总退款金额,总企业红包退款金额,手续费总金额";
				String line = null;
				while((line=br.readLine())!=null) {
					try {
						line = line.trim();
						if(line.length()<1) {
							continue;
						}
						if(cntHeader.equals(line)) {
							countHeader = line;
						}
						if(countHeader != null) {
							countData = line;
						}
						if(countData == null) {
							this.dealWXBill(line);
						}
					}catch(Exception e) {
						logger.info("系统定时服务【订单支付对账】，微信账单处理出现异常，数据【" + line + "】");
					}
				}
				fis.close();
				br.close();
			}
			//支付宝账单
			String alipayBillFilename = "alipay{strBillDate}_1.csv.zip";
			alipayBillFilename = alipayBillFilename.replace("{strBillDate}",strBillDate);
			File aliFile = new File(this.payBillsDir,alipayBillFilename);
			if(!aliFile.exists()) {
				logger.info("系统服务【订单对账】，没有微信的对账数据，交易日【" + strBillDate +"】！");
			}else {
				CHZipUtils.unZip(this.payBillsDir + alipayBillFilename, this.payBillsDir + alipayBillFilename.replace(".zip", "/"));
				File[] aliFileList = new File(this.payBillsDir,alipayBillFilename.replaceAll(".zip", "")).listFiles(new FileFilter("业务明细.csv"));//解压后文件
				if(aliFileList.length>0) {
					aliFile = aliFileList[0];
					FileInputStream fis = new FileInputStream(aliFile);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis,"gbk"));
					//支付宝交易号,商户订单号,业务类型,商品名称,创建时间,完成时间,门店编号,门店名称,操作员,终端号,对方账户,订单金额（元）,商家实收（元）,支付宝红包（元）,集分宝（元）,支付宝优惠（元）,商家优惠（元）,券核销金额（元）,券名称,商家红包消费金额（元）,卡消费金额（元）,退款批次号/请求号,服务费（元）,分润（元）,备注
					//String header = br.readLine();//文件头
					String line = null;
					while((line=br.readLine())!=null) {
						try {
							line = line.trim();
							if(line.length()<1) {
								continue;
							}
							if(line.startsWith("#")) {
								continue;
							}
							if(line.startsWith("支付宝交易号,")) {
								//header = line;
							}else{
								this.dealAliBill(line);
							}
						}catch(Exception e) {
							logger.info("系统定时服务【订单支付对账】，支付宝账单处理出现异常，数据【" + line + "】");
						}
					}
					fis.close();
					br.close();
				}
			}
			//会员余额对账
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("status", "11,21"); //成功
			params.put("beginIncomeTime", strBillDate);
			params.put("endIncomeTime", strBillDate);
			PageCond pageCond = new PageCond(0,100);
			int cnt = this.orderService.countPayFlow(params);
			pageCond.setCount(cnt);
			if(cnt<=0) {
				return;
			}
			int loopCnt = cnt/100+1;
			if(loopCnt>100) {
				loopCnt = 100;
			}
			for(int n=0;n<loopCnt;n++) {
				List<PayFlow> list = this.orderService.getAllPayFlow(params, pageCond);
				if(list == null || list.size()<=0) {
					return;
				}
				for(PayFlow payFlow:list) {
					try {
						if("1".equals(payFlow.getFlowType())) {
							this.orderService.balanceBill(false, payFlow);
						}else {
							this.orderService.balanceBill(true, payFlow);
						}
					}catch(Exception e) {
						logger.info("系统定时服务【订单支付对账】，会员余额支付处理出现异常，数据【" + payFlow.getFlowId() + "】");
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("系统定时服务【订单支付对账】，系统异常，异常信息：" + e.getMessage());
		}
	}
	
	private String dealWXBill(String dataLine) throws Exception {
		dataLine = dataLine.replaceFirst("`", "");
		String[] data = dataLine.split(",`");
		//0-交易时间,1-公众账号ID,2-商户号,3-子商户号,4-设备号,5-微信订单号,6-商户订单号,7-用户标识,8-交易类型,9-交易状态,
		//10-付款银行,11-货币种类,12-总金额,13-企业红包金额,14-微信退款单号,15-商户退款单号,16-退款金额,17-企业红包退款金额,
		//18-退款类型,19-退款状态,20-商品名称,21-商户数据包,22-手续费,23-费率
		String appId = data[1];
		String mchtid = data[2];
		if(!this.wXpay.appId.equals(appId) && !this.wXpay.wxMchtId.equals(mchtid)) {
			return "商户号与APPID不符！";
		}
		String outTradeNo = data[5].trim();
		String payFlowId = data[6].trim();
		String payType = data[8].trim();
		String payStatus = data[9].trim();
		String payAmount = data[12].trim();
		String refundOutNo = data[14].trim();
		String refundPayFlowId = data[15].trim();
		String refundAmount = data[16].trim();
		String refundStatus = data[19].trim();
		String fee = data[22];
		if(refundPayFlowId != null && refundPayFlowId.length()>30) {
			this.orderService.balanceBill(true, refundOutNo, refundPayFlowId, this.wXpay.getPayTypeCode(payType), refundStatus, refundAmount, fee);
		}else {
			this.orderService.balanceBill(false, outTradeNo, payFlowId, this.wXpay.getPayTypeCode(payType), payStatus, payAmount, fee);
		}
		return "00";
	}
	
	private String dealAliBill(String dataLine) throws Exception {
		String[] data = dataLine.split(",");
		//0-支付宝交易号,1-商户订单号,2-业务类型,3-商品名称,4-创建时间,5-完成时间,6-门店编号,7-门店名称,8-操作员,9-终端号,
		//10-对方账户,11-订单金额（元）,12-商家实收（元）,13-支付宝红包（元）,14-集分宝（元）,15-支付宝优惠（元）,16-商家优惠（元）,17-券核销金额（元）,18-券名称,19-商家红包消费金额（元）,20-卡消费金额（元）,
		//21-退款批次号/请求号,22-服务费（元）,23-分润（元）,24-备注
		String outTradeNo = data[0].trim();
		String payFlowId = data[1].trim();
		String refundId = data[21].trim();
		String tradeType = data[2].trim();
		String payType = "3";
		String status = "SUCCESS";
		String amount = data[12].trim();
		String fee = data[23].trim();
		if("退款".equals(tradeType)) {
			fee = fee.replace("-", "");
			amount = amount.replace("-", "");
			this.orderService.balanceBill(true, outTradeNo, refundId, payType, status, amount, fee);
		}else {
			this.orderService.balanceBill(false, outTradeNo, payFlowId, payType, status, amount, fee);
		}
		return "00";
	}
	
	
	public static void main(String[] args) {
		BalanceBillSchedule bs = new BalanceBillSchedule();
		bs.balanceBill();
	}
}


