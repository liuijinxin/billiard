package com.wangpo.billiard.config;

import com.wangpo.billiard.framework.TableSplitInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptConfig {

	/**
	 * mybatis分表拦截器
	 * @return
	 */
	@Bean
	public TableSplitInterceptor initTableSplitInterceptor() {
		return new TableSplitInterceptor();
	}
}

