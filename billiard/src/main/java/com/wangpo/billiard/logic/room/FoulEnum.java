package com.wangpo.billiard.logic.room;

/**
 * @author Administrator
 */

public enum FoulEnum {
	/**
	 * 未犯规
	 */
	NO_FOUL(0,"未犯规"),
	/**
	 * 空杆犯规，未碰到球
	 */
	NULL_CUE(1,"空杆犯规"),
	/**
	 * 击球犯规，未集中目标球
	 */
	HIT_ERROR(2,"击球犯规"),
	/**
	 * 白球进袋
	 */
	WHITE_SNOOKER(3,"白球进袋"),
	/**
	 * 超时犯规
	 */
	TIME_OUT(4,"超时犯规"),
	;

	public int code;
	public String reason;
	FoulEnum(int code,String reason) {
		this.code = code;
		this.reason = reason;
	}
}
