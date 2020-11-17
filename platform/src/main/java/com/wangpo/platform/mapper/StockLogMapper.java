package com.wangpo.platform.mapper;

import com.wangpo.platform.bean.LoginLog;
import com.wangpo.platform.bean.StockLog;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StockLogMapper {

    @Select("select id,log_day,stock_gold,stock_diamond,stock_red_packet,active_gold,active_diamond,active_red_packet from stock_log where log_day = #{logDay}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "logDay", column = "log_day"),
            @Result(property = "stockGold", column = "stock_gold"),
            @Result(property = "stockDiamond", column = "stock_diamond"),
            @Result(property = "stockRedPacket", column = "stock_red_packet"),
            @Result(property = "activeGold", column = "active_gold"),
            @Result(property = "activeDiamond", column = "active_diamond"),
            @Result(property = "activeRedPacket", column = "active_red_packet"),
    })
    StockLog selectStockLogByLogDay(@Param("logDay") String logDay);


    @Insert("insert into stock_log set log_day = #{logDay},stock_gold = #{stockGold},stock_diamond = #{stockDiamond},stock_red_packet = #{stockRedPacket},active_gold = #{activeGold},active_diamond = #{activeDiamond},active_red_packet = #{activeRedPacket}  ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertStockLog(StockLog stockLog);

    @Select("SELECT SUM(gold) AS stock_gold, SUM(diamond) AS stock_diamond , SUM(red_packet) AS stock_red_packet FROM player")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "logDay", column = "log_day"),
            @Result(property = "stockGold", column = "stock_gold"),
            @Result(property = "stockDiamond", column = "stock_diamond"),
            @Result(property = "stockRedPacket", column = "stock_red_packet"),
            @Result(property = "activeGold", column = "active_gold"),
            @Result(property = "activeDiamond", column = "active_diamond"),
            @Result(property = "activeRedPacket", column = "active_red_packet"),
    })
    StockLog sumStock(@Param("logDay") String logDay);

    @Select("SELECT SUM(gold) AS active_gold, SUM(diamond) AS active_diamond , SUM(red_packet) AS active_red_packet FROM player where date_sub(curdate(), interval 7 day) <= date(login_time)")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "logDay", column = "log_day"),
            @Result(property = "stockGold", column = "stock_gold"),
            @Result(property = "stockDiamond", column = "stock_diamond"),
            @Result(property = "stockRedPacket", column = "stock_red_packet"),
            @Result(property = "activeGold", column = "active_gold"),
            @Result(property = "activeDiamond", column = "active_diamond"),
            @Result(property = "activeRedPacket", column = "active_red_packet"),
    })
    StockLog sumActive(@Param("logDay") String logDay);

}
