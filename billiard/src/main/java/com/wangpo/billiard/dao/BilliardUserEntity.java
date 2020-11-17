package com.wangpo.billiard.dao;

import lombok.Data;

@Data
public class BilliardUserEntity {
	private long id;
	private String nick;
	private String token;
	private String head;
	private int gold;
	private int diamond;
	//游戏数据
	private int exp;
	private String fight;
}
