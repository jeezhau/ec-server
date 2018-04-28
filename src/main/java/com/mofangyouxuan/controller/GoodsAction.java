package com.mofangyouxuan.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Category;
import com.mofangyouxuan.model.Goods;
import com.mofangyouxuan.model.PartnerBasic;
import com.mofangyouxuan.model.UserBasic;
import com.mofangyouxuan.service.GoodsService;
import com.mofangyouxuan.service.PartnerBasicService;
import com.mofangyouxuan.service.UserBasicService;

/**
 * 商品管理
 * 商品图片：使用合作伙伴的图片库的相对路径：partner_ID/xxx.jpg
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsAction {
	
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PartnerBasicService  partnerBasicService;
	@Autowired
	private UserBasicService userBasicService;
	
	/**
	 * 添加商品
	 * @param goods
	 * @param result
	 * @return {errcode:0,errmsg:'ok',goodsId:111}
	 */
	@RequestMapping("/add")
	public Object addGoods(@Valid Goods goods,BindingResult result) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				return jsonRet.toString();
			}
			
			//数据检查
			PartnerBasic partner = this.partnerBasicService.getByID(goods.getPartnerId());
			if(partner == null) {
				jsonRet.put("errmsg", "系统中该合作伙伴不存在！");
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				return jsonRet.toString();
			}
			if(!"0".equals(partner.getStatus()) && !"S".equals(partner.getStatus())  && !"C".equals(partner.getStatus()) && !"R".equals(partner.getStatus())) {
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				jsonRet.put("errmsg", "您当前的合作伙伴状态有误，不可进行商品管理！");
				return jsonRet.toString();
			}	
			
			goods.setReviewResult("0"); //待审核
			goods.setReviewLog("");
			goods.setReviewOpr(null);
			goods.setReviewTime(null);
			
			Long id = this.goodsService.add(goods);
			if(id == null) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("goodsId", id);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 修改商品基本信息
	 * @param goods
	 * @param result
	 * @return {errcode:0,errmsg:'ok',goodsId:111}
	 */
	@RequestMapping("/update")
	public Object updateGoods(@Valid Goods goods,BindingResult result) {
		JSONObject jsonRet = new JSONObject();
		try {
			//信息验证结果处理
			if(result.hasErrors()){
				StringBuilder sb = new StringBuilder();
				List<ObjectError> list = result.getAllErrors();
				for(ObjectError e :list){
					sb.append(e.getDefaultMessage());
				}
				jsonRet.put("errmsg", sb.toString());
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				return jsonRet.toString();
			}
			
			//数据检查
			PartnerBasic partner = this.partnerBasicService.getByID(goods.getPartnerId());
			if(partner == null) {
				jsonRet.put("errmsg", "系统中该合作伙伴不存在！");
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				return jsonRet.toString();
			}
			if(!"0".equals(partner.getStatus()) && !"S".equals(partner.getStatus())  && !"C".equals(partner.getStatus()) && !"R".equals(partner.getStatus())) {
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				jsonRet.put("errmsg", "您当前的合作伙伴状态有误，不可进行商品管理！");
				return jsonRet.toString();
			}	
			if(goods.getGoodsId() == null) {
				jsonRet.put("errmsg", "商品ID不可为空！");
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				return jsonRet.toString();
			}
			Goods old = this.goodsService.get(goods.getGoodsId());
			if(old == null) {
				jsonRet.put("errcode", ErrCodes.GOODS_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该商品信息！");
				return jsonRet.toString();
			}
			
			goods.setReviewResult("0"); //待审核
			goods.setReviewLog("");
			goods.setReviewOpr(null);
			goods.setReviewTime(null);
			
			int id = this.goodsService.update(goods);
			if(id <1 ) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("goodsId", goods.getGoodsId());
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	/**
	 * 根据ID获取商品信息
	 * @param goodsId
	 * @return {"errcode":-1,"errmsg":"错误信息"} 或 {商品所有字段}
	 */
	@RequestMapping("/get/{goodsId}")
	public Object getByID(@PathVariable("goodsId")Long goodsId) {
		JSONObject jsonRet = new JSONObject();
		try {
			Goods goods = this.goodsService.get(goodsId);
			if(goods == null) {
				jsonRet.put("errcode", ErrCodes.GOODS_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该商品信息！");
				return jsonRet.toString();
			}
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("goods", goods);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "系统异常，异常信息：" + e.getMessage());
			return jsonRet.toString();
		}
	}
	
	/**
	 * 获取所有商品
	 * @param jsonSearchParams 查询条件 {isSelf,reviewResult,status,partnerId,keywords,category,dispatchMode,isCityWide,distrIds}
	 * @param jsonSortParams  排序条件 {time:"N#0/1",dist:"N#0",sale:"N"#0/1}；time 表示按更新上架时间排序，N为排序位置，0为升序，1为降序；dist表示按距离排序，仅对有同城条件使用;sale 为按销量
	 * @param pageCond 分页条件 
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/getall")
	public Object getAll(String jsonSearchParams,String jsonSortParams,String jsonPageCond) {
		JSONObject jsonRet = new JSONObject();
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("reviewResult", "1");	//默认审核通过
			params.put("status", "1");	//默认已上架
			if(jsonSearchParams != null && jsonSearchParams.length()>0) {
				JSONObject jsonSearch = JSONObject.parseObject(jsonSearchParams);
				if(jsonSearch.containsKey("isSelf") && jsonSearch.getBooleanValue("isSelf")) {//合作伙伴自己
					params.put("reviewResult", null);
					params.put("status", null);
				}
				if(jsonSearch.containsKey("reviewResult")) {
					params.put("reviewResult", jsonSearch.getString("reviewResult"));
				}
				if(jsonSearch.containsKey("status")) {
					params.put("status", jsonSearch.getString("status"));
				}
				if(jsonSearch.containsKey("partnerId")) {
					params.put("partnerId", jsonSearch.getInteger("partnerId"));
				}
				if(jsonSearch.containsKey("keywords")) {//使用关键字查询
					params.put("goodsName", jsonSearch.getString("goodsName"));
					params.put("categoryName", jsonSearch.getString("categoryName"));
				}
				if(jsonSearch.containsKey("categoryId")) {
					params.put("categoryId", jsonSearch.getString("categoryId"));
				}
				if(jsonSearch.containsKey("dispatchMode")) {
					params.put("dispatchMode", jsonSearch.getString("dispatchMode"));
				}
				if(jsonSearch.containsKey("isCityWide")) {
					if(jsonSearch.getBooleanValue("isCityWide")) {
						params.put("isCityWide", "1");
					}else {
						params.put("isCityWide", "0");
					}
				}
				if(jsonSearch.containsKey("distrIds")) {
					params.put("distrIds", jsonSearch.getString("distrIds"));
				}
			}
			String strSorts = null;
			if(jsonSortParams != null && jsonSortParams.length()>0) {
				JSONObject jsonSort = JSONObject.parseObject(jsonSortParams);
				Map<Integer,String> sortMap = new HashMap<Integer,String>();
				if(jsonSort.containsKey("time")) {
					String value = jsonSort.getString("time");
					if(value != null && value.length()>0) {
						String[] arr = value.split("#");
						sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " update_time asc " : " update_time desc " );
					}
				}
				if(jsonSort.containsKey("dist")) {
					String value = jsonSort.getString("dist");
					if(value != null && value.length()>0) {
						String[] arr = value.split("#");
						sortMap.put(new Integer(arr[0]), " distance asc " );
					}
				}
				if(jsonSort.containsKey("sale")) {
					String value = jsonSort.getString("sale");
					if(value != null && value.length()>0) {
						String[] arr = value.split("#");
						sortMap.put(new Integer(arr[0]), ("0".equals(arr[1]))? " sale asc " : " sale desc " );
					}
				}
				
				Set<Integer> set = new TreeSet<Integer>(sortMap.keySet());
				StringBuilder sb = new StringBuilder();
				for(Integer key:set) {
					sb.append(",");
					sb.append(sortMap.get(key));
				}
				if(sb.length()>0) {
					strSorts = " order by " + sb.substring(1);
				}
			}
			PageCond pageCond = null;
			if(jsonPageCond == null) {
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
			int cnt = this.goodsService.countAll(params);
			pageCond.setCount(cnt);
			jsonRet.put("pageCond", pageCond);
			jsonRet.put("errcode", ErrCodes.GOODS_NO_GOODS);
			jsonRet.put("errmsg", "没有获取到商品信息！");
			if(cnt>0) {
				List<Goods> list = this.goodsService.getAll(params, strSorts, pageCond);
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
	 * 合作伙伴变更商品状态：1-上架、2-下架
	 * 
	 * @param goodsIds	待变更的商品ID列表
	 * @param partnerId	合作伙伴ID
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException 
	 */
	@RequestMapping("/changeStatus")
	public String changeOwnStatus(@RequestParam(value="goodsIds",required=true)String goodsIds,
			@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="newStatus",required=true)String newStatus) {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null) {
				jsonRet.put("errmsg", "系统中该合作伙伴不存在！");
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				return jsonRet.toString();
			}
			String[] arr = goodsIds.split(",");
			Set<Long> okSet = new HashSet<Long>();
			Set<String> errSet = new HashSet<String>();
			for(String idStr:arr) {
				try {
					Long id = new Long(idStr);
					okSet.add(id);
				}catch(Exception e) {
					errSet.add(idStr);
				}
			}
			List<Goods> list = new ArrayList<Goods>();
			for(Long id:okSet) {
				Goods g = this.goodsService.get(id);
				if(g != null && g.getPartnerId().equals(partner.getPartnerId())) {
					list.add(g);
				}else {
					errSet.add(id.toString());
				}
			}

			if(!"1".equals(newStatus) && !"2".equals(newStatus)) { //正常或关闭
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				jsonRet.put("errmsg", "状态取值不正确（1-上架，2-下架）！！");
				return jsonRet.toString();
			}
			if(list.size()>0) {
				this.goodsService.changeStatus(list, newStatus);
			}
			
			if(errSet.size() > 0) {
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				jsonRet.put("errmsg", "商品ID列表数据有误，具体数据：" + Arrays.toString(errSet.toArray()));
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//数据处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 商品审核
	 * 
	 * @param goodsId	待审批商品ID
	 * @param currUserId	审批人
	 * @param review 审批意见
	 * @param result 审批结果：S-通过，R-拒绝
	 * 
	 * @return {errcode:0,errmsg:"ok"}
	 * @throws JSONException
	 */
	@RequestMapping("/review")
	public String review(@RequestParam(value="goodsId",required=true)Long goodsId,
			@RequestParam(value="currUserId",required=true)Integer currUserId,
			@RequestParam(value="review",required=true)String review,
			@RequestParam(value="result",required=true)String result){
		JSONObject jsonRet = new JSONObject();
		try {
			UserBasic user = this.userBasicService.get(currUserId);
			if(user == null || user.getUserId()<100 || user.getUserId()>=1000) {
				jsonRet.put("errcode", ErrCodes.USER_NOT_REVIEW_ADMIN);
				jsonRet.put("errmsg", "该用户不是审核管理员！");
				return jsonRet.toString();
			}
			Goods old = this.goodsService.get(goodsId);
			if(old == null || !"0".equals(old.getReviewResult())) {
				jsonRet.put("errcode", ErrCodes.GOODS_STATUS_ERROR);
				jsonRet.put("errmsg", "该商品不存在或状态不正确！");
				return jsonRet.toString();
			}
			if(!"S".equals(result) && !"R".equals(result)) {
				jsonRet.put("errcode", ErrCodes.GOODS_PARAM_ERROR);
				jsonRet.put("errmsg", "审批结果取值不正确（S-通过，R-拒绝）！");
				return jsonRet.toString();
			}
			int cnt = this.goodsService.review(old, currUserId, result, review);
			if(cnt < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//异常处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	
	/**
	 * 修改商品库存
	 * @param partnerId
	 * @param goodsId
	 * @param newCnt
	 * @return
	 */
	@RequestMapping("/changeStock")
	public String changeStock(@RequestParam(value="partnerId",required=true)Integer partnerId,
			@RequestParam(value="goodsId",required=true)Long goodsId,
			@RequestParam(value="newCnt",required=true)Integer newCnt) {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic partner = this.partnerBasicService.getByID(partnerId);
			if(partner == null) {
				jsonRet.put("errmsg", "系统中该合作伙伴不存在！");
				jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
				return jsonRet.toString();
			}
			Goods goods = this.goodsService.get(goodsId);
			if(goods == null) {
				jsonRet.put("errcode", ErrCodes.GOODS_NO_EXISTS);
				jsonRet.put("errmsg", "系统中没有该商品信息！");
				return jsonRet.toString();
			}
			int cnt = this.goodsService.changeStock(goods, newCnt);
			if(cnt < 1) {
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "数据保存至数据库失败！");
			}else {
				jsonRet.put("errcode", 0);
				jsonRet.put("errmsg", "ok");
			}
		}catch(Exception e) {
			//异常处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
		
	}
	
	/**
	 * 获取所有的商品分类
	 * @return {errcode:0,errmsg:"",categories:[{},{},...]}
	 */
	@RequestMapping("/category")
	public Object getGoodsCategory() {
		JSONObject jsonRet = new JSONObject();
		try {
			List<Category> list = this.goodsService.getCategories();
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			jsonRet.put("categories", list);
			return jsonRet;
		}catch(Exception e) {
			//异常处理
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
}
