//package com.wangpo.util;
//
//import org.apache.poi.ss.formula.functions.T;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SpringUtils implements ApplicationContextAware {
//	protected static ApplicationContext applicationContext;
//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		this.applicationContext = applicationContext;
//	}
//
//	public static Object getObject(String id) {
//		Object object = null;
//		object = applicationContext.getBean(id);
//		return object;
//	}
//	public static <T> T getObject(Class<T> tClass) {
//		return applicationContext.getBean(tClass);
//	}
//
//	public static Object getBean(String tClass) {
//		return applicationContext.getBean(tClass);
//	}
//
//	public <T> T getBean(Class<T> tClass) {
//		return applicationContext.getBean(tClass);
//	}
//}
