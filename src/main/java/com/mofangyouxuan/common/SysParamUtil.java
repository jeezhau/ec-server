package com.mofangyouxuan.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mofangyouxuan.model.SysParam;
import com.mofangyouxuan.service.SysParamService;

@Component
public class SysParamUtil {
	private static Map<String,String> SYS_PARAM_MAP = null;
	private static Long LAST_UPD_TIME = null; 	//上次更新时间
	
	@Autowired
	private SysParamService sysParamService;
	
	//系统顶级合作伙伴ID
	private Integer sysPartnerId = 1000;
	public Integer getSysPartnerId() {
		return this.sysPartnerId;
	}
	
	//系统默认最低服务费费率1.2%，百分数，针对交易资金
	private BigDecimal sysLowestServiceFeeRate = new BigDecimal(1.20);
	public BigDecimal getSysLowestServiceFeeRate() {
		return this.sysLowestServiceFeeRate;
	}
	
	//系统默认最高服务费费率10%，百分数，针对交易资金
	private BigDecimal sysHighestServiceFeeRate = new BigDecimal(10.00);
	public BigDecimal getSysHighestServiceFeeRate() {
		return this.sysHighestServiceFeeRate;
	}
	
	//平台默认收取服务费费率3%,百分数，针对交易资金
	private BigDecimal platform_service_fee_ratio = new BigDecimal(3.00);
	public BigDecimal getDefaultServiceFeeRatio() {
		return platform_service_fee_ratio;
	}
	
	//推广商家可获得分润利率3%，百分数，针对收取到的服务费的比例
	private BigDecimal default_partner_profit_ratio = new BigDecimal(3.00);
	public BigDecimal getDefaultPartnerProfitRatio() {
		return default_partner_profit_ratio;
	}
	
	//微信支付手续费费率
	@Value("${wxpay.fee-rate-use-wxpay}")
	private BigDecimal wxFeeRate;		
	public BigDecimal getWxFeeRate() {
		if(this.getSysParam("fee-rate-use-wxpay") != null) {
			try {
				BigDecimal newWXFeeRate = new BigDecimal(this.getSysParam("fee-rate-use-wxpay"));
				return newWXFeeRate;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return wxFeeRate;
	}
	
	//支付宝支付手续费费率
	@Value("${alipay.fee-rate-use-alipay}")
	private BigDecimal aliFeeRate;		
	public BigDecimal getAliFeeRate() {
		if(this.getSysParam("fee-rate-use-alipay") != null) {
			try {
				BigDecimal feeRate = new BigDecimal(this.getSysParam("fee-rate-use-alipay"));
				return feeRate;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return aliFeeRate;
	}
	
	//激活VIP需要积分数量
	@Value("${sys.vip_activate_need_score}")
	private Integer activateVipNeedScore;	
	public Integer getActivateVipNeedScore() {
		Integer activateVipNeedScore = this.activateVipNeedScore;
		try {
			if(getSysParam("vip_activate_need_score") != null) {
				activateVipNeedScore = new Integer(getSysParam("vip_activate_need_score"));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return activateVipNeedScore;
	}
	
	//
	//=============图片相关默认参数=======
	//
	//图片管理目录
	@Value("${sys.image-gallery-dir}")
	private String imageGalleryDir;
	public String getImageGalleryDir() {
		return imageGalleryDir;
	}
	
	//文件夹层级限制
	@Value("${sys.image-folder-level-limit}")
	private Integer imageFolderLevelLimit;
	public Integer getImageFolderLevelLimit() {
		return imageFolderLevelLimit;
	}
	
	//文件总数量限制
	@Value("${sys.image-file-all-limit}")
	private int imageFileAllLimit;
	public int getImageFileAllLimit() {
		if(this.getSysParam("image-file-all-limit") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("image-file-all-limit"));
				return newParam;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return imageFileAllLimit;
	}
	
	//单个文件夹可容纳文件限制
	@Value("${sys.image-folder-file-limit}")
	private int imageFolderFileLimit;
	public int getImageFolderFileLimit() {
		if(this.getSysParam("image-folder-file-limit") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("image-folder-file-limit"));
				return newParam;
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return imageFolderFileLimit;
	}
	
	//
	//=================订单相关默认参数==============
	//
	//可持有的所有待支付订单数量
	@Value("${sys.order-4pay-cnt-all-limit}")
	private int orderForPayCntAllLimit;
	public int getOrderForPayCntAllLimit() {
		if(this.getSysParam("order-4pay-cnt-all-limit") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("order-4pay-cnt-all-limit"));
				return newParam;
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return orderForPayCntAllLimit;
	}
	
	//单个商品可持有的带支付订单数量
	@Value("${sys.order-4pay-cnt-goods-limit}")
	private int orderForPayCntGoodsLimit;
	public int getOrderForPayCntGoodsLimit() {
		if(this.getSysParam("order-4pay-cnt-goods-limit") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("order-4pay-cnt-goods-limit"));
				return newParam;
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return orderForPayCntGoodsLimit;
	}
	
	//每次申请可延长收货天数
	@Value("${sys.order-sign-prolong-days}")
	private int orderSignProlongDays;
	public int getOrderSignProlongDays() {
		if(this.getSysParam("order-sign-prolong-days") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("order-sign-prolong-days"));
				return newParam;
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return orderSignProlongDays;
	}
		
	//超过多久未发货买家可申请退款：待发货状态
	@Value("${sys.order-nodelivery-days-4refund}")
	private int noDeliveryDates4Refund;
	public int getNoDeliveryDates4Refund() {
		if(this.getSysParam("order-nodelivery-days-4refund") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("order-nodelivery-days-4refund"));
				return newParam;
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return noDeliveryDates4Refund;
	}
	
	//超过多久未收到货买家可申请退款：已发货状态
	@Value("${sys.order-nosign-days-4refund}")
	private int noSignDates4Refund;
	public int getNoSignDates4Refund() {
		if(this.getSysParam("order-nosign-days-4refund") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("order-nosign-days-4refund"));
				return newParam;
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return noSignDates4Refund;
	}

	//用户的收藏数量限制
	@Value("${sys.user-collection-limit}")
	private int collectionLimit;
	public int getCollectionLimit() {
		try {
			Integer newParam = new Integer(this.getSysParam("user-collection-limit"));
			return newParam;
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return collectionLimit;
	}

	//会员用户的账户数量限制
	@Value("${sys.vip-account-cnt-limit}")
	private int vipAccountCntLimit;
	public int getVipAccountCntLimit() {
		try {
			Integer newParam = new Integer(this.getSysParam("vip-account-cnt-limit"));
			return newParam;
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return vipAccountCntLimit;
	}

	
	//会员开通合作伙伴需要的积分数量
	@Value("${sys.partner_open_need_socre}")
	private int partner_open_need_socre;
	public int getPartnerOpenNeedSocre() {
		try {
			Integer newParam = new Integer(this.getSysParam("partner_open_need_socre"));
			return newParam;
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return partner_open_need_socre;
	}

	//推广买家可获得所推广买家交易额的分润利率
	@Value("${sys.spread_user_profit_ratio}")
	private BigDecimal spread_user_profit_ratio;
	public BigDecimal getSpreadUserProfitRatio() {
		try {
			BigDecimal newParam = new BigDecimal(this.getSysParam("spread_user_profit_ratio"));
			return newParam;
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return spread_user_profit_ratio;
	}
	
	
	/**
	 * 获取系统配置参数
	 * @param paramName
	 * @return
	 */
	public String getSysParam(String paramName) {
		Long curr = System.currentTimeMillis()/1000/60/60; //小时
		if(LAST_UPD_TIME == null || curr-LAST_UPD_TIME>3) {
			List<SysParam> list = sysParamService.getAll();
			if(list != null) {
				SYS_PARAM_MAP = new HashMap<String,String>();
				LAST_UPD_TIME = curr;
				for(SysParam param:list) {
					SYS_PARAM_MAP.put(param.getParamName(), param.getParamValue());
				}
			}
		}
		if(SYS_PARAM_MAP != null) {
			if(SYS_PARAM_MAP.containsKey(paramName)) {
				return SYS_PARAM_MAP.get(paramName);
			}
		}
		return null;
	}

}
