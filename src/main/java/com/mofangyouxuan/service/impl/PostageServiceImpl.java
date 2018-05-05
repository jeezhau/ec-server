package com.mofangyouxuan.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.mapper.GoodsMapper;
import com.mofangyouxuan.mapper.PostageMapper;
import com.mofangyouxuan.model.Postage;
import com.mofangyouxuan.service.PostageService;

@Service
public class PostageServiceImpl implements PostageService{
	@Value("${sys.postage-cnt-limit}")
	private int cntLimit;
	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private PostageMapper postageMapper;
	
	/**
	 * 新添加运费模版
	 * @param postage
	 * @return 新ID或错误码
	 */
	public Long add(Postage postage) {
		if(this.postageMapper.selectByPartnerAndName(postage.getPartnerId(), postage.getPostageName()) != null) {
			return (long)ErrCodes.POSTAGE_NAME_USED;
		}
		//数量检查
		int n = this.postageMapper.countByPartner(postage.getPartnerId());
		if(n >= this.cntLimit) {
			return (long)ErrCodes.POSTAGE_CNT_LIMIT;
		}
		postage.setPostageId(null);
		postage.setUpdateTime(new Date());
		postage.setStatus("1");
		int cnt = this.postageMapper.insert(postage);
		if(cnt>0) {
			return postage.getPostageId();
		}
		return (long)ErrCodes.COMMON_DB_ERROR;
	}
	
	/**
	 * 修改运费模版信息
	 * @param postage
	 * @return 更新记录数或小于0的错误码
	 */
	public int update(Postage postage) {
		Postage old = this.postageMapper.selectByPartnerAndName(postage.getPartnerId(), postage.getPostageName());
		if(old != null && !old.getPostageId().equals(postage.getPostageId())) {//已有同名
			return ErrCodes.POSTAGE_NAME_USED;
		}
		//数量检查
		int n = this.postageMapper.countByPartner(postage.getPartnerId());
		if(n >= this.cntLimit) {
			return ErrCodes.POSTAGE_CNT_LIMIT;
		}
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
	
	/**
	 * 根据ID列表获取
	 * @param idsList
	 * @return
	 */
	public List<Postage> getByIdList(String idList){
		return this.postageMapper.selectByIdList(idList);
	}
}
