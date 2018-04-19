package com.mofangyouxuan.service;

import com.mofangyouxuan.model.QrCodeSence;

public interface QrCodeSenceService {
	
	/**
	 * 添加新推广二维码
	 * @param qrCodeSence
	 * @return 新ID
	 */
	public Integer add(QrCodeSence qrCodeSence);
	
	/**
	 * 更新推广二维码信息
	 * @param qrCodeSence
	 * @return 更新记录数
	 */
	public int update(QrCodeSence qrCodeSence);
	
	/**
	 * 根据ID获取推广二维码信息
	 * @param id
	 * @return
	 */
	public QrCodeSence get(Integer id);
	
	/**
	 * 删除指定的推广二维码信息
	 * @param id
	 * @return
	 */
	public int delete(Integer id);
	
}
