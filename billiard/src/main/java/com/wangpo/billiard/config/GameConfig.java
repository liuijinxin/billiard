package com.wangpo.billiard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("game")
@Component
public class GameConfig {
	/** 匹配规则，0-随机，1-按规则 **/
	private int matchStrategy;

	public int getMatchStrategy() {
		return matchStrategy;
	}

	public void setMatchStrategy(int matchStrategy) {
		this.matchStrategy = matchStrategy;
	}
}
