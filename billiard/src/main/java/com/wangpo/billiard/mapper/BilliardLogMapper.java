package com.wangpo.billiard.mapper;

import com.wangpo.billiard.bean.BilliardLog;
import com.wangpo.billiard.framework.StrategyManager;
import com.wangpo.billiard.framework.TableSplitRule;
import com.wangpo.billiard.framework.TableSplitTarget;
import org.apache.ibatis.annotations.*;

@Mapper
@TableSplitTarget(rules={@TableSplitRule(tableName="billiard_log",paramName="gameTime",targetName= StrategyManager.FORMAT_YYYYMM)})
public interface BilliardLogMapper {
	@Insert("insert into billiard_log(room_no,chang,player1,player2,player3,money_type,total_cue,double_times,ai_money,fee,game_time) " +
			"values(#{roomNo},#{chang},#{player1},#{player2},#{player3},#{moneyType},#{totalCue},#{doubleTimes},#{aiMoney},#{fee},#{gameTime})")
	int insertBilliardLog(BilliardLog billiardLog);
}
