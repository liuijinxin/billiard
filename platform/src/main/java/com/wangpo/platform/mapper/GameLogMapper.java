package com.wangpo.platform.mapper;

import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.framework.StrategyManager;
import com.wangpo.platform.framework.TableSplitRule;
import com.wangpo.platform.framework.TableSplitTarget;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
@TableSplitTarget(rules={@TableSplitRule(tableName="game_log",paramName="createTime",targetName= StrategyManager._YYYYMM)})
public interface GameLogMapper {

    @Select("select id,player_id,log_type,item_id,item_num,remain_num,reason,create_time from game_log where player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "logType", column = "log_type"),
            @Result(property = "itemId", column = "item_id"),
            @Result(property = "itemNum", column = "item_num"),
            @Result(property = "remainNum", column = "remain_num"),
            @Result(property = "reason", column = "reason"),
            @Result(property = "create_time", column = "create_time"),
    })
    List<GameLog> selectGameLogByPlayerId(int playerId);


    @Insert("insert into game_log set player_id = #{playerId},log_type = #{logType},item_id = #{itemId},remain_num = #{remainNum},item_num = #{itemNum},reason = #{reason},create_time = #{createTime}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGameLog(GameLog gameLog);


}
