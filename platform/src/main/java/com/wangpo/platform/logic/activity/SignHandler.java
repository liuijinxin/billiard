package com.wangpo.platform.logic.activity;

import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.base.bean.PlatFormProto.Award;
import com.wangpo.base.bean.PlatFormProto.S2C_SignIfo;
import com.wangpo.base.bean.PlatFormProto.signAward;
import com.wangpo.base.bean.TaskData;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.task.BilliardTaskType;
import com.wangpo.base.excel.EveryDaySign;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.logic.task.TaskHandler;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

@Component
@Slf4j
public class SignHandler {
    @Resource
    PlayerMgr playerMgr;
    @Resource
    BaseExcelMgr baseExcelMgr;
	@Resource
	ItemMgr itemMgr;
	@Resource
    TaskHandler taskHandler;

    public S2C sign(C2S c2s) throws Exception {
//        PlatFormProto.C2S_Sign proto = PlatFormProto.C2S_Sign.parseFrom(c2s.getBody());
//        int signDay = proto.getSignDay();
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.SIGN);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setCode(1);
            return s2c;
        }
        if (player.getSignStatus() == 1) {
            log.error("玩家已签到");
            s2c.setCode(2);
            return s2c;
        }
        int day = player.getSignDay();
//        if (day + 1 != signDay) {
//            log.error("未达到签到要求，天数：{}",signDay);
//            s2c.setCode(3);
//            return s2c;
//        }
        if (day < 10) {
            player.setSignDay(day + 1);
        }
        EveryDaySign rewaed = baseExcelMgr.getSign().get(player.getSignDay());
        if(rewaed == null) {
          log.error("配置未找到当前签到天数：{}",day);
          s2c.setCode(3);
          return s2c;
        }
        player.setSignStatus(1);
        if(rewaed.getRepeat() == 1) {
        	player.setSignDay(0);
        }
        Map<String,Object> rewardMap = rewaed.getItemNums();
        PlatFormProto.S2C_Sign.Builder s2cSign =  PlatFormProto.S2C_Sign.newBuilder();
        if(rewaed.getGold() > 0) {
        	Award.Builder awardBuilder = Award.newBuilder();
        	awardBuilder.setId(1);
        	awardBuilder.setNum(rewaed.getGold());
        	s2cSign.addAwards(awardBuilder);
        	itemMgr.addItem(player,1,rewaed.getGold(), GameEventEnum.SIGN_AWARD);
        }
        if(rewaed.getDiamond() > 0) {
        	Award.Builder awardBuilder = Award.newBuilder();
        	awardBuilder.setId(2);
        	awardBuilder.setNum(rewaed.getDiamond());
        	s2cSign.addAwards(awardBuilder);
        	itemMgr.addItem(player,2,rewaed.getDiamond(), GameEventEnum.SIGN_AWARD);
        }
        //完成任务
        TaskData taskData = new TaskData();
        taskData.setTimes(1);
        taskData.setTaskType(BilliardTaskType.SIGN.code);
        taskHandler.finishTask(uid, 3, taskData);
        PlatFormProto.S2C_Award.Builder builder = PlatFormProto.S2C_Award.newBuilder();
        for (Map.Entry<String, Object> entry : rewardMap.entrySet()) {
			int type = Integer.parseInt(entry.getKey());
			int price = Integer.parseInt(entry.getValue().toString());
			//桌球服，道具让桌球服处理
			itemMgr.addItem(player,type,price, GameEventEnum.SIGN_AWARD);
			PlatFormProto.Award.Builder award = PlatFormProto.Award.newBuilder();
			award.setId(type).setNum(price);
			builder.addAwards(award.build());
			s2cSign.addAwards(award);
		}
        s2c.setBody(s2cSign.build().toByteArray());
        return s2c;
    }
    
    /**
     * 获取签到信息
     * @param c2s
     * @return
     */
    public S2C signIfo(C2S c2s){
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.SIGN_INFO);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        S2C_SignIfo.Builder signInfoBuilder = S2C_SignIfo.newBuilder();
        if(player.getSignStatus() == 0) {
        	signInfoBuilder.setSignStatus(true);
        }else {
        	 signInfoBuilder.setSignStatus(false);
        }
        signInfoBuilder.setSignDayCount(player.getSignDay());
        Map<Integer, EveryDaySign> signConfig = baseExcelMgr.getSign();
        for(Entry<Integer, EveryDaySign> entry : signConfig.entrySet()) {
        	EveryDaySign everyDaySign = entry.getValue();
        	signAward.Builder signAwardBuilder = signAward.newBuilder();
        	signAwardBuilder.setSignDay(everyDaySign.getDayNum());
        	signAwardBuilder.setGold(everyDaySign.getGold());
        	signAwardBuilder.setDiamond(everyDaySign.getDiamond());
        	Map<String,Object> rewardMap = everyDaySign.getItemNums();
            for(Entry<String,Object> rewardEntry : rewardMap.entrySet()) {
            	Award.Builder awardBuilder = Award.newBuilder();
            	awardBuilder.setId(Integer.valueOf(rewardEntry.getKey()));
            	awardBuilder.setNum(Integer.parseInt(rewardEntry.getValue().toString()));
            	signAwardBuilder.addAwards(awardBuilder);
            }
            signInfoBuilder.addAwards(signAwardBuilder);
        }
        s2c.setBody(signInfoBuilder.build().toByteArray());
        log.info("请求签到信息");
        return s2c;
    }


}
