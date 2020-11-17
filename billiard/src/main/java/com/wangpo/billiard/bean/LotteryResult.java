package com.wangpo.billiard.bean;

import lombok.Data;

import java.util.Date;

/**
 * 抽奖结果
 */
@Data
public class LotteryResult {
    /** 抽奖结果唯一id */
    private int id;
    /** 昵称 */
    private String nick;
    /** 场次 */
    private String chang;
    /** 玩家id */
    private int playerId;
    /** 抽奖总价值 */
    private int totalMoney;
    /** 奖项类型 */
    private int awardType;
    /** 奖项实际价值 */
    private int awardNum;
    /** 奖项价值 */
    private int base;
    /** 抽奖时间 */
    private Date createTime;

}
