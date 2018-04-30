package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.GoodsMapper;
import com.mofangyouxuan.model.Category;
import com.mofangyouxuan.model.Goods;
import com.mofangyouxuan.service.GoodsService;

@Service
public class GoodsServiceImpl implements GoodsService{
	
	@Value("${sys.goods-cnt-limit}")
	private int goodsCntLimit;
	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private com.mofangyouxuan.mapper.CategoryMapper categoryMapper;
	
	/**
	 * 根据ID获取指定商品信息
	 * @param id
	 * @return
	 */
	@Override
	public Goods get(Long id){
		return this.goodsMapper.selectByPrimaryKey(id);
	}
	
	/**
	 * 添加商品信息
	 * @param goods
	 * @return 新ID或错误码
	 */
	@Override
	public Long add(Goods goods) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partnerId", goods.getPartnerId());
		int n = this.goodsMapper.countAll(params);
		if(n>=this.goodsCntLimit) {
			return (long)ErrCodes.GOODS_CNT_LIMIT;
		}
		goods.setGoodsId(null);
		goods.setUpdateTime(new Date());
		goods.setReviewLog(null);
		goods.setReviewOpr(null);
		goods.setReviewResult("0");
		int cnt = this.goodsMapper.insert(goods);
		if(cnt>0) {
			return goods.getGoodsId();
		}
		return (long)ErrCodes.COMMON_DB_ERROR;
	}
	
	/**
	 * 更新商品信息
	 * @param goods
	 * @return 更新的记录数
	 */
	@Override
	public int update(Goods goods) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partnerId", goods.getPartnerId());
		int n = this.goodsMapper.countAll(params);
		if(n>=this.goodsCntLimit) {
			return ErrCodes.GOODS_CNT_LIMIT;
		}
		goods.setUpdateTime(new Date());
		goods.setReviewLog(null);
		goods.setReviewOpr(null);
		goods.setReviewResult("0");
		int cnt = this.goodsMapper.updateByPrimaryKey(goods);
		return cnt;
	}
	
	/**
	 * 删除商品记录
	 * @param goods
	 * @return 删除记录数
	 */
	public int delete(Goods goods) {
		Long id = goods.getGoodsId();
		int cnt = this.goodsMapper.deleteByPrimaryKey(id);
		return cnt;
	}
	
	/**
	 * 变更商品库存
	 * @param goods
	 * @param changeCount 可为正负
	 * @return 更新记录数
	 */
	@Override
	public int changeStock(Goods goods,int newCnt) {
		Long id = goods.getGoodsId();
		Goods g = new Goods();
		g.setGoodsId(id);
		g.setStock(newCnt);
		if(newCnt<=0) {
			g.setStatus("2"); //下架
		}
		int cnt = this.goodsMapper.updateByPrimaryKey(g);
		return cnt;
	}
	
	/**
	 * 记录商品审批结果
	 * @param goods
	 * @param result
	 * @param review
	 * @return 更新记录数
	 */
	@Override
	public int review(Goods goods,Integer oprid,String result,String review) {
		Long id = goods.getGoodsId();
		Goods g = new Goods();
		g.setGoodsId(id);
		g.setReviewLog(review);
		g.setReviewOpr(oprid);
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
	public void changeStatus(List<Goods> list,String newStatus) {
		for(Goods goods:list) {
			Long id = goods.getGoodsId();
			Goods g = new Goods();
			g.setGoodsId(id);
			g.setStatus(newStatus);
			g.setUpdateTime(new Date());
			this.goodsMapper.updateByPrimaryKey(g);
		}
	}
	
	/**
	 * 根据指定查询和排序条件分页获取商品信息
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	@Override
	public List<Goods> getAll(Map<String,Object> params,String sorts,PageCond pageCond){
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

	/**
	 * 获取所有的商品分类数据
	 * @return
	 */
	public List<Category> getCategories(){
		return this.categoryMapper.selectAll();
	}
	
}
