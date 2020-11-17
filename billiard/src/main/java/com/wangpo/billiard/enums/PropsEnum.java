package com.wangpo.billiard.enums;

public enum PropsEnum {
	//金币
	GOLD(1),
	//钻石
	DIAMOND(2),
	//红包
	RED_PACKET(7);

	private int code;
	PropsEnum(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
