package com.wangpo.base.enums;

public enum GameEventEnum {
	CHARGE(1,"游戏充值"),
	TASK_REWARD(2,"任务奖励"),
	ACTIVE_REWARD(3,"活跃度奖励"),
	BILLIARD_GAME(4,"台球对局"),
	BUY_CUE(5,"购买球杆"),
	FINISH_TASK(6,"完成任务"),
	SELL_CUE(7,"出售球杆"),
	UPGRADE_CUE(8,"升级球杆"),
	DEFEND_CUE(9,"维护球杆"),
	VIP_REWARD(10,"会员奖励"),
	BUY_ROLE(11,"购买角色"),
	MAIL(12,"邮件附件"),
	LOTTERY(13,"抽奖奖励"),
	GIFT_LUCKY_CUE(14,"购买幸运一杆礼包"),
	SIGN_AWARD(15,"签到奖励"),
	RECHARGE(16,"充值"),
	NEW_USER(17,"创建新用户"),
	MON_CARD(18,"月卡每日奖励"),
	RED_ENVELOPER(19,"红包"),
	RESRRECTION(20,"复活"),
	BILLIARD_GAME_FEE(21,"台球对局台费"),
	SHARE(23,"分享"),
	CMS(25,"后台添加"),
	REDENVELOPS(26,"红包"),
	SHOPBUY(27,"商城购买"),
	LUCKY_CUE(3001,"幸运一杆"),
	TEST(9999,"测试"),
	;

	public int code;
	public String reason;

	GameEventEnum(int code, String reason) {
		this.code = code;
		this.reason = reason;
	}
}
