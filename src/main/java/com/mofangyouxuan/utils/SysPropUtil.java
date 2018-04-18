package com.mofangyouxuan.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;


public class SysPropUtil {
	

	private static String prop_file = "props/SysParam";
	
	private static  ResourceBundle BUNDLE = ResourceBundle.getBundle(prop_file);
	
	/**
	 * 根据指定key获取对应的值
	 * @param key
	 * @param params
	 * @return
	 */
	public static String getForamtValue(String key,Object[] params) {
		String keyValue = null;
		String msg = null;
		try{
			keyValue = BUNDLE.getString(key);
			MessageFormat mf = new MessageFormat(keyValue); 
			msg = mf.format(params);  
		}catch(Exception e){
			return null;
		}
		return msg;
	}
	
	public static String getParam(String key) {
		return BUNDLE.getString(key);
	}
	
	
}
