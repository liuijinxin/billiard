package com.wangpo.platform.bean;

import lombok.Data;
import java.util.Date;

/**
 * 玩家会员
 */
@Data
public class PlayerVip {
    /** 数据库id */
    private int id;
    /** 玩家id */
    private int playerId;
    /** 日奖励领取状态 */
    private int dayGift;
    /** 等级奖励领取状态，位运算记录领取状态 */
    private int levelGift;
    /** 会员点数 */
    private int points;
    /** 会员等级 */
    private int level;
    /** 今日日期，记录为了充值每日领取状态 */
    private String today;
    /** 衰退时间 */
    private Date declineTime;
    private Date updateTime;
    private Date createTime;

}
