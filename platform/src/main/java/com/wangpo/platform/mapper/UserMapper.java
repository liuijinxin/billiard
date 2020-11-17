package com.wangpo.platform.mapper;

import com.wangpo.platform.dao.UserEntity;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
	@Insert("insert into user(token,nick,head,gold,diamond,update_time,create_time) values(#{token},#{nick},#{head},#{gold},#{diamond},#{updateTime},#{createTime})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(UserEntity userEntity);

	@Update("update user set nick=#{nick}, gold=#{gold},diamond=#{diamond},update_time=#{updateTime} where id=#{id}")
	int update(UserEntity userEntity);

	@Delete("delete from user where id=#{id}")
	int delete(int id);

	@Select("select id, token,nick,head,gold,diamond,update_time,create_time from user where id=#{id}")
	UserEntity selectByID(int id);

	@Select("select id, token,nick,head,gold,diamond,update_time,create_time from user where token=#{token}")
	UserEntity selectByToken(String token);
}
