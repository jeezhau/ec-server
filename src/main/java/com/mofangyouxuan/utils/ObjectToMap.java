package com.mofangyouxuan.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 将java 对象转换为Map
 * @author jeekhan
 *
 */
public class ObjectToMap {
	
	public static Map<String,Object> object2Map(Object obj,String[] excludeFields) {
		Map<String,Object> map = new HashMap<String,Object>();
		Field[] fields = obj.getClass().getDeclaredFields();
		for(Field field:fields) {
			try {
				String methodName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
				methodName = "get"+ methodName;
				Method method = obj.getClass().getDeclaredMethod(methodName, Void.class);
				if(!(Arrays.binarySearch(excludeFields, field.getName()) > 0)) {
					map.put(field.getName(), method.invoke(obj, null));
				}
			}catch(Exception e) {
				
			}
		}
		return map;
	}

}
