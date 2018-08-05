package com.mofangyouxuan.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.common.SysParamUtil;
import com.mofangyouxuan.model.Goods;
import com.mofangyouxuan.model.GoodsSpec;
import com.mofangyouxuan.model.Order;
import com.mofangyouxuan.model.OrderBal;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.model.PayFlow;
import com.mofangyouxuan.model.Postage;
import com.mofangyouxuan.model.Receiver;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.model.VipBasic;
import com.mofangyouxuan.pay.WXPay;
import com.mofangyouxuan.service.GoodsService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.PartnerStaffService;
import com.mofangyouxuan.service.PostageService;
import com.mofangyouxuan.service.ReceiverService;
import com.mofangyouxuan.service.UserBasicService;
import com.mofangyouxuan.service.VipBasicService;
import com.mofangyouxuan.utils.NonceStrUtil;
import com.mofangyouxuan.utils.SignUtils;

/**
 * 点单管理
 * 1、每个用户有待付款订单数量限制；
 * 2、没付款之前订单可主动取消或修改；
 * 3、买家在付款后未发货之前可联系卖家取消并同时主动取消；
 * 4、卖家在未发货前可联系买家取消并同时主动取消；
 * 5、买家付款之后更新库存；取消完成恢复库存；
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	private WXPay wXPay;
	
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private VipBasicService vipBasicService;
	@Autowired
	private PartnerBasicService partnerBasicService;
	@Autowired
	private PartnerStaffService partnerStaffService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private ReceiverService receiverService;
	@Autowired
	private PostageService postageService;
	@Autowired
	private OrderService orderService;

	@Autowired
	private SysParamUtil sysParamUtil;
	
	/**
	 * 创建订单
	 * @param userId
	 * @param receiver
	 * @param result
	 * @return {errcode:0,errmsg:"ok",orderId:111}
	 */
	@RequestMapping("/{userId}/create")
	public Object create(@Valid Order order,BindingResult result,
			@PathVariable("userId")Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(userId);
			if(user == null || !"1".equals(user.getStatus())) {
				jsonRet.put("errcode", ErrCodes.USER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该用户！");
				return jsonRet.toString();
			}
			if(!order.getUserId().equals(userId)) {
				jsonRet.put("errmsg", "用户信息不一致！");
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				return jsonRet.toString();
			}
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.RECEIVER_PARAM_ERROR);
				return jsonRet.toString();
			}
			//用户数据检查
//			if(user.getPhone() == null || user.getPhone().length()<6) {
//				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
//				jsonRet.put("errmsg", "您的联系电话还未补充，请先到个人信息中补充提交，该电话将方便商家与您取得联系！");
//				return jsonRet.toString();
//			}
			//商品、配送信息检查
			Goods goods = this.goodsService.get(true, order.getGoodsId(),false);
			if(goods == null || !"S".equals(goods.getPartner().getStatus()) || 
					!"1".equals(goods.getStatus()) || !Goods.REWSTAT.normal.getValue().equals(goods.getReviewResult())) {
				jsonRet.put("errcode", ErrCodes.GOODS_STATUS_ERROR);
				jsonRet.put("errmsg", "该商品当前不支持下单购买！");
				return jsonRet.toString();
			}
			if(user.getUserId().equals(goods.getPartner().getVipId())) {
				jsonRet.put("errcode", ErrCodes.GOODS_STATUS_ERROR);
				jsonRet.put("errmsg", "您不可以购买自己的商品！");
				return jsonRet.toString();
			}
			Receiver receiver = this.receiverService.getById(order.getRecvId());
			if(receiver == null) {
				jsonRet.put("errcode", ErrCodes.RECEIVER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该收货人信息！");
				return jsonRet.toString();
			}
			Postage postage = this.postageService.get(order.getPostageId());
			if(postage == null) {
				jsonRet.put("errcode", ErrCodes.POSTAGE_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该运费模板信息！");
				return jsonRet.toString();
			}else {
				//同城配送
				if("1".equals(postage.getIsCityWide())) {
					Integer postage_distLimit = postage.getDistLimit(); //同城配送距离限制
					if(postage_distLimit == null) {
						postage_distLimit = 0;
					}
					Integer distance = this.getDistance(goods.getPartner(), receiver);
					//可配送检查
					if(!(distance != null && (0 == postage_distLimit || postage_distLimit <= distance))) {//可送
						jsonRet.put("errcode", ErrCodes.ORDER_ERROR_POSTAGE);
						jsonRet.put("errmsg", "该配送模式不支持该收件地区配送！");
						return jsonRet.toString();
					}
				}else {//全国配送
					String postage_provLimit = postage.getProvLimit(); //全国配送省份限制
					if(postage_provLimit == null) {
						postage_provLimit = "全国";
					}
					//省份检查
					if(!("全国".equals(postage_provLimit.trim()) || postage_provLimit.contains(receiver.getProvince()))){//可送
						jsonRet.put("errcode", ErrCodes.ORDER_ERROR_POSTAGE);
						jsonRet.put("errmsg", "该配送模式不支持该收件地区配送！");
						return jsonRet.toString();
					}
				}
			}
			List<GoodsSpec> applySpec = JSONArray.parseArray(order.getGoodsSpec(), GoodsSpec.class);
			if(applySpec == null || applySpec.size()<1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "您的购买信息不可为空！");
				return jsonRet.toString();
			}
			Map<String,Object> buyStatistics = new HashMap<String,Object>();
//			Double amount = 0.0;	//购买金额
//			Integer count = 0;	//购买数量
//			Integer weight = 0;	//购买毛重量
//			buyStatistics.put("amount", amount);
//			buyStatistics.put("count", count);
//			buyStatistics.put("weight", weight);
			//订单数据检查
			String errStr = this.checkOrderData(goods, applySpec, userId, buyStatistics);
			if(errStr != null) {
				return errStr;
			}
			if(applySpec.size()<1 || (Integer)buyStatistics.get("count") < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "您的购买信息不可为空！");
				return jsonRet.toString();
			}
			//计算运费
			Double carrage = this.getCarrage(postage, this.getDistance(goods.getPartner(), receiver),
					(Integer)buyStatistics.get("weight"), (Double)buyStatistics.get("amount"));
			//数据处理
			Double amount = (Double)buyStatistics.get("amount");
			amount += carrage;
			String am = new DecimalFormat("#0.00").format(amount); 
			order.setAmount(new BigDecimal(am));
			order.setCarrage(new BigDecimal(carrage));
			order.setGoodsSpec(JSONArray.toJSONString(applySpec));
			String id = this.orderService.add(order);
			
			if(!id.startsWith("-") ) {
				jsonRet.put("orderId", id);
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", id);
				jsonRet.put("errmsg", "数据保存出现错误！错误码：" + id);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toJSONString();
	}

	/**
	 * 获取是否可配送客户所选收货地区
	 * 1、如果不可配送则不可下单，并给出提示；
	 * 2、如果可以配送，则给出所有的可选模式以及对应的费用；
	 * @param recvId		收货信息ID
	 * @param goodsId	商品ID
	 * @param goodsSpec		商品数量
	 * @return {"errcode":0,"errmsg":"ok",match:[{postageId,mode,carrage}...]}
	 */
	@RequestMapping("/{userId}/checkData")
	public String checkDataAndMatch(@PathVariable("userId")Integer userId,
			@RequestParam(value="recvId",required=true) Long recvId,
			@RequestParam(value="goodsId",required=true) Long goodsId,
			@RequestParam(value="goodsSpec",required=true) String goodsSpec) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = userBasicService.get(userId);
			Goods goods = goodsService.get(true, goodsId,false);
			Receiver receiver = receiverService.getById(recvId);
			String postageIds = goods.getPostageIds();
			List<Postage> postages = postageService.getByIdList(postageIds);
			List<GoodsSpec> applySpec = JSONArray.parseArray(goodsSpec, GoodsSpec.class);
			List<GoodsSpec> sysSpec = goods.getSpecDetail();
			if(user == null || goods == null || receiver == null || postages == null || applySpec.size() ==0 || sysSpec.size() ==0) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "获取数据失败！");
				return jsonRet.toString();
			}
			Map<String,Object> buyStatistics = new HashMap<String,Object>();
			Double amount = 0.0;	//购买金额
			Integer count = 0;	//购买数量
			Integer weight = 0;	//购买毛重量
			buyStatistics.put("amount", amount);
			buyStatistics.put("count", count);
			buyStatistics.put("weight", weight);
			//用户数据检查
//			if(user.getPhone() == null || user.getPhone().length()<6) {
//				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
//				jsonRet.put("errmsg", "您的联系电话还未补充，请先到个人信息中补充提交，该电话将方便商家与您取得联系！");
//				return jsonRet.toString();
//			}
			if(goods == null || !"S".equals(goods.getPartner().getStatus()) || 
					!"1".equals(goods.getStatus()) || 
					!user.getUserId().equals(goods.getPartner().getVipId()) && !Goods.REWSTAT.normal.getValue().equals(goods.getReviewResult())
					) {
				jsonRet.put("errcode", ErrCodes.GOODS_STATUS_ERROR);
				jsonRet.put("errmsg", "该商品当前不支持下单购买！");
				return jsonRet.toString();
			}
//			if(user.getUserId().equals(goods.getPartner().getVipId())) {
//				jsonRet.put("errcode", ErrCodes.GOODS_STATUS_ERROR);
//				jsonRet.put("errmsg", "您不可以购买自己的商品！");
//				return jsonRet.toString();
//			}
			//订单数据检查
			String errStr = this.checkOrderData(goods, applySpec, userId, buyStatistics);
			if(errStr != null) {
				return errStr;
			}
			if(applySpec.size()<1 || (Integer)buyStatistics.get("count") < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_PARAM_ERROR);
				jsonRet.put("errmsg", "您的购买信息不可为空！");
				return jsonRet.toString();
			}
			//配送方案获取
			JSONArray matchArray = this.getDispatchMatch(goods.getPartner(),goods, receiver, postages, buyStatistics);
			if(matchArray.size()>0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("match", matchArray);
			}else {
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "该商品不支持该收件地区的配送！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getLocalizedMessage());
		}
		return jsonRet.toJSONString();
	}
	
	/**
	 * 订单数据检查 
	 * @param goods
	 * @param applySpec
	 * @param userId
	 * @param buyStatistics	购买信息统计：总价(amount)，总重(weight)，总数量(counts)
	 * @return 错误信息，null表示没有错误
	 */
	private String checkOrderData(Goods goods,List<GoodsSpec> applySpec,Integer userId,Map<String,Object> buyStatistics) {
		List<GoodsSpec> sysSpec = goods.getSpecDetail();

		JSONObject jsonRet = new JSONObject();
		
		//库存检查
		String overStockErr = this.checStock(applySpec, sysSpec, buyStatistics);
		if(overStockErr != null) {
			jsonRet.put("errcode", ErrCodes.ORDER_STOCK_OVER);
			jsonRet.put("errmsg", overStockErr);
			return jsonRet.toString();
		}
		//已有待付款检查
		jsonRet = this.checkForPayCnt(goods, userId);
		if(jsonRet != null) {
			return jsonRet.toJSONString();
		}
		jsonRet = new JSONObject();
		
		//限购检查
		if(!this.buyLimitCheck(goods, userId, (Integer)buyStatistics.get("count"))) {
			jsonRet.put("errcode", ErrCodes.ORDER_BUY_LIMIT);
			jsonRet.put("errmsg", "该商品当前限购，限购时间：" + goods.getBeginTime() + " 至 " + goods.getEndTime() 
				+ "，限购数量：" + goods.getLimitedNum() + "!");
			return jsonRet.toJSONString();
		}
		return null;
	}
	
	/**
	 * 库存检查，使用系统中的规格信息补充提交的购买规格信息，并统计购买信息
	 * @param applySpec	用户的提交的购买信息
	 * @param sysSpec	系统中商品的规格信息
	 * @param buyStatistics	购买信息统计：总价(amount)，总重(weight)，总数量(counts)
	 * @return 检查结果信息，null表示没有错误
	 */
	private String checStock(List<GoodsSpec> applySpec,List<GoodsSpec> sysSpec,Map<String,Object> buyStatistics) {
		StringBuilder sb = new StringBuilder();
		Integer count = 0;
		BigDecimal amount = new BigDecimal(0);
		Integer weight = 0;
		for(int i=0;i<applySpec.size();i++) {
			GoodsSpec spec = applySpec.get(i);
			Integer buyNum = spec.getBuyNum();
			if(buyNum != null && buyNum > 0) {
				boolean flag = false;	//是否有该规格
				for(GoodsSpec p : sysSpec) {
					if(p.getName().equals(spec.getName())) {
						flag = true;
						count += buyNum;
						amount = amount.add(new BigDecimal(buyNum * p.getPrice().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP));
						weight += buyNum * p.getGrossWeight();
						if(buyNum > p.getStock()) {
							sb.append(" " + p.getName() + ": 最大库存 " + p.getStock() + " 件，您购买了 " + buyNum + " 件，超过了最大库存！");
						}
						spec.setGrossWeight(p.getGrossWeight());
						spec.setPrice(p.getPrice());
						spec.setStock(null);
						spec.setUnit(p.getUnit());
						spec.setVal(p.getVal());
					}
				}
				if(flag == false) {
					sb.append("系统中没有该规格名称的商品，规格名：" + spec.getName() + "！");
				}
			}else {
				applySpec.remove(i);
				i--;
			}
		}
		buyStatistics.put("count", count);
		buyStatistics.put("amount", amount.doubleValue());
		buyStatistics.put("weight", weight);
		if(sb.length()>0) {
			return sb.toString();
		}else {
			return null;
		}
	}
	
	/**
	 * 计算同城配送距离
	 * @param partner
	 * @param receiver
	 * @return 配送距离,null表示非同城 
	 */
	private Integer getDistance(PartnerBasic partner,Receiver receiver) {
		String receiver_city = receiver.getCity(); //收货城市
		String partner_city = partner.getCity();	//商品所在城市
		
		Integer distance = null;	//同城计算距离
		if(receiver_city.equals(partner_city)) {//同城须计算距离，单位：km
			BigDecimal lon1 = partner.getLocationX(); //商户经度
			BigDecimal lat1 = partner.getLocationY();  //商户纬度
			BigDecimal lon2 = receiver.getLocationX(); //收货人经度
			BigDecimal lat2 = receiver.getLocationY(); //收货人纬度
			if(lon1 != null && lat1 != null && lon2 != null && lat2 != null) {
//				double hsinX = Math.sin((lon1.doubleValue() - lon2.doubleValue()) * 0.5);
//		        double hsinY = Math.sin((lat1.doubleValue() - lat2.doubleValue()) * 0.5);
//		        double h = hsinY * hsinY +
//		                (Math.cos(lat1.doubleValue()) * Math.cos(lat2.doubleValue()) * hsinX * hsinX);
//		        distance = (int)(2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h)) * 6367);
//		        
		        double dx = lon1.doubleValue() - lon2.doubleValue(); // 经度差值
		        double dy = lat1.doubleValue() - lat2.doubleValue(); // 纬度差值
		        double b = (lat1.doubleValue() + lat2.doubleValue()) / 2.0; // 平均纬度
		        double Lx = (dx*Math.PI/180) * 6367 * Math.cos(b*Math.PI/180); // 东西距离
		        double Ly = 6367 * (dy*Math.PI/180); // 南北距离
		        distance = (int)Math.sqrt(Lx * Lx + Ly * Ly);  // 用平面的矩形对角距离公式计算总距离
			}else {
				distance = 30;
			}
		}
		return distance;
	}
	
	/**
	 * 获取可配送方案及对应的配送费
	 * @param goods
	 * @param receiver
	 * @param postages
	 * @param weight
	 * @param amount
	 * @return
	 */
	private JSONArray getDispatchMatch(PartnerBasic partner,Goods goods,Receiver receiver,List<Postage> postages,Map<String,Object> buyStatistics) {
		JSONArray matchArray = new JSONArray();
		
		for(Postage postage:postages) {
			Double carrage = null;
			//同城配送
			if("1".equals(postage.getIsCityWide())) {
				Integer postage_distLimit = postage.getDistLimit(); //同城配送距离限制
				if(postage_distLimit == null) {
					postage_distLimit = 0;
				}
				Integer distance = this.getDistance(partner, receiver);
				//距离检查
				if(distance != null && (0 == postage_distLimit || postage_distLimit <= distance)) {//可送
					//费送计算
					carrage = getCarrage(postage,distance,(Integer)buyStatistics.get("weight"),(Double)buyStatistics.get("amount"));
				}else {//不可送
					continue;
				}
			}else {//全国配送
				String postage_provLimit = postage.getProvLimit(); //全国配送省份限制
				if(postage_provLimit == null) {
					postage_provLimit = "全国";
				}
				//省份检查
				if("全国".equals(postage_provLimit.trim()) || postage_provLimit.contains(receiver.getProvince())){//可送
					carrage = getCarrage(postage,null,(Integer)buyStatistics.get("weight"),(Double)buyStatistics.get("amount"));	
				}else {//不可送
					continue;
				}
			}
			
			String[] modes = postage.getDispatchMode().split("");
			for(String mode:modes) {
				JSONObject match = new JSONObject();
				if(mode.trim().length()>0) {
					match.put("postageId", postage.getPostageId());
					match.put("mode", mode);
					match.put("carrage", new DecimalFormat("#0.00").format(carrage));
					matchArray.add(match);
				}
			}
		}
		return matchArray;
	}
	
	/**
	 * 待付款数量检查
	 * @param goods
	 * @param userId
	 * @return null:检查通过，其他：错误信息
	 */
	private JSONObject checkForPayCnt(Goods goods,Integer userId) {
		//待付款数量检查
		JSONObject jsonRet = null;
		String foPayStatus = "10";
		JSONObject jsonParams = new JSONObject();
		jsonParams.put("status", foPayStatus);
		jsonParams.put("userId", userId);
		int cntAll = this.orderService.countAll(jsonParams);
		if(cntAll>=sysParamUtil.getOrderForPayCntAllLimit()) {
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.ORDER_FOR_PAY_CNT_ALL_LIMIT);
			jsonRet.put("errmsg", "您已有多个订单还未支付，请先完成才可再次下单！");
			return jsonRet;
		}
		jsonParams.put("goodsId", goods.getGoodsId());
		int cntPer =  this.orderService.countAll(jsonParams);
		if(cntPer >= sysParamUtil.getOrderForPayCntAllLimit()) {
			jsonRet = new JSONObject();
			jsonRet.put("errcode", ErrCodes.ORDER_FOR_PAY_CNT_GOODS_LIMIT);
			jsonRet.put("errmsg", "您已有该商品订单还未支付，请先完成才可再次下单！");
			return jsonRet;
		}
		return jsonRet;
	}
	/**
	 * 商品限购检查
	 * @param goods
	 * @param currBuyNum
	 * 
	 * @return true:可购买，false：数量超限
	 */
	private boolean buyLimitCheck(Goods goods,Integer userId,int currBuyNum) {
		//限购检查
		if(goods.getLimitedNum() > 0) {
			JSONObject jsonParams = new JSONObject();
			jsonParams.put("userId", userId);
			jsonParams.put("goodsId", goods.getGoodsId());
			if(currBuyNum > goods.getLimitedNum()) {
				return false;
			}
			String curr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			if(goods.getBeginTime().compareTo(curr)<=0 && curr.compareTo(goods.getEndTime())<=0) {
				jsonParams.put("beginCreateTime", goods.getBeginTime());
				jsonParams.put("endCreateTime", goods.getEndTime());
				String status = "10,11,20,21,30,31,,40,41,50,51,52,53,54,55,56";
				jsonParams.put("status", status);
				List<Order> list = this.orderService.getAll(null,jsonParams, null, new PageCond(0,10000));
				if(list != null && list.size()>0) {
					int num = 0;
					for(Order o:list) {
						JSONArray arr = JSONArray.parseArray(o.getGoodsSpec());
						for(int i=0;i<arr.size();i++) {
							num += arr.getJSONObject(i).getIntValue("buyNum");
						}
					}
					if(num + currBuyNum > goods.getLimitedNum() )
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 运费计算
	 * @param postage
	 * @param distance
	 * @param weight
	 * @param amount
	 * @return
	 */
	private double getCarrage(Postage postage,Integer distance,Integer weight,Double amount) {
		double carrage = 0.0;
		Integer freeWeight = postage.getFreeWeight() == null ? 1 : postage.getFreeWeight();
		Integer freeDist = postage.getFreeDist() == null ? 1 : postage.getFreeDist();
		Double freeAmount = postage.getFreeAmount() == null ? 100 : postage.getFreeAmount().doubleValue();
		if("1".equals(postage.getIsFree().trim())) {//无条件免邮
			return carrage;
		}else if("2".equals(postage.getIsFree().trim())) {//重量免邮
			if(weight <= freeWeight) {
				return carrage;
			}
		}else if("3".equals(postage.getIsFree().trim())) {//金额免邮
			if(amount >= freeAmount) {
				return carrage;
			}
		}else if("4".equals(postage.getIsFree().trim())) {//距离免邮
			if("1".equals(postage.getIsCityWide()) && distance <= freeDist) {
				return carrage;
			}
		}else if("23".equals(postage.getIsFree().trim())) {//重量+金额免邮
			if(weight <= freeWeight && amount >= freeAmount) {
				return carrage;
			}
		}else if("24".equals(postage.getIsFree().trim())) {//重量+距离免邮
			if(weight <= freeWeight && distance != null && distance <= freeDist) {
				return carrage;
			}
		}else if("34".equals(postage.getIsFree().trim())) {//金额+距离免邮
			if(amount >= freeAmount && distance != null && distance <= freeDist) {
				return carrage;
			}
		}else if("234".equals(postage.getIsFree().trim())) {//重量+金额+距离免邮
			if(weight <= freeWeight && amount >= freeAmount && distance != null && distance <= freeDist) {
				return carrage;
			}
		}
		
		Integer firstWeight = postage.getFirstWeight() == null ? 1 : postage.getFirstWeight();
		double firstWPrice = postage.getFirstWPrice() == null ? 10.0 : postage.getFirstWPrice().doubleValue();
		Integer additionWeight = postage.getAdditionWeight() == null ? 1 : postage.getAdditionWeight();
		double additonWPrice = postage.getAdditionWPrice() == null ? 5.0 : postage.getAdditionWPrice().doubleValue();
		if(firstWeight < 1) {
			firstWeight = 1;		//默认首重1kg
		}
		if(firstWPrice < 0) {
			firstWPrice = 10.0;;	//默认首重10元
		}
		if(additionWeight<1) {
			additionWeight = 1; //默认续重1kg
		}
		if(additonWPrice < 0) {
			additonWPrice = 5.0; //默认续重5元
		}
		carrage += firstWPrice;
		double cnt_w = Math.ceil((weight - firstWeight)/(additionWeight*1.0));
		if(cnt_w>0) {
			carrage += cnt_w * additonWPrice;
		}
		if("1".equals(postage.getIsCityWide()) && distance != null && distance > 0) {
			Integer firstDist = postage.getFirstDist() == null ? 1 : postage.getFirstDist();
			double firstDPrice = postage.getFirstDPrice() == null ? 3.0 : postage.getFirstDPrice().doubleValue();
			Integer additionDist = postage.getAdditionDist() == null ? 1 : postage.getAdditionDist();
			double additonDPrice = postage.getAdditionDPrice() == null ? 1.0 : postage.getAdditionDPrice().doubleValue();
			carrage += firstDPrice;
			double cnt_d = Math.ceil((distance - firstDist)/(additionDist*1.0));
			if(cnt_d>0) {
				carrage += cnt_d * additonDPrice;
			}
		}
		return carrage;
	}
	
	/**
	 * 根据ID获取订单信息
	 * @param orderId
	 * @return {errcode:0,errmsg:"ok",order:{...},orderBal:{...}}
	 */
	@RequestMapping("/get/{orderId}")
	public Object getOrderByID(@PathVariable("orderId")String orderId) {
		JSONObject jsonRet = new JSONObject();
		try {
			
			Order order = this.orderService.get(orderId);
			if(order == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_NO_EXISTS);
				jsonRet.put("errmsg", "该订单系统中不存在！");
			}else {
				OrderBal orderBal = this.orderService.getOBal(orderId);
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("order", order);
				jsonRet.put("orderBalStat", (orderBal != null?orderBal.getStatus():""));
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	/**
	 * 查询指定查询条件、排序条件、分页条件的订单信息；
	 * 商品ID与合作伙伴ID不可都为空
	 * @param jsonShowGroups		显示字段分组:{needReceiver,needLogistics,needGoodsAndUser}
	 * @param jsonSearchParams	查询条件:{userId,goodsId, partnerId,upPartnerId,status,keywords,categoryId:,dispatchMode,postageId:,appraiseStatus,appr4User}
	 * @param jsonSortParams		排序条件:{createTime:"N#0/1",sendTime:"N#0/1",signTime:"N#0/1"}
	 * @param jsonPageCond		分页信息:{begin:, pageSize:}
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/getall")
	public Object searchOrders(@RequestParam(value="jsonShowGroups",required=true)String jsonShowGroups,
			@RequestParam(value="jsonSearchParams",required=true)String jsonSearchParams,
			String jsonSortParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
			if(!jsonSearch.containsKey("goodsId") && !jsonSearch.containsKey("partnerId") && 
					!jsonSearch.containsKey("userId") && !jsonSearch.containsKey("upPartnerId")) {
				jsonRet.put("errcode", ErrCodes.ORDER_SEARCH_PARAM);
				jsonRet.put("errmsg", "下单用户ID、商品ID和合作伙伴ID不可都为空！");
				return jsonRet.toString();
			}
			
			JSONObject jsonShow = JSONObject.parseObject(jsonShowGroups);
			
			JSONObject jsonSorts = new JSONObject();
			if(jsonSortParams != null && jsonSortParams.length()>0) {
				jsonSorts = JSONObject.parseObject(jsonSortParams);
			}
			PageCond pageCond = null;
			if(jsonPageCond == null || jsonPageCond.length()<1) {
				pageCond = new PageCond(0,100);
			}else {
				pageCond = JSONObject.toJavaObject(JSONObject.parseObject(jsonPageCond), PageCond.class);
				if(pageCond == null) {
					pageCond = new PageCond(0,100);
				}
				if( pageCond.getBegin()<=0) {
					pageCond.setBegin(0);
				}
				if(pageCond.getPageSize()<2) {
					pageCond.setPageSize(100);
				}
			}

			int cnt = this.orderService.countAll(jsonSearch);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", ErrCodes.GOODS_NO_GOODS);
			jsonRet.put("errmsg", "没有获取到订单信息！");
			if(cnt>0) {
				List<Order> list = this.orderService.getAll(jsonShow,jsonSearch, jsonSorts, pageCond);
				if(list != null && list.size()>0) {
					jsonRet.put("datas", list);
					jsonRet.put("errcode", 0);
					jsonRet.put("errmsg", "ok");
				}
			}
			return jsonRet.toString();
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}

	/**
	 * 查询指定查询条件、排序条件、分页条件的订单信息；
	 * 用户ID 或 合作伙伴ID不可都为空
	 * @param userId		
	 * @param partnerId
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/count/partibystatus")
	public Object countPartibyStatus(Integer userId,Long goodsId,Integer partnerId) {
		JSONObject jsonRet = new JSONObject();
		try {
			List<Map<String,Integer>> ret = this.orderService.countPartibyStatus(partnerId, goodsId, userId);
			jsonRet.put("datas", ret);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 获取订单的最新支付流水
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @param type	支付类型：1-消费，2-退款，空则不论
	 * @return {errcode,errmsg,payflow:{}}
	 */
	@RequestMapping("/{userId}/payflow/{orderId}")
	public Object getPayFlow(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId,
			String type) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(userId);
			VipBasic userVip = this.vipBasicService.get(userId);
			if(user == null || userVip == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(type == null || type.trim().length() == 0) {
				type = null;
			}
			PayFlow payFlow = this.orderService.getLastedFlow(orderId, type);
			if(payFlow == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该订单的支付流水！");
			}else if(!user.getUserId().equals(payFlow.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限查询该数据！");
				return jsonRet.toJSONString();
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
				jsonRet.put("payFlow", payFlow);
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 买家支付完成
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @return {errcode,errmsg,payType,payTime,amount,fee}
	 */
	@RequestMapping("/{userId}/payfinish/{orderId}")
	public Object payFinish(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic userVip = this.vipBasicService.get(userId);
			Order order = this.orderService.get(orderId);
			if(userVip == null || order == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!userVip.getVipId().equals(order.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			jsonRet = this.orderService.payFinish(userVip, order);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 查询物流
	 * @param orderId	订单ID
	 * @return {errcode,errmsg,order:{...}}
	 */
	@RequestMapping("/logistics/{orderId}")
	public Object getLogistics(@PathVariable(value="orderId",required=true)String orderId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Order order = this.orderService.get(orderId);
			if(order == null ) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!order.getStatus().startsWith("3") && !order.getStatus().startsWith("4") &&
					!order.getStatus().startsWith("5") && !order.getStatus().startsWith("6")) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "该订单当前不可查询物流！");
				return jsonRet.toJSONString();
			}
			jsonRet.put("order", order);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 创建支付订单
	 * @param orderId	订单ID
	 * @param payType	支付方式
	 * @param userId		用户ID
	 * @param userIp		用户IP
	 * @param passwd		会员操作密码
	 * @return {errcode,errmsg,payType,outPayUrl,prepay_id,AliPayForm}
	 */
	@RequestMapping("/{userId}/createpay/{orderId}")
	public Object createPrePay(@PathVariable(value="orderId",required=true)String orderId,
			@RequestParam(value="payType",required=true)String payType,
			@PathVariable(value="userId",required=true)Integer userId,
			@RequestParam(value="userIp",required=true)String userIp,
			String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(userId);
			VipBasic userVip = this.vipBasicService.getVipBal(userId);
			Order order = this.orderService.get(orderId);
			Goods goods = this.goodsService.get(true, order.getGoodsId(),false);
			if(user == null || userVip == null || order == null || goods == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!user.getUserId().equals(order.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			if(!"1".equals(payType) && 
					!"21".equals(payType) && !"22".equals(payType) && !"23".equals(payType) &&
					!"31".equals(payType) && !"32".equals(payType)) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "支付方式取值不正确！");
				return jsonRet.toJSONString();
			}
			//库存检查
			Map<String,Object> buyStatistics = new HashMap<String,Object>();
			Double amount = 0.0;	//购买金额
			Integer count = 0;	//购买数量
			Integer weight = 0;	//购买毛重量
			buyStatistics.put("amount", amount);
			buyStatistics.put("count", count);
			buyStatistics.put("weight", weight);
			List<GoodsSpec> applySpec = JSONObject.parseArray(order.getGoodsSpec(), GoodsSpec.class);
			List<GoodsSpec> sysSpec = goods.getSpecDetail();
			String errmsg = this.checStock(applySpec, sysSpec, buyStatistics);
			if(errmsg != null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", errmsg);
				return jsonRet.toJSONString();
			}
			//errcode,errmsg,payType,prepay_id,outPayUrl,total,AliPayForm}
			jsonRet = this.orderService.createPrePay(user, userVip, order, goods.getPartner().getVipId(), payType, userIp);
			if(jsonRet.containsKey("prepay_id") && "21".equals(payType)) { //公众号支付
				Long timestamp = System.currentTimeMillis()/1000;
				String nonceStr = NonceStrUtil.getNonceStr(20);
				Map<String,String> signMap = new HashMap<String,String>();
				signMap.put("appId", this.wXPay.appId);
				signMap.put("timeStamp", timestamp+"");
				signMap.put("nonceStr", nonceStr);
				signMap.put("package", "prepay_id=" + jsonRet.getString("prepay_id"));
				signMap.put("signType", "MD5");
				String sign = this.wXPay.signMap(signMap);
				jsonRet.put("paySign", sign);
				jsonRet.put("appId", this.wXPay.appId);
				jsonRet.put("timeStamp", timestamp);
				jsonRet.put("nonceStr", nonceStr);
			}
			jsonRet.put("payType", payType);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 提交余额支付
	 * 1、须进行会员密码密码验证
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @param passwd		会员操作密码
	 * @return {errcode,errmsg}
	 */
	@RequestMapping(value="/{userId}/balpay/submit/{orderId}",method=RequestMethod.POST)
	public Object submitBalPay(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic userVip = this.vipBasicService.getVipBal(userId);
			Order order = this.orderService.get(orderId);
			Goods goods = this.goodsService.get(true, order.getGoodsId(),false);
			PayFlow payFlow = this.orderService.getLastedFlow(orderId, "1");
			if(userVip == null || payFlow == null || order == null || goods == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			
			if(!userVip.getVipId().equals(payFlow.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			if(!"00".equals(payFlow.getStatus()) ) {//非待支付
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行支付！");
				return jsonRet;
			}
			//密码验证
			if(userVip.getPasswd() == null || userVip.getPasswd().length()<10) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您还未设置资金操作密码，请先到会员中心完成设置！");
				return jsonRet.toJSONString();
			}
			if(!SignUtils.encodeSHA256Hex(passwd).equals(userVip.getPasswd())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您的资金操作密码输入不正确！");
				return jsonRet.toJSONString();
			}
			if(userVip.getBalance() < payFlow.getPayAmount()) {//判断余额是否够支付
				jsonRet.put("errcode", -1);
				jsonRet.put("errmsg", "余额支付失败，余额不足！");
				return jsonRet;
			}
			this.orderService.execPaySucc(true,payFlow, userVip.getVipId(),order, goods.getPartner().getVipId(),"");
			payFlow = this.orderService.getLastedFlow(orderId, "1");
			this.orderService.balanceBill(false, payFlow);
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 买家取消订单
	 * 1、会员需要输入密码才可取消；
	 * 2、如果未付款，则直接取消；如果预付款，则申请原路退款；
	 * 3、保存退款与取消缘由至售后信息；
	 * 4、余额支付则直接退款，并解冻商家，修改库存；
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @param reason		取消理由
	 * @param passwd		会员操作密码，会员需要密码
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{userId}/cancel/{orderId}")
	public Object cancelOrder(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId,
			@RequestParam(value="reason",required=true)String reason,
			String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			VipBasic userVip = this.vipBasicService.get(userId);
			Order order = this.orderService.get(orderId);
			PartnerBasic partner = this.partnerBasicService.getByID(order.getPartnerId());
			if(userVip == null || order == null || partner == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!userVip.getVipId().equals(order.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			//密码检查
			if("1".equals(userVip.getStatus())) {
				if(passwd == null || passwd.length()<6) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您的会员密码不可为空！");
					return jsonRet.toJSONString();
				}
				//密码验证
				if(userVip.getPasswd() == null || userVip.getPasswd().length()<10) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您还未设置会员操作密码，请先到会员中心完成设置！");
					return jsonRet.toJSONString();
				}
				if(!SignUtils.encodeSHA256Hex(passwd).equals(userVip.getPasswd())) {
					jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "您的会员操作密码输入不正确！");
					return jsonRet.toJSONString();
				}
			}
			jsonRet = this.orderService.cancelOrder(order, userVip.getVipId(), partner.getVipId(),reason);
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 买家申请延长签收
	 * 1、申请延长收货期限，最多可延长三次，每次5天（可配置）；
	 * @param orderId	订单ID
	 * @param userId		用户ID
	 * @param passwd		会员操作密码，会员需要密码
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{userId}/prolong/{orderId}")
	public Object signProlong(@PathVariable(value="orderId",required=true)String orderId,
			@PathVariable(value="userId",required=true)Integer userId) {
		JSONObject jsonRet = new JSONObject();
		try {
			
			VipBasic userVip = this.vipBasicService.get(userId);
			Order order = this.orderService.get(orderId);
			PartnerBasic partner = this.partnerBasicService.getByID(order.getPartnerId());
			if(userVip == null || order == null || partner == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!userVip.getVipId().equals(order.getUserId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			
			
			//订单状态检查
			if(!"22".equals(order.getStatus()) && !"30".equals(order.getStatus()) && !"55".equals(order.getStatus()) ) {
				jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
				jsonRet.put("errmsg", "您当前不可执行收货延长申请操作！");
				return jsonRet.toJSONString();
			}
			
			Order updOdr = new Order();
			updOdr.setOrderId(order.getOrderId());
			int days = this.sysParamUtil.getOrderSignProlongDays();
			if(order.getSignProlong() == null || order.getSignProlong()<days) {
				updOdr.setSignProlong(days);
			}else {
				if(order.getSignProlong() >= days*3) {
					jsonRet.put("errcode", ErrCodes.ORDER_STATUS_ERROR);
					jsonRet.put("errmsg", "您当前不可再次执行收货延长申请操作，已达最大限制次数！");
					return jsonRet.toJSONString();
				}else {
					updOdr.setSignProlong(order.getSignProlong() + days);
				}
			}
			int cnt = this.orderService.update(updOdr);
			if(cnt >0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库保存数据出错！");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
		
	}
	
	

	/**
	 * 卖家备货与取消备货
	 * @param orderId	订单ID
	 * @param partnerId	合作伙伴ID
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{partnerId}/ready/{orderId}")
	public Object readyGoods(@PathVariable(value="partnerId",required=true)Integer partnerId,
			@PathVariable(value="orderId",required=true)String orderId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Order order = this.orderService.get(orderId);
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(order == null || partner == null) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "参数有误，系统中没有指定数据！");
				return jsonRet.toJSONString();
			}
			if(!order.getPartnerId().equals(partner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			if(!"20".equals(order.getStatus()) && !"21".equals(order.getStatus())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行备货管理！");
				return jsonRet.toJSONString();
			}
			Order nO = new Order();
			nO.setOrderId(order.getOrderId());
			nO.setStatus(order.getStatus().equals("20")?"21":"20");
			int cnt = this.orderService.update(nO);
			if(cnt >0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库保存数据出错！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	/**
	 * 卖家发货
	 * @param orderId	订单ID
	 * @param partnerId	合作伙伴ID
	 * @param logisticsComp	快递公司名称
	 * @param logisticsNo	快递单号
	 * @return {errcode,errmsg}
	 */
	@RequestMapping("/{partnerId}/delivery/{orderId}")
	public Object deliveryGoods(@PathVariable(value="partnerId",required=true)Integer partnerId,
			@PathVariable(value="orderId",required=true)String orderId,
			@RequestParam(value="logisticsComp",required=true)String logisticsComp,
			@RequestParam(value="logisticsNo",required=true)String logisticsNo,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="passwd",required=true)String passwd) {
		JSONObject jsonRet = new JSONObject();
		try {
			//数据检查
			PartnerBasic myPartner = this.partnerBasicService.getByID(partnerId);
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
				jsonRet.put("errcode", ErrCodes.PARTNER_PARAM_ERROR);
				jsonRet.put("errmsg", "系统中没有该合作伙伴的信息！");
				return jsonRet.toString();
			}
			VipBasic vip = this.vipBasicService.get(currUserId);
			//操作员与密码验证
			Integer updateOpr = null;
			Boolean isPass = false;
			String signPwd = SignUtils.encodeSHA256Hex(passwd);
			if(vip != null && myPartner.getUpdateOpr().equals(vip.getVipId())) { //绑定会员
				if(signPwd.equals(vip.getPasswd())) { //会员密码验证
					isPass = true;
					updateOpr = vip.getVipId();
				}
			}
			if(isPass != true ) {
				PartnerStaff operator = this.partnerStaffService.get(partnerId, currUserId); //员工&& operator != null) {
				if(operator != null && operator.getTagList() != null && operator.getTagList().contains("saleorder") && signPwd.equals(operator.getPasswd())) { //员工密码验证
					isPass = true;
					updateOpr = operator.getUserId();
				}
			}
			if(!isPass) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您无权对该合作伙伴进行管理(或密码不正确)！");
				return jsonRet.toString();
			}
			
			Order order = this.orderService.get(orderId);
			if(!order.getPartnerId().equals(myPartner.getPartnerId())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您没有权限处理该订单！");
				return jsonRet.toJSONString();
			}
			if(!"20".equals(order.getStatus()) && !"21".equals(order.getStatus())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行发货管理！");
				return jsonRet.toJSONString();
			}
			//数据处理保存
			Order nO = new Order();
			nO.setOrderId(order.getOrderId());
			nO.setStatus("30");	//待收货
			nO.setSendOpr(updateOpr);
			nO.setSendTime(new Date()); //设置发货时间
			nO.setLogisticsComp(logisticsComp);
			nO.setLogisticsNo(logisticsNo);
			int cnt = this.orderService.update(nO);
			if(cnt >0) {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}else {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据库保存数据出错！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}

	
}


