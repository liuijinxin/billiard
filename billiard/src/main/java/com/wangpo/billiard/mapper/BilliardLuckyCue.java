package com.wangpo.billiard.mapper;

import com.wangpo.billiard.bean.LuckyCue;
import org.apache.ibatis.annotations.*;

@Mapper
public interface BilliardLuckyCue {
	@Insert("insert into lucky_cue(player_id,level,free_flag,vip_flag,free_time,free_times,vip_times) " +
			"values(#{playerId},#{level},#{freeFlag},#{vipFlag},#{freeTime},#{freeTimes},#{vipTimes})")
	int insertLuckyCue(LuckyCue luckyCue);

	@Update("update lucky_cue set level=#{level},free_flag=#{freeFlag},vip_flag=#{vipFlag},free_time=#{freeTime},free_times=#{freeTimes},vip_times=#{vipTimes} where id = #{id}")
	int updateLuckyCue(LuckyCue luckyCue);

	@Select("select id,player_id,level,free_flag,vip_flag,free_time,free_times,vip_times from lucky_cue where player_id=#{id}")
	@Results(value = {
			@Result(property = "id", column = "id"),
			@Result(property = "playerId", column = "player_id"),
			@Result(property = "level", column = "level"),
			@Result(property = "freeFlag", column = "free_flag"),
			@Result(property = "vipFlag", column = "vip_flag"),
			@Result(property = "freeTime", column = "free_time"),
			@Result(property = "freeTimes", column = "free_times"),
			@Result(property = "vipTimes", column = "vip_times"),
	})
	LuckyCue selectLuckyCueByID(int id);
}
