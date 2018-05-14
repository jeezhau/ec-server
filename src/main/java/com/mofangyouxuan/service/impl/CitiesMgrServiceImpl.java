package com.mofangyouxuan.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mofangyouxuan.mapper.AreaMapper;
import com.mofangyouxuan.mapper.CityMapper;
import com.mofangyouxuan.mapper.ProvinceMapper;
import com.mofangyouxuan.model.Area;
import com.mofangyouxuan.model.City;
import com.mofangyouxuan.model.Province;
import com.mofangyouxuan.service.CitiesMgrService;

@Service
@Transactional
public class CitiesMgrServiceImpl implements CitiesMgrService {
	
	@Autowired
	private ProvinceMapper provinceMapper;
	
	@Autowired
	private CityMapper cityMapper;
	
	@Autowired
	private AreaMapper areaMapper;
	

	@Override
	public Province getProvince(String codeName) {
		return this.provinceMapper.selectByCodeName(codeName);
	}

	@Override
	public City getCity(String codeName) {
		return this.cityMapper.selectByCodeName(codeName);
	}

	@Override
	public Area getArea(String codeName) {
		return this.areaMapper.selectByCodeName(codeName);
	}

	@Override
	public List<Province> getAllProvince() {
		return this.provinceMapper.selectAll();
	}

	@Override
	public List<City> getCities(String provCode) {
		return this.cityMapper.selectByProvCode(provCode);
	}

	@Override
	public List<Area> getAreas(String cityCode) {
		return this.areaMapper.selectByCityCode(cityCode);
	}

}
