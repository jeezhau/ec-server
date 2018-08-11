package com.mofangyouxuan.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.Goods;
import com.mofangyouxuan.model.GoodsSpec;

/**
 * 商品服务接口
 * @author jeekhan
 *
 */
public interface GoodsService {
	
	/**
	 * 根据ID获取指定商品信息
	 * @param needPartner 是否包含合作伙伴信息
	 * @param id
	 * @param isSelf 是否时合作伙伴自己
	 * 
	 * @return
	 */
	public Goods get(Boolean needPartner,Long id,Boolean isSelf);
	
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
	public int update(Goods old,Goods goods);
	
	/**
	 * 删除商品记录
	 * @param goods
	 * @return
	 */
	public int delete(Goods goods);
	
	/**
	 * 变更商品规格与库存(不可变更名称)
	 * 
	 * @param goodsId	商品ID
	 * @param applySpec	变更的规格信息
	 * @param updType	变更方式：1-覆盖，2-增加，3-减少
	 * @param updStockSum	需要变更的库存
	 * @param updPriceLowest	需要变更的最低价（全覆盖时有用）
	 * @return
	 */
	public int changeSP(Long goodsId,List<GoodsSpec> applySpec,int updType,Integer updStockSum, BigDecimal updPriceLowest,Integer updateOpr);
	
	/**
	 * 记录商品审批结果
	 * @param goods
	 * @param rewPartnerId
	 * @param oprId
	 * @param result 审批结果：1-审核通过，2-审核拒绝
	 * @param review
	 * @return
	 */
	public int review(Goods goods,Integer rewPartnerId,Integer oprid,String result,String review);
	
	/**
	 * 变更商品状态:1-上架、2-下架
	 * @param list
	 * @param newStatus
	 * @return
	 */
	public void changeStatus(List<Goods> list,String newStatus,Integer upateOpr) ;
	
	/**
	 * 根据指定查询和排序条件分页获取商品信息
	 * @param needPartner 是否包含合作伙伴信息
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<Goods> getAll(boolean needPartner,Map<String,Object> params,String sorts,PageCond pageCond);

	/**
	 * 根据指定查询获取商品信息数量
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params);
	
	
}
