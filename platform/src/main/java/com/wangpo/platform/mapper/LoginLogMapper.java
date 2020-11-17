package com.wangpo.platform.mapper;

import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.bean.LoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LoginLogMapper {

    @Select("select id,player_id,create_day,login_day,login_times,online from login_log where player_id = #{playerId} and login_day = #{today}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "createDay", column = "create_day"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "loginDay", column = "login_day"),
            @Result(property = "loginTimes", column = "login_times"),
            @Result(property = "online", column = "online"),
    })
    LoginLog selectLoginLogByPlayerId(@Param("playerId") int playerId,@Param("today") String today);


    @Insert("insert into login_log set player_id = #{playerId},create_day = #{createDay},login_day = #{loginDay},login_times = #{loginTimes},online = #{online}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginLog(LoginLog loginLog);


    @Update("update login_log set login_times = #{loginTimes}, online = #{online}  where id = #{id}")
    int updateLoginLog(LoginLog loginLog);
}
