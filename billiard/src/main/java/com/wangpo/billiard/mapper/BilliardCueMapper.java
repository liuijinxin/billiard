package com.wangpo.billiard.mapper;

import com.wangpo.billiard.bean.PlayerCue;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BilliardCueMapper {

    @Select("select id,player_id,cue_id,star,damage_time,isuse,defend_times,defend_day,create_time,update_time from player_cue where player_id = #{playerID}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerID", column = "player_id"),
            @Result(property = "cueID", column = "cue_id"),
            @Result(property = "star", column = "star"),
            @Result(property = "damageTime", column = "damage_time"),
            @Result(property = "isUse", column = "isuse"),
            @Result(property = "defendTimes", column = "defend_times"),
            @Result(property = "defendDay", column = "defend_day"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "update_time"),
    })
    List<PlayerCue> selectCueByPlayerId(Integer playerId);

    @Delete("delete from player_cue where id = #{id}")
    int deleteCueById(int id);

    @Insert("insert into player_cue(player_id,cue_id,star,damage_time,isuse,defend_times,defend_day,update_time,create_time) " +
            "values(#{playerID},#{cueID},#{star},#{damageTime},#{isUse},#{defendTimes},#{defendDay},#{updateTime},#{createTime})")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    int addCue(PlayerCue playerCue);

    @Select("select id,player_id,cue_id,star,damage_time,isuse,defend_times,defend_day,create_time,update_time from player_cue where player_id = #{playerID} and cue_id = #{cueID}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerID", column = "player_id"),
            @Result(property = "cueID", column = "cue_id"),
            @Result(property = "star", column = "star"),
            @Result(property = "damageTime", column = "damage_time"),
            @Result(property = "isUse", column = "isuse"),
            @Result(property = "defendTimes", column = "defend_times"),
            @Result(property = "defendDay", column = "defend_day"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "update_time"),
    })
    PlayerCue selectCueById(Integer id, int cueId);

    @Update("update player_cue set star = #{star},cue_id = #{cueID},update_time=#{updateTime} where id = #{id}")
    int upgradeCue(PlayerCue playerCue);

    @Update("update player_cue set isuse = 0 where player_id = #{id}")
    int updateUseCueByPlayerId(Integer id);

    @Update("update player_cue set isuse = #{isUse},update_time=#{updateTime} where id = #{id}")
    int updateUseCue(PlayerCue playerCue);

    @Update("update player_cue set defend_times=#{defendTimes},defend_day=#{defendDay},update_time=#{updateTime} where id = #{id}")
    int updateCueDefend(PlayerCue playerCue);

    @Update("update player_cue set star = #{star},cue_id = #{cueID},isuse = #{isUse},defend_times=#{defendTimes}," +
            "defend_day=#{defendDay},update_time=#{updateTime} where id = #{id}")
    int updateCue(PlayerCue playerCue);

}
