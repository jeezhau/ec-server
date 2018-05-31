package com.mofangyouxuan.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.PartnerStaffMapper;
import com.mofangyouxuan.model.PartnerStaff;
import com.mofangyouxuan.service.PartnerStaffService;

@Service
@Transactional
public class PartnerStaffServiceImpl implements PartnerStaffService{

	@Autowired
	private PartnerStaffMapper partnerStaffMapper;
	
	/**
	 * 添加员工信息
	 * @return
	 */
	public JSONObject addStaff(PartnerStaff staff) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userId", staff.getUserId());
		params.put("partnerId", staff.getPartnerId());
		int hasCnt = this.partnerStaffMapper.countAll(params);
		if(hasCnt>0) {
			jsonRet.put("errcode", ErrCodes.PARTNER_STAFF_EXISTS);
			jsonRet.put("errmsg", "该用户已经添加过！");
			return jsonRet;
		}
		if(staff.getTagList() != null) {
			staff.setTagList("0," + staff.getTagList() + ",0");
		}
		staff.setRecId(null);
		staff.setUpdateTime(new Date());
		int cnt = this.partnerStaffMapper.insert(staff);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 更新员工信息
	 * @return
	 */
	public JSONObject updateStaffBasic(PartnerStaff staff) {
		JSONObject jsonRet = new JSONObject();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userId", staff.getUserId());
		params.put("partnerId", staff.getPartnerId());
		List<PartnerStaff> list = this.partnerStaffMapper.selectAll(params, null, new PageCond(0,1));
		if(list !=null && list.size()>0) {
			if(!list.get(0).getRecId().equals(staff.getRecId())) {
				jsonRet.put("errcode", ErrCodes.COMPLAIN_HAS_EXISTS);
				jsonRet.put("errmsg", "该用户已经添加过！");
				return jsonRet;
			}
		}
		if(staff.getTagList() != null) {
			staff.setTagList("0," + staff.getTagList() + ",0");
		}
		staff.setUpdateTime(new Date());
		int cnt = this.partnerStaffMapper.updateByPrimaryKeySelective(staff);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 更新密码与标签等信息
	 * @param staff
	 * @return
	 */
	public JSONObject updateOther(PartnerStaff staff) {
		JSONObject jsonRet = new JSONObject();
		int cnt = this.partnerStaffMapper.updateByPrimaryKeySelective(staff);
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 删除员工信息
	 * @return
	 */
	public JSONObject deleteStaff(PartnerStaff staff) {
		JSONObject jsonRet = new JSONObject();
		int cnt = this.partnerStaffMapper.deleteByPrimaryKey(staff.getRecId());
		if(cnt >0) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据库保存数据错误！");
		}
		return jsonRet;
	}
	
	/**
	 * 使用合作伙伴ID与用户ID获取员工信息
	 * @param partnerId
	 * @param userId
	 * @return
	 */
	public PartnerStaff get(Integer partnerId,Integer userId) {
		return this.partnerStaffMapper.selectByUser(partnerId, userId);
	}
	
	/**
	 * 根据系统ID获取员工信息
	 * @param recId
	 * @return
	 */
	public PartnerStaff get(Long recId) {
		return this.partnerStaffMapper.selectByPrimaryKey(recId);
	}
	
	/**
	 * 获取所有的员工信息
	 * 
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<PartnerStaff> getAll(Map<String,Object> params,String sorts,PageCond pageCond){
		if(pageCond == null) {
			pageCond = new PageCond(0,30);
		}
		return this.partnerStaffMapper.selectAll(params, sorts, pageCond);
	}
	
	
	/**
	 * 统计所有的员工数量
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params) {
		return this.partnerStaffMapper.countAll(params);
	}
	
	/**
	 * 添加标签至员工
	 * @param userList
	 * @param tagId
	 * @return
	 */
	public JSONObject addTag2Staff(Integer partnerId,List<Integer> userList,long tagId,Integer updateOpr) {
		JSONObject jsonRet = new JSONObject();
		JSONArray errList = new JSONArray();
		List<Integer> okList = new ArrayList<Integer>();
		for(Integer userId: userList) {
			int cnt = this.partnerStaffMapper.addTag2Staff(partnerId, userId, tagId,updateOpr);
			if(cnt>0) {
				okList.add(userId);
			}else {
				errList.add(userId);
			}
		}
		if(okList.size() == userList.size()) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "添加失败的用户：" + errList.toJSONString());
		}
		return jsonRet;
	}
	
	/**
	 * 从员工身上移除标签
	 * @param userList
	 * @param tagId
	 * @return
	 */
	public JSONObject removeTag4Staff(Integer partnerId,List<Integer> userList,long tagId,Integer updateOpr) {
		JSONObject jsonRet = new JSONObject();
		JSONArray errList = new JSONArray();
		List<Integer> okList = new ArrayList<Integer>();
		for(Integer userId: userList) {
			int cnt = this.partnerStaffMapper.removeTagFromStaff(partnerId, userId, tagId,updateOpr);
			if(cnt>0) {
				okList.add(userId);
			}else {
				errList.add(userId);
			}
		}
		if(okList.size() == userList.size()) {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
		}else {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "添加失败的用户：" + errList.toJSONString());
		}
		return jsonRet;
	}
	
	/**
	 * 删除标签
	 * @param userList
	 * @param tagId
	 * @return
	 */
	public JSONObject removeTag(Integer partnerId,long tagId,Integer updateOpr) {
		JSONObject jsonRet = new JSONObject();
		this.partnerStaffMapper.removeTagFromAll(partnerId, tagId,updateOpr);
		jsonRet.put("errcode", 0);
		jsonRet.put("errmsg", "ok");
		return jsonRet;
	}	
	
}


