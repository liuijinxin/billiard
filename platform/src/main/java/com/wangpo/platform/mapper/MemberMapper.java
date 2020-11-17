package com.wangpo.platform.mapper;

import com.wangpo.platform.bean.PlayerVip;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MemberMapper {

    @Select("select id,player_id,day_gift,level_gift,points,level,today,decline_time,update_time,create_time from player_vip where player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "dayGift", column = "day_gift"),
            @Result(property = "levelGift", column = "level_gift"),
            @Result(property = "points", column = "points"),
            @Result(property = "level", column = "level"),
            @Result(property = "today", column = "today"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "declineTime", column = "decline_time"),
            @Result(property = "createTime", column = "create_time"),
    })
    PlayerVip selectMemberByPlayerId(int playerId);

    @Insert("insert into player_vip(player_id,day_gift,level_gift,points,level,today,decline_time,update_time,create_time)" +
            " values(#{playerId},#{dayGift},#{levelGift},#{points},#{level},#{today},#{declineTime},#{updateTime},#{createTime})")
    int insertMember(PlayerVip playerVip);

    @Update("update player_vip set day_gift = #{dayGift},level_gift = #{levelGift},points = #{points},level = #{level}, today = #{today}, decline_time = #{declineTime}, update_time = #{updateTime} where id = #{id}")
    int updateMember(PlayerVip playerVip);

}
