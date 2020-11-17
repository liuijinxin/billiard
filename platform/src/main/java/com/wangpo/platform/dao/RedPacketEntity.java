package com.wangpo.platform.dao;

import com.wangpo.base.bean.PlatFormProto;
import lombok.Data;

import java.util.Date;

@Data
public class RedPacketEntity {
    /** 数据库唯一id */
    private int id;
    /** 玩家id */
    private int playerId;
     /** 玩家昵称 */
    private String nick;
    /** 会员等级 */
    private int vip;
    /** 场次 */
    private int chang;
    /** 金额 */
    private int num;
    /** 领取时间戳 */
    private long time;
    private Date updateTime;
    private Date createTime;

    public PlatFormProto.RedPacket.Builder redPacket2Proto(){
        PlatFormProto.RedPacket.Builder builder = PlatFormProto.RedPacket.newBuilder();
        return builder.setId(playerId)
                .setNick(nick)
                .setVip(vip)
                .setNum(num)
                .setTime(time);
    }


}
