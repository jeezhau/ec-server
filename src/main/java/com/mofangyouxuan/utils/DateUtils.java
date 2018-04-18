package com.mofangyouxuan.utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtils {
	private static SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy");
	private static SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdf14 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	public Date getCurrentDate(){
		return new Date();
	}
	
	public String getYear(){
		return sdf4.format(new Date());
	}
	
	public String getYear(Date date){
		return sdf4.format(date);
	}
	
	public String getDate(){
		return sdf8.format(new Date());
	}
	
	public String getDate(Date date){
		return sdf8.format(date);
	}
	
	public String getDateTime(){
		return sdf14.format(new Date());
	}
	
	public String getDateTime(Date date){
		return sdf14.format(date);
	}
}
