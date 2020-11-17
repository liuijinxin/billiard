package com.wangpo.billiard.config;

import com.wangpo.billiard.framework.StrategyManager;
import com.wangpo.billiard.framework.YYYYMMStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfig {

	/**
	 * 策略配置类
	 * @return
	 */
	@Bean
	public StrategyManager strategyManager() {
		StrategyManager strategyManager = new StrategyManager();
		strategyManager.addStrategy(StrategyManager.FORMAT_YYYYMM, new YYYYMMStrategy());
		return strategyManager;
	}

}
