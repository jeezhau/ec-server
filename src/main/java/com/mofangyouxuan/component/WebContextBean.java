package com.mofangyouxuan.component;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;


@Component
public class WebContextBean implements ApplicationContextAware {

	private static ApplicationContext applicationContext; // Spring应用上下文环境

	/*
	 *
	 * 实现了ApplicationContextAware 接口，必须实现该方法；
	 *
	 * 通过传递applicationContext参数初始化成员变量applicationContext
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		WebContextBean.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) throws BeansException {
		return (T) applicationContext.getBean(name);
	}
	
	public static ServletContext getServletContext(){
		WebApplicationContext context = (WebApplicationContext) applicationContext;
		return context.getServletContext();
	}

}
