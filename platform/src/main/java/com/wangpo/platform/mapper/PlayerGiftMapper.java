package com.wangpo.platform.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wangpo.base.bean.Mail;
import com.wangpo.platform.dao.BuyOrder;
import com.wangpo.platform.dao.PlayerGift;

@Mapper
public interface PlayerGiftMapper {
	
    @Select("select id,player_id,goods_id,today_use,end_time,every_day_buy,permanent_buy,room_limit from player_gift where player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "goodsId", column = "goods_id"),
            @Result(property = "todayUse", column = "today_use"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "everyDayBuy", column = "every_day_buy"),
            @Result(property = "permanentBuy", column = "permanent_buy"),
            @Result(property = "roomNum", column = "room_limit")
    })
    List<PlayerGift> selectPlayerGift(int playerId);
    
    
    @Select("select id,player_id,goods_id,today_use,end_time,every_day_buy,permanent_buy,room_limit from player_gift where goods_id = #{id} and player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "goodsId", column = "goods_id"),
            @Result(property = "todayUse", column = "today_use"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "everyDayBuy", column = "every_day_buy"),
            @Result(property = "permanentBuy", column = "permanent_buy"),
            @Result(property = "roomNum", column = "room_limit")
    })
    PlayerGift selectPlayerGiftById(@Param("id")int id,@Param("playerId")int playerId);
    
    @Insert("insert into player_gift (player_id,goods_id,today_use,end_time,every_day_buy,permanent_buy,room_limit) " +
            "value(#{playerId},#{goodsId},#{todayUse},#{endTime},#{everyDayBuy},#{permanentBuy},#{roomNum})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPlayerGift(PlayerGift playerGift);
    
    @Update("update player_gift set today_use = #{todayUse},end_time = #{endTime},every_day_buy = #{everyDayBuy},permanent_buy = #{permanentBuy} where id = #{id}")
    int updatePlayerGift(PlayerGift playerGift);
    
    @Delete("delete from player_gift where id = #{id}")
    int deletePlayerGiftById(long id);

}
