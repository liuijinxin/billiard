package com.wangpo.billiard.mapper;

import com.wangpo.billiard.bean.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Insert("insert into role(role_id,player_id,isuse,exp,update_time,create_time) values (#{roleId},#{playerId},#{isUse},#{exp},#{updateTime},#{createTime})")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    int insertRole(Role role);

    @Select("select id,role_id,player_id,isuse,exp,update_time,create_time from role where player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "isUse", column = "isuse"),
            @Result(property = "exp", column = "exp"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
    })
    List<Role> selectRoleById(int playerId);

    @Update("update role set role_id=#{roleId},isuse=#{isUse},exp=#{exp},update_time=#{updateTime} where id = #{id}")
    int updateRole(Role role);


}
