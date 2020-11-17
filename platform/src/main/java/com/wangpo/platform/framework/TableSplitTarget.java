package com.wangpo.platform.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableSplitTarget {

	boolean interFale() default true;
	//分表规则
	public TableSplitRule[] rules();
}
