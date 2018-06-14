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
	
	//微信手续费费率
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
	
	//微信手续费费率
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
		if(this.getSysParam("image-folder-level-limit") != null) {
			try {
				Integer newParam = new Integer(this.getSysParam("image-folder-level-limit"));
				return newParam;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
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
				e.printStackTrace();
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
				e.printStackTrace();
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
				e.printStackTrace();
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
				e.printStackTrace();
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
				e.printStackTrace();
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
				e.printStackTrace();
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
			e.printStackTrace();
		}
		return collectionLimit;
	}

	//会员用户的每类通道的账户数量限制
	@Value("${sys.vip-account-cnt-limit}")
	private int vipAccountCntLimit;
	public int getVipAccountCntLimit() {
		try {
			Integer newParam = new Integer(this.getSysParam("vip-account-cnt-limit"));
			return newParam;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return vipAccountCntLimit;
	}

	
	//会员用户的每类通道的账户数量限制
	@Value("${sys.partner_open_need_socre}")
	private int partner_open_need_socre;
	public int getPartnerOpenNeedSocre() {
		try {
			Integer newParam = new Integer(this.getSysParam("partner_open_need_socre"));
			return newParam;
		}catch(Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return spread_user_profit_ratio;
	}
	
	//推广商家可获得所推广商家交易额的分润利率
	@Value("${sys.spread_user_profit_ratio}")
	private BigDecimal spread_mcht_profit_ratio;
	public BigDecimal getSpreadMchtProfitRatio() {
		try {
			BigDecimal newParam = new BigDecimal(this.getSysParam("spread_mcht_profit_ratio"));
			return newParam;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return spread_mcht_profit_ratio;
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
