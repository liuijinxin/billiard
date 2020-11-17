package com.wangpo.billiard.mapper;

import com.wangpo.billiard.bean.Player;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface BilliardPlayerMapper {
	@Insert("insert into player(id,nick,head,sex,gold,diamond,exp,fight,chang,login_time,logout_time,update_time,create_time) " +
			"values(#{id},#{nick},#{head},#{sex},#{gold},#{diamond},#{exp},#{fight},#{chang},#{loginTime},#{logoutTime},#{updateTime},#{createTime})")
	int insertPlayer(Player player);

	@Update("update player set gold=#{gold},diamond=#{diamond}, exp=#{exp},fight=#{fight,jdbcType=OTHER,typeHandler=com.wangpo.billiard.mapper.MySqlJsonHandler}," +
			"chang=#{chang,jdbcType=OTHER,typeHandler=com.wangpo.billiard.mapper.MySqlJsonHandler}," +
			"item=#{itemList,jdbcType=OTHER,typeHandler=com.wangpo.billiard.mapper.MySqlItemListHandler},login_time=#{loginTime},logout_time=#{logoutTime},update_time=#{updateTime} where id = #{id}")
	int updatePlayer(Player player);

	@Select("select id,nick,head,sex,exp,fight,chang,item,login_time,logout_time,update_time,create_time from player where id=#{id}")
	@Results(value = {
			@Result(property = "id", column = "id"),
			@Result(property = "nick", column = "nick"),
			@Result(property = "head", column = "head"),
			@Result(property = "sex", column = "sex"),
			@Result(property = "gold", column = "gold"),
			@Result(property = "nick", column = "nick"),
			@Result(property = "exp", column = "exp"),
			@Result(property = "fight", column = "fight"),
			@Result(property = "chang", column = "chang"),
			@Result(property = "itemList", column = "item", jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.billiard.mapper.MySqlItemListHandler.class),
			@Result(property = "loginTime", column = "login_time"),
			@Result(property = "logoutTime", column = "logout_time"),
			@Result(property = "createTime", column = "createTime"),
			@Result(property = "updateTime", column = "update_time"),
	})
	Player selectPlayerByID(int id);

	@Select("select id,nick,head,sex,gold,diamond,exp,fight,chang,item,login_time,logout_time,update_time,create_time from player where id<0")
	@Results(value = {
			@Result(property = "id", column = "id"),
			@Result(property = "nick", column = "nick"),
			@Result(property = "head", column = "head"),
			@Result(property = "sex", column = "sex"),
			@Result(property = "gold", column = "gold"),
			@Result(property = "diamond", column = "diamond"),
			@Result(property = "exp", column = "exp"),
			@Result(property = "fight", column = "fight"),
			@Result(property = "chang", column = "chang"),
			@Result(property = "itemList", column = "item", jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.billiard.mapper.MySqlItemListHandler.class),
			@Result(property = "loginTime", column = "login_time"),
			@Result(property = "logoutTime", column = "logout_time"),
			@Result(property = "createTime", column = "createTime"),
			@Result(property = "updateTime", column = "update_time"),
	})
	List<Player> selectRobot();
}
