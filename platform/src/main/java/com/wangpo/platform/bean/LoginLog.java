package com.wangpo.platform.bean;

import lombok.Data;

@Data
public class LoginLog {
	private long id;
	private int playerId;
	private String createDay;//注册天
	private String loginDay; //登录天
	private int loginTimes;//登录次数
	private int online;//在线时长
}
