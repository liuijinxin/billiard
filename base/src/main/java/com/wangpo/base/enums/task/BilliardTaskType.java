package com.wangpo.base.enums.task;

public enum BilliardTaskType {
	SHARE(1), //分享任务
	WIN(2), //赢局
	GAME(3),//台球游戏
	ONE_GAN(4),//一杆清
	SIGN(5),//累计签到多少次
	RECHARGE(6) //累计充值多少元
	;
	public int code;

	BilliardTaskType(int code) {
		this.code = code;
	}
}
