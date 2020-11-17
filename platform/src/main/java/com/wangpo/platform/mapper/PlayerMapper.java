package com.wangpo.platform.mapper;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.platform.bean.Player;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface PlayerMapper {

    @Select("select id,parent_id,origin,new_origin,open_id,union_id,token,nick,sex,head,phone,name,idcard,alipay,gold,diamond,day_active,day_active_status,week_active,week_active_status,last_day,last_monday,total_game,login_time,logout_time," +
            "sign_day,sign_status,status,update_time,create_time,novice_guide from player where id = #{id}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "origin", column = "origin"),
            @Result(property = "newOrigin", column = "new_origin"),
            @Result(property = "token", column = "token"),
            @Result(property = "openId", column = "open_id"),
            @Result(property = "unionId", column = "union_id"),
            @Result(property = "nick", column = "nick"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "head", column = "head"),
            @Result(property = "alipay", column = "alipay",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
            @Result(property = "phone", column = "phone"),
            @Result(property = "name", column = "name"),
            @Result(property = "idcard", column = "idcard"),
            @Result(property = "gold", column = "gold"),
            @Result(property = "diamond", column = "diamond"),
            @Result(property = "dayActive", column = "day_active"),
            @Result(property = "dayActiveStatus", column = "day_active_status"),
            @Result(property = "weekActive", column = "week_active"),
            @Result(property = "weekActiveStatus", column = "week_active_status"),
            @Result(property = "lastDay", column = "last_day"),
            @Result(property = "lastMonday", column = "last_monday"),
            @Result(property = "totalGame", column = "total_game"),
            @Result(property = "loginTime", column = "login_time"),
            @Result(property = "logoutTime", column = "logout_time"),
            @Result(property = "signDay", column = "sign_day"),
            @Result(property = "signStatus", column = "sign_status"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "noviceGuide", column = "novice_guide",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
    })
    Player selectPlayerById(int id);

    @Select("select id,parent_id,origin,new_origin,open_id,union_id,token,nick,sex,head,phone,name,idcard,alipay,gold,diamond,red_packet,day_active,day_active_status,week_active,week_active_status,last_day,last_monday,total_game,login_time,logout_time," +
            "sign_day,sign_status,status,update_time,create_time,novice_guide from player where token = #{token}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "origin", column = "origin"),
            @Result(property = "newOrigin", column = "new_origin"),
            @Result(property = "token", column = "token"),
            @Result(property = "openId", column = "open_id"),
            @Result(property = "unionId", column = "union_id"),
            @Result(property = "nick", column = "nick"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "head", column = "head"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "name", column = "name"),
            @Result(property = "idcard", column = "idcard"),
            @Result(property = "alipay", column = "alipay",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
            @Result(property = "gold", column = "gold"),
            @Result(property = "diamond", column = "diamond"),
            @Result(property = "redPacket", column = "red_packet"),
            @Result(property = "dayActive", column = "day_active"),
            @Result(property = "dayActiveStatus", column = "day_active_status"),
            @Result(property = "weekActive", column = "week_active"),
            @Result(property = "weekActiveStatus", column = "week_active_status"),
            @Result(property = "lastDay", column = "last_day"),
            @Result(property = "lastMonday", column = "last_monday"),
            @Result(property = "totalGame", column = "total_game"),
            @Result(property = "loginTime", column = "login_time"),
            @Result(property = "logoutTime", column = "logout_time"),
            @Result(property = "signDay", column = "sign_day"),
            @Result(property = "signStatus", column = "sign_status"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "noviceGuide", column = "novice_guide",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
    })
    Player selectPlayerByToken(String token);


    @Select("select id,parent_id,origin,new_origin,open_id,union_id,token,nick,sex,head,phone,name,idcard,alipay,gold,diamond,red_packet,day_active,day_active_status,week_active,week_active_status,last_day,last_monday,total_game,login_time,logout_time," +
            "sign_day,sign_status,status,update_time,create_time,novice_guide from player where open_id = #{openId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "origin", column = "origin"),
            @Result(property = "newOrigin", column = "new_origin"),
            @Result(property = "token", column = "token"),
            @Result(property = "openId", column = "open_id"),
            @Result(property = "unionId", column = "union_id"),
            @Result(property = "nick", column = "nick"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "head", column = "head"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "name", column = "name"),
            @Result(property = "idcard", column = "idcard"),
            @Result(property = "alipay", column = "alipay",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
            @Result(property = "gold", column = "gold"),
            @Result(property = "diamond", column = "diamond"),
            @Result(property = "redPacket", column = "red_packet"),
            @Result(property = "dayActive", column = "day_active"),
            @Result(property = "dayActiveStatus", column = "day_active_status"),
            @Result(property = "weekActive", column = "week_active"),
            @Result(property = "weekActiveStatus", column = "week_active_status"),
            @Result(property = "lastDay", column = "last_day"),
            @Result(property = "lastMonday", column = "last_monday"),
            @Result(property = "totalGame", column = "total_game"),
            @Result(property = "loginTime", column = "login_time"),
            @Result(property = "logoutTime", column = "logout_time"),
            @Result(property = "signDay", column = "sign_day"),
            @Result(property = "signStatus", column = "sign_status"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "noviceGuide", column = "novice_guide",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
    })
    Player selectPlayerByOpenid(String openId);
    
    @Select("select id,parent_id,origin,open_id,union_id,token,nick,sex,head,phone,alipay,gold,diamond,red_packet,day_active,day_active_status,week_active,week_active_status,last_day,last_monday,total_game,login_time,logout_time," +
            "sign_day,sign_status,status,update_time,create_time,novice_guide from player where phone = #{phone}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "origin", column = "origin"),
            @Result(property = "newOrigin", column = "new_origin"),
            @Result(property = "token", column = "token"),
            @Result(property = "openId", column = "open_id"),
            @Result(property = "unionId", column = "union_id"),
            @Result(property = "nick", column = "nick"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "head", column = "head"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "alipay", column = "alipay",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
            @Result(property = "gold", column = "gold"),
            @Result(property = "diamond", column = "diamond"),
            @Result(property = "redPacket", column = "red_packet"),
            @Result(property = "dayActive", column = "day_active"),
            @Result(property = "dayActiveStatus", column = "day_active_status"),
            @Result(property = "weekActive", column = "week_active"),
            @Result(property = "weekActiveStatus", column = "week_active_status"),
            @Result(property = "lastDay", column = "last_day"),
            @Result(property = "lastMonday", column = "last_monday"),
            @Result(property = "totalGame", column = "total_game"),
            @Result(property = "loginTime", column = "login_time"),
            @Result(property = "logoutTime", column = "logout_time"),
            @Result(property = "signDay", column = "sign_day"),
            @Result(property = "signStatus", column = "sign_status"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "noviceGuide", column = "novice_guide",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class),
    })
    Player selectPlayerByPhone(String phone);


    @Insert("insert into player (parent_id,origin,new_origin,open_id,union_id,token,nick,sex,head,phone,alipay,name,idcard,gold,diamond,red_packet,status,day_active,day_active_status,week_active,week_active_status,last_day,last_monday,total_game,login_time,logout_time,sign_day,sign_status,update_time,create_time,novice_guide) " +
            "value(#{parentId},#{origin},#{newOrigin},#{openId},#{unionId},#{token},#{nick},#{sex},#{head},#{phone},#{alipay},#{name},#{idcard},#{gold},#{diamond},#{redPacket},#{status},#{dayActive},#{dayActiveStatus},#{weekActive},#{weekActiveStatus},#{lastDay},#{lastMonday},#{totalGame},#{loginTime},#{logoutTime},#{signDay},#{signStatus},#{updateTime},#{createTime},#{noviceGuide})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPlayer(Player player);

    @Update("update player set parent_id = #{parentId},new_origin = #{newOrigin}, open_id = #{openId}, union_id = #{unionId}, nick = #{nick}, head = #{head}, day_active = #{dayActive},day_active_status = #{dayActiveStatus},week_active = #{weekActive},week_active_status = #{weekActiveStatus}," +
            "last_day = #{lastDay}, last_monday = #{lastMonday},total_game = #{totalGame},login_time=#{loginTime},logout_time=#{logoutTime}," +
            "sign_day = #{signDay}, sign_status = #{signStatus},update_time = #{updateTime},novice_guide = #{noviceGuide} where id = #{id}")
    int updatePlayer(Player player);

    @Update("update player set name= #{name}, idcard= #{idcard} where id = #{id}")
    int updateIdcard(Player player);

    @Update("update player set red_packet=red_packet+#{redPacketNum} where id=#{id}")
    int updateRedPacketById(@Param("id") int id,@Param("redPacketNum") int redPacketNum);

    @Update("update player set gold=gold+#{goldNum} where id=#{id}")
    int updateGoldById(@Param("id") int id,@Param("goldNum") int goldNum);

    @Update("update player set diamond=diamond+#{diamondNum} where id=#{id}")
    int updateDiamondById(@Param("id") int id,@Param("diamondNum") int diamondNum);

    @Update("update player set alipay=#{alipay,jdbcType=OTHER,typeHandler=com.wangpo.platform.mapper.MySqlJsonHandler} where id=#{id}")
    int updateAlipayById(@Param("id") int id,@Param("alipay") JSONObject alipay);
    
    @Update("update player set phone=#{phone} where id=#{id}")
    int updatePhoneById(@Param("id") int id,@Param("phone") String phone);
}
