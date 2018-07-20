package com.mofangyouxuan.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.mapper.ReceiverMapper;
import com.mofangyouxuan.model.Receiver;
import com.mofangyouxuan.service.ReceiverService;

@Service
@Transactional
public class ReceiverServiceImpl implements ReceiverService{
	
	@Value("${sys.receiver-cnt-limit}")
	private int receiverCntLimit;
	
	@Autowired
	private ReceiverMapper receiverMapper;
	
	/**
	 * 获取指定ID的收货人信息
	 * @param recvId
	 * @return
	 */
	public Receiver getById(Long recvId) {
		return this.receiverMapper.selectByPrimaryKey(recvId);
	}
	
	/**
	 * 添加收货人信息
	 * @param receiver
	 * @return
	 */
	public Long add(Receiver receiver) {
		receiver.setRecvId(null);
		int n = this.receiverMapper.countAllByUser(receiver.getUserId(),receiver.getRecvType());
		if(n >= this.receiverCntLimit) {
			return (long)ErrCodes.RECEIVER_CNT_LIMIT;
		}
		if(n==0) {//收条信息默认
			receiver.setIsDefault("1");
		}
		if("1".equals(receiver.getIsDefault())){
			this.receiverMapper.unDefault(receiver.getUserId(),receiver.getRecvType());//取消已有默认
		}
		int cnt = this.receiverMapper.insert(receiver);
		if(cnt>0) {
			return receiver.getRecvId();
		}
		return (long)ErrCodes.COMMON_DB_ERROR;
	}
	
	/**
	 * 更新收货人信息
	 * @param receiver
	 * @return
	 */
	public int update(Receiver receiver) {
		int n = this.receiverMapper.countAllByUser(receiver.getUserId(),receiver.getRecvType());
		if(n > 1 && "1".equals(receiver.getIsDefault())){
			this.receiverMapper.unDefault(receiver.getUserId(),receiver.getRecvType());//取消已有默认
		}
		if(n <= 1) {
			receiver.setIsDefault("1");
		}
		int cnt = this.receiverMapper.updateByPrimaryKeySelective(receiver);
		return cnt;
	}
	
	/**
	 * 删除收货人信息
	 * @param receiver
	 * @return
	 */
	public int delete(Receiver receiver) {
		int cnt = this.receiverMapper.deleteByPrimaryKey(receiver.getRecvId());
		return cnt;
	}
	
	/**
	 * 设置为默认
	 * @param receiver
	 * @return
	 */
	public int setDefault(Receiver receiver) {
		this.receiverMapper.unDefault(receiver.getUserId(),receiver.getRecvType());
		int cnt = this.receiverMapper.setDefault(receiver.getRecvId());
		return cnt;
	}

	/**
	 * 获取默认收货人信息
	 * @param userId
	 * @return
	 */
	public Receiver getDefault(Integer userId,String recvType) {
		List<Receiver> list = this.receiverMapper.selectDefault(userId,recvType);
		if(list != null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 获取指定用户的所有所获人信息
	 * @param userId
	 * @return
	 */
	public List<Receiver> getAllByUser(Integer userId,String recvType){
		return this.receiverMapper.selectAllByUser(userId,recvType);
	}
	
}
