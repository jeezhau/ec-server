package com.mofangyouxuan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mofangyouxuan.service.CitiesMgrService;

/**
 * 国家城市元数据管理
 * @author jeekhan
 *
 */
@RestController
@RequestMapping("/city")
public class CitiesMgrAction {
	
	@Autowired
	private CitiesMgrService citiesMgrService;
	
	/**
	 * 获取所有省份
	 * @return
	 */
	@RequestMapping("/province/getall")
	public Object getAllProvinces() {
		return this.citiesMgrService.getAllProvince();
	}

	/**
	 * 获取指定省份下的所有城市
	 * @param provCode
	 * @return
	 */
	@RequestMapping("/city/getbyprov/{provCode}")
	public Object getCities(@PathVariable("provCode")String provCode) {
		
		return this.citiesMgrService.getCities(provCode);
	}
	
	/**
	 * 获取指定城市下的所有县
	 * @param cityCode
	 * @return
	 */
	@RequestMapping("/area/getbycity/{cityCode}")
	public Object getAreas(@PathVariable("cityCode")String cityCode) {
		
		return this.citiesMgrService.getAreas(cityCode);
	}
	
	
}
