package com.wangpo.base.enums;

public enum GlobalEnum {
	//出生金币
	GOLD(1,"出生金币"),
	//出生钻石
	DIAMOND(2,"出生钻石"),
	//初始VIP经验
	VIP(3,"初始VIP经验"),
	//初始球杆ID
	CUE_ID(30001,"初始球杆ID"),
	//初始角色形象ID
	ROLE_ID(30002,"初始角色形象ID"),
	//非抽牌玩法匹配多少秒后直接匹配机器人
	MATCH_TIMEOUT(30003,"匹配多少秒后直接匹配机器人"),
	//匹配方式（0=纯随机，1=根据战力匹配）
	MATCH_WAY(30004,"匹配方式（0=纯随机，1=根据战力匹配）"),
	//抽奖奖池下限（元）
	LOTTERY_DOWN(30005,"抽奖奖池下限（元）"),
	//抽奖奖池上限（元）
	LOTTERY_UP(30006,"抽奖奖池上限（元）"),
	//1元和金币汇率，影响抽奖模块
	GOLD_EXCHANGE_RATE(30007,"1元和金币汇率，影响抽奖模块"),
	//1元和钻石汇率，影响抽奖模块
	DIAMOND_EXCHANGE_RATE(30008,"1元和钻石汇率，影响抽奖模块"),
	//1元和红包汇率，影响抽奖模块
	RED_PACKET_EXCHANGE_RATE(30009,"1元和红包汇率，影响抽奖模块"),
	//1元比强化卡汇率，影响抽奖模块
	STRONG_CARD_EXCHANGE_RATE(30010,"1元比强化卡汇率，影响抽奖模块"),
	//抽牌玩法匹配多少秒后直接匹配机器人
//	DRAW_MATCH_TIMEOUT(30011,"抽牌玩法匹配多少秒后直接匹配机器人"),
	;

	public int code;
	public String desc;

	GlobalEnum(int code, String reason) {
		this.code = code;
		this.desc = reason;
	}
}
