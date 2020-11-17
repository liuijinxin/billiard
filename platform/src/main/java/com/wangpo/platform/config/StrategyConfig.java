package com.wangpo.platform.config;

import com.wangpo.platform.framework.StrategyManager;
import com.wangpo.platform.framework.YYYYMMStrategy;
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
		strategyManager.addStrategy(StrategyManager._YYYYMM, new YYYYMMStrategy());
		return strategyManager;
	}

}
