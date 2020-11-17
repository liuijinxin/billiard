package com.wangpo.base.enums;

public enum ItemEnum {
	GOLD(1),
	DIAMOND(2),
	RED_PACKET(7),
	LUCKY_CUE(8), //幸运一杆次数
	STRONG_CARD(3001),//强化卡
	YONGSHI_CUE(3101),//勇士球杆
	SHUIJING_CUE(3201),//水晶球杆
	HUANGJIN_CUE(3301),//黄金球杆
	ZUANSHI_CUE(3401),//钻石球杆
	XINGYAO_CUE(3501),//星耀球杆
	WANGHZHE_CUE(3601),//王者球杆
	XIYOU_CUE(3701),//稀有球杆
	BISHENG_CUE(3801),//必胜球杆
	GOLDEN_CUE(3901),//金色传说
	MAHOGANY_CUE(4001),//必胜球杆
	MASTER_CUE(4101),//大师球杆
	TRUMP_CUE(4201)//王牌球杆
	;

	public int code;

	ItemEnum(int code){
		this.code = code;
	}
}
