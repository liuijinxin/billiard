package com.wangpo.billiard.bean;

import lombok.Data;

import java.util.Date;

/**
 * 幸运一杆
 */

@Data
public class LuckyCue {
	private long id;
	private int playerId;
	private int level;
	private long freeTime;
	//免费次数
	private int freeTimes;
	//vip购买次数
	private int vipTimes;
	//免费领奖领奖标志
	private int freeFlag;
	//收费领奖标志
	private int vipFlag;

	/** 领奖次数，一开球即扣次数，小于0可以领奖 **/
	private int rewardTimes = 0;
	private int rewardType = 0;
}
