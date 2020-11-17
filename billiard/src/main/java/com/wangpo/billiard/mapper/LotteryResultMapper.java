package com.wangpo.billiard.mapper;

import com.wangpo.billiard.bean.LotteryResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LotteryResultMapper {

    @Insert("insert into lottery_result(player_id,nick,chang,total_money,award_type,award_num,base,create_time) " +
            "values(#{playerId},#{nick},#{chang},#{totalMoney},#{awardType},#{awardNum},#{base},#{createTime})")
    int insertLotteryResult(LotteryResult lotteryResult);



}
