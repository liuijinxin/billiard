package com.wangpo.billiard.bean;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.BilliardProto;
import lombok.Data;

import java.util.Date;

/**
 * 球杆
 */
@Data
public class PlayerCue {
	/**
	 * 数据库配置
	 */
	/** 数据库id */
	private int id;
	/** 玩家 id*/
	private int playerID;
	/** 球杆ID，升级后会改变 */
	private int cueID;
	 /** 球杆损坏到期时间也就是球杆寿命，维护后可延长时间 */
	private long damageTime;
	 /** 星级 */
	private int star;
	/** 是否使用 */
	private int isUse;
	/** 维护次数 */
	private  int defendTimes;
	/** 维护天数 */
	private long defendDay;
	private Date updateTime;
	private Date createTime;

	public BilliardProto.PlayerCue.Builder toProto(){
		return BilliardProto.PlayerCue.newBuilder()
				.setId(id)
				.setPlayerID(playerID)
				.setCueID(cueID)
				.setGrade(star)
				.setIsUse(isUse)
				.setDamageTime(damageTime)
				.setDefendDay(defendDay)
				.setDefendTimes(defendTimes);
	}

}
