package com.mofangyouxuan.service;

import java.util.List;

import com.mofangyouxuan.model.Area;
import com.mofangyouxuan.model.City;
import com.mofangyouxuan.model.Province;

public interface CitiesMgrService {
	
	public Province getProvince(String codeName);
	
	public City getCity(String codeName);
	
	public Area getArea(String codeName);
	
	public List<Province> getAllProvince();
	
	public List<City> getCities(String provCode);
	
	public List<Area> getAreas(String cityCode);

	
}
