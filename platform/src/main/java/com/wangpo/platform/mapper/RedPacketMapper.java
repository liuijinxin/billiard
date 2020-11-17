package com.wangpo.platform.mapper;

import com.wangpo.platform.dao.RedPacketEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RedPacketMapper {

    @Insert("insert into red_packet (player_id,nick,vip,chang,num,time,update_time,create_time)" +
            " values(#{playerId},#{nick},#{vip},#{chang},#{num},#{time},#{updateTime},#{createTime})")
    int insertRedPacket(RedPacketEntity redPacketEntity);

    @Select("select id,player_id,nick,vip,chang,num,time,update_time,create_time from red_packet limit 100")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "nick", column = "nick"),
            @Result(property = "vip", column = "vip"),
            @Result(property = "chang", column = "chang"),
            @Result(property = "num", column = "num"),
            @Result(property = "time", column = "time"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "update_time"),
    })
    List<RedPacketEntity> selectAllRedPacket();

}
