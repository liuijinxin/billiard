package com.wangpo.billiard.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableSplitRule {

	public String tableName();

	//暂时只支持单参数
	public String paramName();

	public String targetName();
}