package com.mofangyouxuan.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Category;
import com.mofangyouxuan.model.Goods;

/**
 * 商品服务接口
 * @author jeekhan
 *
 */
public interface GoodsService {
	
	/**
	 * 根据ID获取指定商品信息
	 * @param id
	 * @return
	 */
	public Goods get(Long id);
	
	/**
	 * 添加商品信息
	 * @param goods
	 * @return
	 */
	public Long add(Goods goods);
	
	/**
	 * 更新商品信息
	 * @param goods
	 * @return
	 */
	public int update(Goods goods);
	
	/**
	 * 删除商品记录
	 * @param goods
	 * @return
	 */
	public int delete(Goods goods);
	
	/**
	 * 变更商品规格与库存
	 * @param goods
	 * @param newCnt 新的库存数量
	 * @return
	 */
	public int changeSpec(Goods goods,String specDetail,Integer stockSum,BigDecimal priceLowest);
	
	/**
	 * 记录商品审批结果
	 * @param goods
	 * @param oprid
	 * @param result 审批结果：1-审核通过，2-审核拒绝
	 * @param review
	 * @return
	 */
	public int review(Goods goods,Integer oprid,String result,String review);
	
	/**
	 * 变更商品状态:1-上架、2-下架
	 * @param list
	 * @param newStatus
	 * @return
	 */
	public void changeStatus(List<Goods> list,String newStatus) ;
	
	/**
	 * 根据指定查询和排序条件分页获取商品信息
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<Goods> getAll(Map<String,Object> params,String sorts,PageCond pageCond);

	/**
	 * 根据指定查询获取商品信息数量
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params);
	
	/**
	 * 获取所有的商品分类数据
	 * @return
	 */
	public List<Category> getCategories();
	
}
