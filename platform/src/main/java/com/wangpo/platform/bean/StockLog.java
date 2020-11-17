package com.wangpo.platform.bean;

import lombok.Data;

/**
 * 库存日志，每日记录金币，钻石，红包券的库存总量
 */

@Data
public class StockLog {
	private int id;
	private String logDay;
	private long stockGold;
	private long stockDiamond;
	private long stockRedPacket;
	private long activeGold;
	private long activeDiamond;
	private long activeRedPacket;
}
