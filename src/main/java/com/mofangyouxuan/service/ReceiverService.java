package com.mofangyouxuan.service;

import java.util.List;

import com.mofangyouxuan.model.Receiver;

public interface ReceiverService {
	
	/**
	 * 获取指定ID的收货人信息
	 * @param recvId
	 * @return
	 */
	public Receiver getById(Long recvId) ;
	
	/**
	 * 添加收货人信息
	 * @param receiver
	 * @return
	 */
	public Long add(Receiver receiver);
	
	/**
	 * 更新收货人信息
	 * @param receiver
	 * @return
	 */
	public int update(Receiver receiver);
	
	/**
	 * 删除收货人信息
	 * @param receiver
	 * @return
	 */
	public int delete(Receiver receiver);
	
	/**
	 * 设置为默认
	 * @param receiver
	 * @return
	 */
	public int setDefault(Receiver receiver);

	/**
	 * 获取默认收货人信息
	 * @param userId
	 * @return
	 */
	public Receiver getDefault(Integer userId,String recvType);
	
	/**
	 * 获取指定用户的所有所获人信息
	 * @param userId
	 * @return
	 */
	public List<Receiver> getAllByUser(Integer userId,String recvType);
	
}
