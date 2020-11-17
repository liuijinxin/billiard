package com.wangpo.platform.mapper;

import com.wangpo.base.bean.Mail;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface MailMapper {

    @Select("select id,player_id,system_id,mail_type,mail_state,title,content,item,time,create_time,end_time from mail where player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "systemId", column = "system_id"),
            @Result(property = "mailType", column = "mail_type"),
            @Result(property = "mailState", column = "mail_state"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "item", column = "item", jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
            @Result(property = "time", column = "time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "endTime", column = "end_time")
    })
    List<Mail> selectMailByPlayerId(int playerId);

    @Select("select id,player_id,system_id,mail_type,mail_state,title,content,item,time,create_time,end_time from mail where mail_type = 0")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "systemId", column = "system_id"),
            @Result(property = "mailType", column = "mail_type"),
            @Result(property = "mailState", column = "mail_state"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "item", column = "item", jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
            @Result(property = "time", column = "time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "endTime", column = "end_time")
    })
    List<Mail> selectSystemMail();

    @Update("update mail set mail_state = #{mailState} where id = #{id}")
    int updateMail(Mail mail);

    @Insert("insert into mail(player_id,system_id,mail_type,mail_state,title,content,item,time,create_time,end_time) " +
            "value(#{playerId},#{systemId},#{mailType},#{mailState},#{title},#{content},#{item,jdbcType=OTHER,typeHandler=com.wangpo.platform.mapper.MySqlJsonHandler},#{time},#{createTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMail(Mail mail);

    @Delete("delete from mail where id = #{id}")
    int deleteMailById(long id);


}
