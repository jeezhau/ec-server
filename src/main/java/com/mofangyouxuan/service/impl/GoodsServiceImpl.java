package com.mofangyouxuan.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.GoodsMapper;
import com.mofangyouxuan.mapper.GoodsSpecMapper;
import com.mofangyouxuan.model.Goods;
import com.mofangyouxuan.model.GoodsSpec;
import com.mofangyouxuan.service.GoodsService;
import com.mofangyouxuan.service.ImageGalleryService;
import com.mofangyouxuan.utils.CommonUtil;


@Service
@Transactional
public class GoodsServiceImpl implements GoodsService{
	
	@Value("${sys.goods-cnt-limit}")
	private int goodsCntLimit; //合作伙伴的商户数量限制
	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private GoodsSpecMapper goodsSpecMapper;
	@Autowired
	private ImageGalleryService imageGalleryService;
	
	/**
	 * 根据ID获取指定商品信息
	 * @param needPartner 是否包含合作伙伴信息
	 * @param id
	 * @param isSelf 是否时合作伙伴自己
	 * @return
	 */
	@Override
	public Goods get(Boolean needPartner,Long goodsId,Boolean isSelf){
		Goods goods = this.goodsMapper.selectByPrimaryKey(needPartner,goodsId,isSelf);
		return goods;
	}
	
	/**
	 * 添加商品信息
	 * @param goods
	 * @return 新ID或错误码
	 */
	@Override
	public Long add(Goods goods) {
		Date currTime = new Date();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partnerId", goods.getPartnerId());
		int n = this.goodsMapper.countAll(params);
		if(n>=this.goodsCntLimit) {
			return (long)ErrCodes.GOODS_CNT_LIMIT;
		}
		goods.setGoodsId(null);
		goods.setUpdateTime(currTime);
		//goods.setReviewLog(null);
		//goods.setReviewResult("1");
		int cnt = this.goodsMapper.insert(goods);
		if(cnt>0) {
			Long goodsId = goods.getGoodsId();
			List<GoodsSpec> specList = goods.getSpecDetail();
			for(GoodsSpec spec:specList) {
				spec.setGoodsId(goodsId);
				spec.setUpdateTime(currTime);
				spec.setUpdateOpr(goods.getUpdateOpr());
				this.goodsSpecMapper.insert(spec);
			}
			//更新图片使用次数
			this.updateImgUsingCnt(null, goods);
			return goodsId;
		}
		return (long)ErrCodes.COMMON_DB_ERROR;
	}
	
	/**
	 * 更新商品信息
	 * @param goods
	 * @return 更新的记录数
	 */
	@Override
	public int update(Goods old,Goods goods) {
		Date currTime = new Date();
		
		goods.setUpdateTime(new Date());
		//goods.setReviewLog(null);
		//goods.setReviewResult("1");
		//重置规格库存
		this.goodsSpecMapper.deleteAll(goods.getGoodsId());
		List<GoodsSpec> specList = goods.getSpecDetail();
		for(GoodsSpec spec:specList) {
			spec.setGoodsId(goods.getGoodsId());
			spec.setUpdateTime(currTime);
			spec.setUpdateOpr(goods.getUpdateOpr());
			this.goodsSpecMapper.insert(spec);
		}
		int cnt = this.goodsMapper.updateByPrimaryKey(goods);
		//更新图片使用次数
		this.updateImgUsingCnt(old, goods);
		return cnt;
	}
	
	/**
	 * 删除商品
	 * @param goods
	 * @return 删除记录数
	 */
	public int delete(Goods goods) {
		Long id = goods.getGoodsId();
		int cnt = this.goodsMapper.deleteByPrimaryKey(id);
		return cnt;
	}
	
	/**
	 * 变更商品规格与库存()
	 * @param goodsId	商品ID
	 * @param specDetail	变更的规格信息
	 * @param updType	变更方式：1-重置规格库存(不包含名称)，2-增加库存，3-减少库存
	 * @param updStockSum	需要变更的库存
	 * @param updPriceLowest	需要变更的最低价（全覆盖时有用）
	 * @return 更新记录数
	 */
	@Override
	public int changeSP(Long goodsId,List<GoodsSpec> applySpec,int updType,Integer updStockSum, BigDecimal updPriceLowest,Integer updateOpr){
		Goods goods = this.get(false, goodsId,true);
		if(goods == null) {
			return ErrCodes.GOODS_NO_GOODS;
		}
		Date currTime = new Date();
		Goods updG = new Goods();
		updG.setGoodsId(goodsId);
		updG.setStockSum(goods.getStockSum());
		if(updType == 1) {//重置规格库存(不包含名称)
			List<GoodsSpec> sysSpec = this.goodsSpecMapper.selectAll(goodsId);
			this.goodsSpecMapper.deleteAll(goodsId);
			for(GoodsSpec spec:applySpec) {
				for(GoodsSpec sgs:sysSpec) {
					if(spec.getName().equals(sgs.getName())) {
						this.goodsSpecMapper.deleteSpec(goodsId, spec.getName());
						spec.setGoodsId(goods.getGoodsId());
						spec.setUpdateTime(currTime);
						spec.setUpdateOpr(goods.getUpdateOpr());
						this.goodsSpecMapper.insert(spec);
					}
				}
			}
			updG.setStockSum(updStockSum);
			updG.setPriceLowest(updPriceLowest);
			updG.setUpdateTime(new Date());
		}else if(updType == 2) {//增加库存
			int cnt = 0;
			for(GoodsSpec p : applySpec) { //变更数据
				this.goodsSpecMapper.updateStock(goodsId, p.getName(), p.getBuyNum());
				updG.setStockSum(updG.getStockSum() + p.getBuyNum());
				cnt += p.getBuyNum();
			}
			cnt = goods.getSaledCnt()-cnt;
			if(cnt < 0) {
				cnt = 0;
			}
			updG.setSaledCnt(cnt);
		}else {//减少
			int cnt = 0; //总减少库存
			for(GoodsSpec p : applySpec) {
				this.goodsSpecMapper.updateStock(goodsId, p.getName(), -p.getBuyNum()); //减少库存
				updG.setStockSum(updG.getStockSum() - p.getBuyNum());
				cnt += p.getBuyNum();
			}
			cnt = goods.getSaledCnt()+cnt;
			updG.setSaledCnt(cnt);
		}
		
		if(updG.getStockSum()<=0) {
			updG.setStatus("2"); //下架
		}
		updG.setUpdateOpr(updateOpr);
		updG.setUpdateTime(new Date());
		int cnt = this.goodsMapper.updateByPrimaryKey(updG);
		return cnt;
	}
	
	/**
	 * 记录商品审批结果
	 * @param goods
	 * @param rewPartnerId
	 * @param oprId
	 * @param result
	 * @param review
	 * @return 更新记录数
	 */
	@Override
	public int review(Goods goods,Integer rewPartnerId,Integer oprId,String result,String review) {
		Goods g = new Goods();
		g.setGoodsId(goods.getGoodsId());
		JSONArray rewLog = JSONArray.parseArray(goods.getReviewLog());
		if(rewLog == null) {
			rewLog = new JSONArray();
		}
		JSONObject jobj = new JSONObject();
		jobj.put("partnerId", rewPartnerId);
		jobj.put("operator", oprId);
		jobj.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
		jobj.put("result", result);
		jobj.put("content", review);
		rewLog.add(0, jobj);
		g.setReviewLog(rewLog.toJSONString());
		g.setReviewResult(result);
		g.setReviewTime(new Date());
		int cnt = this.goodsMapper.updateByPrimaryKey(g);
		return cnt;
	}
	
	/**
	 * 变更商品状态:上架、下架
	 * @param list
	 * @param newStatus
	 * @return 更新记录数
	 */
	@Override
	public void changeStatus(List<Goods> list,String newStatus,Integer updateOpr) {
		for(Goods goods:list) {
			Long id = goods.getGoodsId();
			Goods g = new Goods();
			g.setGoodsId(id);
			g.setStatus(newStatus);
			g.setUpdateOpr(updateOpr);
			g.setUpdateTime(new Date());
			this.goodsMapper.updateByPrimaryKey(g);
		}
	}
	
	/**
	 * 根据指定查询和排序条件分页获取商品信息
	 * @param needPartner 是否包含合作伙伴信息
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	@Override
	public List<Goods> getAll(boolean needPartner,Map<String,Object> params,String sorts,PageCond pageCond){
		if(params == null) {
			params = new HashMap<String,Object>();
		}
		if(needPartner) { //需要合作伙伴信息
			params.put("needPartner", true);
			return this.goodsMapper.selectAll(params, sorts, pageCond);
		}
		return this.goodsMapper.selectAll(params, sorts, pageCond);
	}

	/**
	 * 根据指定查询获取商品信息数量
	 * @param params
	 * @return
	 */
	@Override
	public int countAll(Map<String,Object> params) {
		return this.goodsMapper.countAll(params);
	}
	
	private Map<String,Integer> selImgFromGoodsDesc(Integer partnerId,String goodsDesc){
		if(goodsDesc == null || goodsDesc.length()<1) {
			return null;
		}
		List<String> list = CommonUtil.getImgStr(goodsDesc);
		if(list == null || list.size()<1) {
			return null;
		}
		Map<String,Integer> imgIdMap = new HashMap<String,Integer>();
		String prefix = "/shop/gimage/" + partnerId + "/";
		for(String str:list) {
			str = str.trim();
			if(str.startsWith(prefix)) {
				String imgId = str.replace(prefix, "");
				imgIdMap.put(imgId, imgIdMap.get(imgId)==null?1:imgIdMap.get(imgId)+1);
			}
		}
		return imgIdMap;
	}
	
	private void updateImgUsingCnt(Goods oldG,Goods newG) {
		Map<String,Integer> oldImgMap = new HashMap<String,Integer>();
		if(oldG != null) {
			oldImgMap = this.selImgFromGoodsDesc(oldG.getPartnerId(), oldG.getGoodsDesc());
			if(oldImgMap == null) {
				oldImgMap = new HashMap<String,Integer>();
			}
			if(oldG.getMainImgPath() != null && oldG.getMainImgPath().length()>0) {
				Integer cnt = oldImgMap.get(oldG.getMainImgPath().trim());
				if(cnt == null) {
					cnt = 1;
				}else {
					cnt += 1;
				}
				oldImgMap.put(oldG.getMainImgPath().trim(),cnt);
			}
			if(oldG.getCarouselImgPaths() != null && oldG.getCarouselImgPaths().length()>0) {
				String[] arr = oldG.getCarouselImgPaths().split(",");
				if(arr != null && arr.length > 0) {
					for(String imgId:arr) {
						Integer cnt = oldImgMap.get(imgId.trim());
						if(cnt == null) {
							cnt = 1;
						}else {
							cnt += 1;
						}
						oldImgMap.put(imgId.trim(),cnt);
					}
				}
			}
		}
		
		Map<String,Integer> newImgMap = this.selImgFromGoodsDesc(newG.getPartnerId(), newG.getGoodsDesc());
		if(newImgMap == null) {
			newImgMap = new HashMap<String,Integer>();
		}
		if(newG.getMainImgPath() != null && newG.getMainImgPath().length()>0) {
			Integer cnt = newImgMap.get(newG.getMainImgPath().trim());
			if(cnt == null) {
				cnt = 1;
			}else {
				cnt += 1;
			}
			newImgMap.put(newG.getMainImgPath().trim(),cnt);
		}
		if(newG.getCarouselImgPaths() != null && newG.getCarouselImgPaths().length()>0) {
			String[] arr = newG.getCarouselImgPaths().split(",");
			if(arr != null && arr.length > 0) {
				for(String imgId:arr) {
					Integer cnt = newImgMap.get(imgId.trim());
					if(cnt == null) {
						cnt = 1;
					}else {
						cnt += 1;
					}
					newImgMap.put(imgId.trim(),cnt);
				}
			}
		}
		//新添加的图片
		Set<String> addSet = new HashSet<String>(newImgMap.keySet());//新增
		addSet.removeAll(oldImgMap.keySet());
		Set<String> delSet = new HashSet<String>(oldImgMap.keySet());//删除
		delSet.removeAll(newImgMap.keySet());
		Set<String> samSet = new HashSet<String>(newImgMap.keySet());//保留
		samSet.removeAll(addSet);
		samSet.removeAll(delSet);
		for(String imgId:addSet) {
			this.imageGalleryService.updUsingCnt(newG.getPartnerId(), imgId,newImgMap.get(imgId));
		}
		for(String imgId:delSet) {
			this.imageGalleryService.updUsingCnt(oldG.getPartnerId(), imgId,-oldImgMap.get(imgId));
		}
		for(String imgId:samSet) {
			this.imageGalleryService.updUsingCnt(oldG.getPartnerId(), imgId,newImgMap.get(imgId)-oldImgMap.get(imgId));
		}
	}
	
}
