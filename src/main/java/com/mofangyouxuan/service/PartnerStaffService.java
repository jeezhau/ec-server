package com.mofangyouxuan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.model.PartnerStaff;

public interface PartnerStaffService {
	
	/**
	 * 添加员工信息
	 * @return
	 */
	public JSONObject addStaff(PartnerStaff staff);
	
	/**
	 * 更新员工基本信息
	 * @return
	 */
	public JSONObject updateStaffBasic(PartnerStaff staff);
	
	/**
	 * 更新密码与标签等信息
	 * @param staff
	 * @return
	 */
	public JSONObject updateOther(PartnerStaff staff);
	
	/**
	 * 删除员工信息
	 * @return
	 */
	public JSONObject deleteStaff(PartnerStaff staff);
	
	/**
	 * 使用合作伙伴ID与用户ID获取员工信息
	 * @param partnerId
	 * @param userId
	 * @return
	 */
	public PartnerStaff get(Integer partnerId,Integer userId);
	
	/**
	 * 根据系统ID获取员工信息
	 * @param recId
	 * @return
	 */
	public PartnerStaff get(Long recId);
	
	/**
	 * 获取所有的员工信息
	 * 
	 * @param params
	 * @param sorts
	 * @param pageCond
	 * @return
	 */
	public List<PartnerStaff> getAll(Map<String,Object> params,String sorts,PageCond pageCond);
	
	
	/**
	 * 统计所有的员工数量
	 * @param params
	 * @return
	 */
	public int countAll(Map<String,Object> params);
	
	/**
	 * 添加标签至员工
	 * @param userList
	 * @param tagId
	 * @return
	 */
	public JSONObject addTag2Staff(Integer partnerId,List<Integer> userList,long tagId,Integer updateOpr);
	
	/**
	 * 从员工身上移除标签
	 * @param userList
	 * @param tagId
	 * @return
	 */
	public JSONObject removeTag4Staff(Integer partnerId,List<Integer> userList,long tagId,Integer updateOpr);
	
	/**
	 * 删除标签
	 * @param partnerId
	 * @param tagId
	 * @param updateOpr
	 * @return
	 */
	public JSONObject removeTag(Integer partnerId,long tagId,Integer updateOpr);
	
}



