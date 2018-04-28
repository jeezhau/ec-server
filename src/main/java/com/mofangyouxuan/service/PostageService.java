package com.mofangyouxuan.service;

import java.util.List;

import com.mofangyouxuan.model.Postage;

public interface PostageService {
	
	/**
	 * 新添加运费模版
	 * @param postage
	 * @return 新ID或null
	 */
	public Long add(Postage postage);
	
	/**
	 * 修改运费模版信息
	 * @param postage
	 * @return
	 */
	public int update(Postage postage);
	
	/**
	 * 删除运费模版
	 * 正在被商户使用的模版不可删除
	 * @param postage
	 * @return 删除记录数或小于0的错误码
	 */
	public int delete(Postage postage);
	
	/**
	 * 根据ID获取模版信息
	 * @param postageId
	 * @return
	 */
	public Postage get(Long postageId);
	
	/**
	 * 获取指定合作伙伴的所有模版信息
	 * @param partnerId
	 * @return
	 */
	public List<Postage> getByPartnerId(Integer partnerId);

	/**
	 * 获取模版正被使用的次数
	 * @param postageId
	 * @return
	 */
	public int getUsingCnt(Long postageId);
	
}
