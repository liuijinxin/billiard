package com.wangpo.platform.bean;

import lombok.Data;

import java.util.Date;

@Data
public class GameLog {
	private int id;
	private int logType;
	private int playerId;
	private int itemId;
	private int itemNum;
	private int remainNum;
	private String reason;
	private Date createTime;

	public GameLog() {
	}

	public GameLog(int logType, int playerId, int itemId, int itemNum, String reason) {
		this.logType = logType;
		this.playerId = playerId;
		this.itemId = itemId;
		this.itemNum = itemNum;
		this.reason = reason;
	}

}
