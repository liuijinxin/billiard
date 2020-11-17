package com.wangpo.billiard.logic.cue;

import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.billiard.bean.Player;
import com.wangpo.base.excel.BilliardCueConfig;
import com.wangpo.base.excel.BilliardCueTypeConfig;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.bean.PlayerCue;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.player.PlayerHandler;
import com.wangpo.billiard.logic.room.AbstractGame;
import com.wangpo.billiard.logic.room.GameMgr;
import com.wangpo.billiard.logic.room.GamePlayer;
import com.wangpo.billiard.service.BilliardCueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 球杆系统
 * 球杆升级，维护等逻辑在这里处理
 */
@Component
public class CueMgr {
    private static final Logger log = LoggerFactory.getLogger(CueMgr.class);
    @Resource
    BilliardCueService cueService;
    @Resource
    ExcelMgr excelMgr;
    @Resource
    PlayerHandler playerHandler;
    @Resource
    BilliardPushService pushService;
    @Resource
    GameMgr roomMgr;


    public List<PlayerCue> getCueList(int playerId) {
        return cueService.selectCueByPlayerId(playerId);
    }

    /**
     * 我的球杆列表
     * @param player 玩家
     * @return s2c
     */
    public S2C myCue(Player player, S2C s2c){
        //从内存中读取玩家的球杆
        List<PlayerCue> cueList = player.getCueList();
        for (PlayerCue playerCue : cueList) {
            //玩家球杆维护时间到期，置为0
            if (playerCue.getDefendDay() < System.currentTimeMillis()) {
                playerCue.setDefendDay(0);
                //cueService.updateCueDefend(playerCue);
            }
        }
        //将球杆信息发送给客户端
        BilliardProto.S2C_MyCue.Builder builder = BilliardProto.S2C_MyCue.newBuilder();
        if (cueList.size() > 0) {
            for (PlayerCue playerCue : cueList) {
                builder.addPlayerCue(playerCue.toProto());
            }
        }
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 购买球杆
     * @param player 玩家
     * @return s2c
     */
    public S2C buyCue(Player player, int cueId, S2C s2c){
        //根据球杆id找到对应的球杆配置
        BilliardCueTypeConfig cueTypeConfig = excelMgr.getPlayerCueType(cueId);
        if (cueTypeConfig == null) {
            log.error("球杆不存在");
            s2c.setCode(2);
            return s2c;
        }
        //扣除玩家金币，钻石
        boolean flag = playerHandler.usePlayerItem(player,s2c, cueTypeConfig.getBuyType(), -cueTypeConfig.getBuyPrice(),true, GameEventEnum.BUY_CUE);
        if (flag) {
	        return s2c;
        }
        //球杆入库
        PlayerCue playerCue = new PlayerCue();
        playerCue.setCueID(cueId);
        playerCue.setPlayerID(player.getId());
        playerCue.setStar(cueTypeConfig.getStar());
        playerCue.setIsUse(0);
        playerCue.setDefendTimes(30);
        playerCue.setCreateTime(new Date());
        playerCue.setUpdateTime(new Date());
        cueService.addCue(playerCue);
        //购买s级以上球杆，推送游戏公告
        if ("S".equals(cueTypeConfig.getQuality()) || "SS".equals(cueTypeConfig.getQuality()) || "SSS".equals(cueTypeConfig.getQuality())) {
            String msg = String.format("{}玩家购了{}球杆，快来膜拜",player.getUser().getNick(), cueTypeConfig.getQuality());
            playerHandler.notice(msg,player.getId());
        }
        //球杆加入玩家球杆缓存中
        player.getCueList().add(playerCue);
        //球杆信息发送给客户端
        BilliardProto.S2C_BuyCue.Builder builder = BilliardProto.S2C_BuyCue.newBuilder();
        builder.setPlayerCue(playerCue.toProto());
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 出售球杆
     * @param player 玩家
     * @return s2c
     */
    public S2C sellCue(Player player, int cueId, S2C s2c){
        //查找玩家球杆
        List<PlayerCue> cueList = player.getCueList();
        PlayerCue playerCue = null;
        for (PlayerCue playerCue1 : cueList ){
            if (playerCue1.getId() == cueId) {
                playerCue = playerCue1;
                break;
            }
        }
        if (playerCue == null) {
            log.error("球杆不存在");
            s2c.setCode(2);
            return s2c;
        }
        if (playerCue.getCueID()/100 == 1 || playerCue.getIsUse() == 1) {
            log.error("默认球杆禁止售出");
            s2c.setCode(7);
            return s2c;
        }
        //找到对应的球杆属性cue
        BilliardCueConfig cueConfig = excelMgr.getPlayerCue(playerCue.getCueID());
        if (cueConfig == null) {
            log.error("球杆配置不存在");
            s2c.setCode(2);
            return s2c;
        }
        //增加玩家的金币，钻石
        boolean flag = playerHandler.usePlayerItem(player,s2c, cueConfig.getSellPayType(), cueConfig.getSellPayPrice(),false,GameEventEnum.SELL_CUE);
        if (flag) {
	        return s2c;
        }

        //从玩家球杆中移除
        player.getCueList().remove(playerCue);
        //从数据库删除球杆信息
        cueService.deleteCueById(cueId);
        //通知客户端，删除球杆
        BilliardProto.S2C_SellCue.Builder builder = BilliardProto.S2C_SellCue.newBuilder();
        builder.setId(cueId);
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 升级球杆
     * @param player 玩家
     * @return s2c
     */
    public S2C upgradeCue(Player player, int cueId, S2C s2c){
        //通过球杆id在玩家球杆中找到对应球杆
        List<PlayerCue> cueList = player.getCueList();
        PlayerCue playerCue = null;
        for (PlayerCue playerCue1 : cueList) {
            if (cueId == playerCue1.getId()) {
                playerCue = playerCue1;
                break;
            }
        }
        if (playerCue == null) {
            log.error("球杆不存在");
            s2c.setCode(2);
            return s2c;
        }
        //获取球杆的配置信息
        BilliardCueConfig cueConfig =excelMgr.getPlayerCue(playerCue.getCueID());
        //获取球杆下一个等级的配置信息
        BilliardCueConfig cueConfig1 = excelMgr.getPlayerCue(playerCue.getCueID() + 1);
        //下一个等级球杆信息为空，则球杆已升到满级
        if (cueConfig1 == null) {
            log.error("球杆已无法升级");
            s2c.setCode(5);
            return s2c;
        }
        //扣除玩家对应的金币，钻石，道具
        boolean flag = playerHandler.usePlayerItem(player,s2c, cueConfig.getUpgradePayType(),-cueConfig.getUpgradePayPrice(),true,GameEventEnum.UPGRADE_CUE);
        if (flag) {
	        return s2c;
        }

        //玩家升级球杆，改变球杆id
        playerCue.setCueID(cueConfig1.getId());
        playerCue.setStar(cueConfig1.getStar());
        //cueService.upgradeCue(playerCue);

        //若球杆等级大于六星，发送游戏公告
        if (playerCue.getStar() > 6) {
            String msg = String.format("{}玩家成功将{}球杆升级到{}星，实力大增",player.getUser().getNick(), cueConfig.getName(),playerCue.getStar());
            playerHandler.notice(msg,player.getId());
        }
        //球杆信息发送给客户端
        BilliardProto.S2C_UpgradeCue.Builder builder = BilliardProto.S2C_UpgradeCue.newBuilder();
        builder.setPlayerCue(playerCue.toProto());
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 使用球杆
     * @param player 玩家
     */
    public S2C useCue(Player player, int cueID, S2C s2c) {
        //通过球杆id在玩家球杆中找到对应球杆
        PlayerCue playerCue = null;
        List<PlayerCue> cueList = player.getCueList();
        for (PlayerCue playerCue1 : cueList) {
            if (playerCue1.getId() == cueID) {
                playerCue = playerCue1;
                break;
            }
        }
        if (playerCue == null) {
            log.error("玩家未拥有该球杆");
            s2c.setCode(2);
            return s2c;
        }
        //将球杆置为使用中
        playerCue.setIsUse(1);
        //遍历玩家球杆，将之前已使用的球杆置为未使用
        for (PlayerCue cue : cueList) {
            if (cue.getId() == playerCue.getId()) {
                cue.setIsUse(1);
            }
            if (cue.getIsUse() == 1 && cue.getId() != playerCue.getId()) {
                cue.setIsUse(0);
            }
        }
        //判断玩家是否在游戏中，游戏中直接推送更换球杆
        if( player.getRoomNo()>0) {
            AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
            if( gameRoom != null ) {
                S2C s2c2 = new S2C();
                s2c2.setCid(Cmd.CHANGE_CUE);
                s2c2.setBody(BilliardProto.S2C_ChangeCue.newBuilder()
                        .setCueId(playerCue.getCueID())
                        .setPlayerId(player.getId())
                        .build().toByteArray());
                for(GamePlayer gp:gameRoom.getPlayerList()) {
                    if(gp.getId()==player.getId()) {
	                    gp.setCueId(playerCue.getCueID());
                    }
                    s2c2.setUid(gp.getId());
                    pushService.push(s2c2);
                }
            }

        }
        BilliardProto.S2C_UseCue.Builder builder = BilliardProto.S2C_UseCue.newBuilder();
        builder.setId(cueID);
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 维护球杆
     * @param player 玩家
     * @param cueId 球杆id
     * @param defendType 维护的类型（1、维护30次，2、维护3天，3、维护7天，4、维护30天，5、维护365天）
     * @return s2c
     */
    public S2C defendCue(Player player,int cueId,int defendType,S2C s2c){
        List<PlayerCue> cueList = player.getCueList();
        //通过球杆id找到玩家的球杆
        PlayerCue playerCue = null;
        if (cueList.size() > 0){
            for (PlayerCue playerCue1 : cueList) {
                if (playerCue1.getId() == cueId) {
                    playerCue = playerCue1;
                    break;
                }
            }
        }
        if (playerCue == null) {
            log.error("球杆不存在");
            s2c.setCode(2);
            return s2c;
        }
        int cueTypeId = playerCue.getCueID() / 100 * 100 + 1;
        BilliardCueTypeConfig cueType = excelMgr.getPlayerCueType(cueTypeId);
        if (cueType == null) {
            log.error("球杆配置不存在");
            s2c.setCode(2);
            return s2c;
        }
        //维护次数
        int defendTimes = playerCue.getDefendTimes();
        //维护天数
        long defendDay = playerCue.getDefendDay();
        //判断维护类型,扣除玩家金币
        switch (defendType) {
            case 1: { //维护30次
                boolean flag = playerHandler.usePlayerItem(player,s2c,cueType.getDefend_30_times_type(),-cueType.getDefend_30_times_price(),true,GameEventEnum.DEFEND_CUE);
                if (flag) {
	                return s2c;
                }
                playerCue.setDefendTimes(defendTimes + 30);
                break;
            }
            case 2: { //维护3天
                boolean flag = playerHandler.usePlayerItem(player,s2c,cueType.getDefend_3_days_type(),-cueType.getDefend_3_days_price(),true,GameEventEnum.DEFEND_CUE);
                if (flag) {
	                return s2c;
                }
                long day = modifyDefendDay(defendDay, 3);
                playerCue.setDefendDay(day);
                break;
            }
            case 3: { //维护7天
                boolean flag = playerHandler.usePlayerItem(player,s2c,cueType.getDefend_7_days_type(),-cueType.getDefend_7_days_price(),true,GameEventEnum.DEFEND_CUE);
                if (flag) {
	                return s2c;
                }
                long day = modifyDefendDay(defendDay, 7);
                playerCue.setDefendDay(day);
                break;
            }
            case 4: { //维护30天
                boolean flag = playerHandler.usePlayerItem(player,s2c,cueType.getDefend_30_days_type(),-cueType.getDefend_30_days_price(),true,GameEventEnum.DEFEND_CUE);
                if (flag) {
	                return s2c;
                }
                long day = modifyDefendDay(defendDay, 30);
                playerCue.setDefendDay(day);
                break;
            }
            case 5: { //维护365天
                boolean flag = playerHandler.usePlayerItem(player,s2c,cueType.getDefend_365_days_type(),-cueType.getDefend_365_days_price(),true,GameEventEnum.DEFEND_CUE);
                if (flag) {
	                return s2c;
                }
                long day = modifyDefendDay(defendDay, 365);
                playerCue.setDefendDay(day);
                break;
            }
            default:
                break;
        }
        //将更新后的球杆信息发送给客户端
        BilliardProto.S2C_DefendCue.Builder builder = BilliardProto.S2C_DefendCue.newBuilder();
        builder.setPlayerCue(playerCue.toProto());
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 修改玩家维护天数
     */
    private long modifyDefendDay(long defendDay, int day) {
        long now = System.currentTimeMillis();
        long defendTime = day * 24 * 60 * 60 * 1000L;
        if (defendDay < now) {
            return now + defendTime;
        } else {
            return defendDay + defendTime;
        }
    }

    /**
     * 修改球杆维护情况
     * @param player 玩家
     */
    public void modifyCueDefend(Player player){
        //从玩家球杆列表中找到使用中的球杆
        List<PlayerCue> collect = player.getCueList().stream().filter(playerCue -> playerCue.getIsUse() == 1).collect(Collectors.toList());
        if( collect!=null && collect.size()>0) {
            PlayerCue playerCue = collect.get(0);
            if (playerCue != null) {
                //判断球杆维护时间是否过期
                if (playerCue.getDefendDay() < System.currentTimeMillis()) {
                    int defendTimes = playerCue.getDefendTimes();
                    //判断球杆使用次数是否已用完，没有则扣减
                    if (defendTimes > 0) {
                        playerCue.setDefendTimes(defendTimes - 1);
                    }
                    //将损坏情况推送给客户端
                    S2C s2c = new S2C();
                    s2c.setCid(Cmd.DAMAGE_CUE);
                    s2c.setUid(player.getId());
                    BilliardProto.S2C_DefendCue.Builder builder = BilliardProto.S2C_DefendCue.newBuilder();
                    builder.setPlayerCue(playerCue.toProto());
                    s2c.setBody(builder.build().toByteArray());
                    pushService.push(s2c);
                }
            }
        }
    }

    /**
     * 添加球杆
     * @param player 玩家
     * @param num
     */
    public void addCue(Player player, int cueId, int num){
        for (int i = 0; i < num; i++) {
            BilliardCueTypeConfig cueTypeConfig = excelMgr.getPlayerCueType(cueId);
            //球杆入库
            PlayerCue playerCue = new PlayerCue();
            playerCue.setCueID(cueId);
            playerCue.setPlayerID(player.getId());
            playerCue.setStar(cueTypeConfig.getStar());
            playerCue.setIsUse(0);
            playerCue.setDefendTimes(30);
            playerCue.setCreateTime(new Date());
            playerCue.setUpdateTime(new Date());
            cueService.addCue(playerCue);
            //球杆加入玩家球杆缓存中
            player.getCueList().add(playerCue);
        }
    }

}
