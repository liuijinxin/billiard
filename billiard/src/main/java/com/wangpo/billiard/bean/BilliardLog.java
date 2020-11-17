package com.wangpo.billiard.bean;

import lombok.Data;

import java.util.Date;

@Data
public class BilliardLog {
	/** 数据库唯一id */
	private long id;
	/** 房间号 */
	private int roomNo;
	/** 场次 */
	private int chang;
	/** 玩家1 */
	private int player1;
	/** 玩家2 */
	private int player2;
	/** 玩家3 */
	private int player3;
	/** 金币类型 */
	private int moneyType;
	/** 总杆数 */
	private int totalCue;
	/** 加倍次数 */
	private int doubleTimes;
	/** ai盈亏 */
	private int aiMoney;
	/** 场费 */
	private int fee;
	/** 游戏事件 */
	private Date gameTime;
}
