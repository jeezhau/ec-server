package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.mapper.GoodsMapper;
import com.mofangyouxuan.mapper.PostageMapper;
import com.mofangyouxuan.model.Postage;
import com.mofangyouxuan.service.PostageService;

@Service
public class PostageServiceImpl implements PostageService{
	
	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private PostageMapper postageMapper;
	
	/**
	 * 新添加运费模版
	 * @param postage
	 * @return 新ID或null
	 */
	public Long add(Postage postage) {
		postage.setUpdateTime(new Date());
		postage.setStatus("1");
		int cnt = this.postageMapper.insert(postage);
		if(cnt>0) {
			return postage.getPostageId();
		}
		return null;
	}
	
	/**
	 * 修改运费模版信息
	 * @param postage
	 * @return 更新记录数或小于0的错误码
	 */
	public int update(Postage postage) {
		
		postage.setUpdateTime(new Date());
		postage.setStatus("1");
		int cnt = this.postageMapper.updateByPrimaryKey(postage);
		return cnt;
	}
	
	/**
	 * 删除运费模版
	 * 正在被商户使用的模版不可删除
	 * @param postage
	 * @return 删除记录数或小于0的错误码
	 */
	public int delete(Postage postage) {
		int useCnt = this.goodsMapper.countUsePostageCnt(postage.getPostageId());
		if(useCnt>0) {
			return ErrCodes.POSTAGE_USING_NOW;
		}
		int cnt = this.postageMapper.deleteByPrimaryKey(postage.getPostageId());
		return cnt;
	}
	
	/**
	 * 根据ID获取模版信息
	 * @param postageId
	 * @return
	 */
	public Postage get(Long postageId) {
		return this.postageMapper.selectByPrimaryKey(postageId);
	}
	
	/**
	 * 获取指定合作伙伴的所有模版信息
	 * @param partnerId
	 * @return
	 */
	public List<Postage> getByPartnerId(Integer partnerId){
		return this.postageMapper.selectByPartner(partnerId);
	}
	
	/**
	 * 获取模版正被使用的次数
	 * @param postageId
	 * @return
	 */
	public int getUsingCnt(Long postageId) {
		return this.goodsMapper.countUsePostageCnt(postageId);
	}
}
