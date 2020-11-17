package com.wangpo.platform.enums;


public enum ErrorTipsEnum {
	TIPS_INFO1001(1001,"兑换红包未找到相关配置"),
	TIPS_INFO1002(1002,"红包不足"),
	TIPS_INFO1003(1003,"下单失败，请重新下单"),
	TIPS_INFO1004(1004,"请填写支付宝账号"),
	TIPS_INFO1005(1005,"请填写支付宝姓名"),
	TIPS_INFO1006(1006,"您已经绑定过手机号了"),
	TIPS_INFO1007(1007,"该手机号已经被绑定过了"),
	TIPS_INFO1008(1008,"发送手机验证码时间太频繁！"),
	TIPS_INFO1009(1009,"验证码错误"),
	TIPS_INFO1010(1010,"您还没有购买月卡"),
	TIPS_INFO1011(1011,"已经领取过该奖励了"),
	TIPS_INFO1012(1012,"没有购买该复活礼包"),
	TIPS_INFO1013(1013,"请购买复活月卡"),
	TIPS_INFO1014(1014,"红包最大兑换次数已满"),
	TIPS_INFO1015(1015,"信息错误"),
	TIPS_INFO1016(1016,"商品已下架");
	private int tipsType;
	private String info;
	
	ErrorTipsEnum(int type,String info) {
		this.tipsType = type;
		this.info = info;
	}

	public int getTipsType() {
		return tipsType;
	}

	public void setTipsType(int tipsType) {
		this.tipsType = tipsType;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	

}
