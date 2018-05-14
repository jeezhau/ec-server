package com.mofangyouxuan.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.mapper.QrCodeSenceMapper;
import com.mofangyouxuan.model.QrCodeSence;
import com.mofangyouxuan.service.QrCodeSenceService;

@Service
@Transactional
public class QrCodeSenceServiceImple implements QrCodeSenceService {

	@Autowired
	private QrCodeSenceMapper qrCodeSenceMapper;
	
	@Override
	public Integer add(QrCodeSence qrCodeSence) {
		qrCodeSence.setCreateTime(new Date());
		int cnt = this.qrCodeSenceMapper.insert(qrCodeSence);
		if(cnt>0) {
			return qrCodeSence.getUserId();
		}
		return null;
	}

	@Override
	public int update(QrCodeSence qrCodeSence) {
		qrCodeSence.setCreateTime(new Date());
		int cnt = this.qrCodeSenceMapper.updateByPrimaryKey(qrCodeSence);
		return cnt;
	}

	@Override
	public QrCodeSence get(Integer id) {
		return this.qrCodeSenceMapper.selectByPrimaryKey(id);
	}
	
	/**
	 * 删除指定的推广二维码信息
	 * @param id
	 * @return
	 */
	public int delete(Integer id) {
		return this.qrCodeSenceMapper.deleteByPrimaryKey(id);
	}

}
