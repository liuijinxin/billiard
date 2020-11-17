package com.wangpo.platform.dao;

import java.util.Date;

import lombok.Data;

@Data
public class PlayerGift {
	
	private int id;
	/** 玩家Id*/
	private int playerId;
	/** 商品Id*/
	private int goodsId;
	/** 当日领取次数*/
	private int  todayUse;
	/** 到期时间0代表每日重置删除*/
	private long endTime;
	/** 每日购买次数*/
	private int everyDayBuy;
	/** 永久购买次数*/
	private int permanentBuy;
	/** 房间场次*/
	private int roomNum;

}
