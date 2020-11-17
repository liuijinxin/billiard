package com.wangpo.platform.logic.member;

import com.wangpo.base.kits.DateKit;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.GlobalEnum;
import com.wangpo.base.excel.GlobalConfig;
import com.wangpo.base.excel.MemberConfig;
import com.wangpo.base.kits.FormatKit;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.bean.PlayerVip;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.MemberService;
import com.wangpo.platform.service.PlayerMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class MemberHandler {
    @Resource
    BaseExcelMgr excelMgr;
    @Resource
    PlayerMgr playerMgr;
    @Resource
    ItemMgr itemMgr;
    @DubboReference
    private BilliardPushService billiardPushService;
    @Resource
    MemberService memberService;
    @Resource
    BaseExcelMgr baseExcelMgr;

    /**
     * 获取当前玩家会员等级
     * @param player 玩家
     */
    public void getVIPLevel(Player player) {
        //获取玩家的会员信息
        PlayerVip playerVip = memberService.selectMemberByPlayerId(player.getId());
        if (playerVip == null) {
            //初始vip经验
            playerVip = new PlayerVip();
            playerVip.setPlayerId(player.getId());
            playerVip.setDayGift(0);
            playerVip.setLevelGift(0);
            playerVip.setDeclineTime(new Date());
            GlobalConfig vip = BaseExcelMgr.GLOBAL_MAP.get(GlobalEnum.VIP.code);
            if (vip != null) {
                playerVip.setPoints(Integer.parseInt(vip.getNum()));
            } else {
                playerVip.setPoints(0);
                playerVip.setLevel(0);
            }
            playerVip.setToday(FormatKit.today10());
            memberService.insertMember(playerVip);
        }
        player.setPlayerVip(playerVip);
        player.setVip(getVipLv(player));

    }

    /**
     * 获取玩家会员信息
     */
    public S2C getVIPInfo(C2S c2s){
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.MEMBER_INFO);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setCode(1);
            return s2c;
        }
        PlayerVip playerVip = player.getPlayerVip();
        PlatFormProto.S2C_Member_info.Builder builder = PlatFormProto.S2C_Member_info.newBuilder();
        builder.setPoints(playerVip.getPoints());
        builder.setDayGift(playerVip.getDayGift());
        builder.setLevelGift(playerVip.getLevelGift());
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 修改玩家会员点数
     * @param player 玩家
     * @param num 数量
     */
    public void modifyPoint(Player player, int num) {
        int vipLevel = player.getVip();
        Map<Integer, MemberConfig> MemberConfigMap = BaseExcelMgr.MEMBER_MAP;
        //充值后的处理
        int points = player.getPlayerVip().getPoints() + num;
        int afterLevel = vipLevel;
        for (MemberConfig MemberConfig : MemberConfigMap.values()) {
            if (points >= MemberConfig.getPoint()) {
                afterLevel = MemberConfig.getId();
            } else if (points < MemberConfig.getPoint()) {
                break;
            }
        }
        //修改会员信息
        player.getPlayerVip().setPoints(points);
        player.getPlayerVip().setLevel(afterLevel);
        player.setVip(afterLevel);
        pushVipPoints(player);

        memberService.updateMember(player.getPlayerVip());
    }

    /**
     * 通知客户端修改会员点数
     * @param player 玩家
     */
    private void pushVipPoints(Player player) {
        PlatFormProto.S2C_Member_Upgrade.Builder builder = PlatFormProto.S2C_Member_Upgrade.newBuilder();
        builder.setPoints(player.getPlayerVip().getPoints());
        S2C s2c = new S2C();
        s2c.setCid(Cmd.MEMBER_UPGRADE);
        s2c.setUid(player.getId());
        s2c.setBody(builder.build().toByteArray());
        billiardPushService.push(s2c);
    }

    /**
     * 获取会员等级
     * @param player 玩家
     * @return 会员等级
     */
    private int getVipLv(Player player) {
        Map<Integer, MemberConfig> MemberConfigMap = BaseExcelMgr.MEMBER_MAP;
        //充值后的处理
        int points = player.getPlayerVip().getPoints()  ;
        int lv = 0;
        for (MemberConfig MemberConfig : MemberConfigMap.values()) {
            if (points >= MemberConfig.getPoint()) {
                lv = MemberConfig.getId();
            } else if (points < MemberConfig.getPoint()) {
                break;
            }
        }
        player.getPlayerVip().setLevel(lv);
        return lv;
    }

    /**
     * 领取会员等级奖励
     */
    public S2C getLevelReward(C2S c2s) throws Exception {
        PlatFormProto.C2S_Member_Level MemberConfigAward = PlatFormProto.C2S_Member_Level.parseFrom(c2s.getBody());
        int level = MemberConfigAward.getLevel();
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.LEVEL_AWARD);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setCode(1);
            return s2c;
        }
        if (level > player.getVip()) {
            log.error("未达到会员等级");
            s2c.setCode(3);
            return s2c;
        }
        PlayerVip playerVip = player.getPlayerVip();
        int levelGift = playerVip.getLevelGift();
        //唯一判断玩家会员奖励是否已领取
        if ((levelGift >> level & 1) == 1) {
            log.error("会员奖励已领取");
            s2c.setCode(2);
            return s2c;
        }
        //将该等级的领取状态置为已领取
        int nowLevelGift = levelGift + (1 << level);
        playerVip.setLevelGift(nowLevelGift);
        PlatFormProto.S2C_Member_Level.Builder builder = PlatFormProto.S2C_Member_Level.newBuilder();
        builder.setLevelGift(nowLevelGift);
        MemberConfig MemberConfig = BaseExcelMgr.MEMBER_MAP.get(level);
        if (MemberConfig == null) {
            log.error("找不到会员等级数据");
            s2c.setCode(4);
            return s2c;
        }
        for (Map.Entry<String, Object> entry : MemberConfig.getUpgradeRewards().entrySet()) {
            int type = Integer.parseInt(entry.getKey());
            int price = Integer.parseInt(String.valueOf(entry.getValue()));
            itemMgr.addItem(player,type,price, GameEventEnum.VIP_REWARD);
            PlatFormProto.Award.Builder award = PlatFormProto.Award.newBuilder();
            award.setId(type);
            award.setNum(price);
            builder.addAward(award);
        }
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 领取会员奖励
     */
    public S2C getDayReward(C2S c2s) throws Exception {
        PlatFormProto.C2S_Member_Award MemberConfigAward = PlatFormProto.C2S_Member_Award.parseFrom(c2s.getBody());
        int level = MemberConfigAward.getLevel();
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.MEMBER_AWARD);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setCode(1);
            return s2c;
        }
        if (level > player.getVip()) {
            log.error("未达到会员等级");
            s2c.setCode(3);
            return s2c;
        }
        if (level == player.getVip()) {
            if (player.getPlayerVip().getDayGift() == 1) {
                log.error("奖励已领取");
                s2c.setCode(2);
                return s2c;
            }
            player.getPlayerVip().setDayGift(1);
            PlatFormProto.S2C_Member_Award.Builder builder = PlatFormProto.S2C_Member_Award.newBuilder();
            builder.setDayGift(1);
            MemberConfig MemberConfig = BaseExcelMgr.MEMBER_MAP.get(level);
            if (MemberConfig == null) {
                log.error("找不到会员等级数据");
                s2c.setCode(4);
                return s2c;
            }
            for (Map.Entry<String, Object> entry : MemberConfig.getDayRewards().entrySet()) {
                int type = Integer.parseInt(entry.getKey());
                int price = Integer.parseInt(String.valueOf(entry.getValue()));
                itemMgr.addItem(player,type,price, GameEventEnum.VIP_REWARD);
                PlatFormProto.Award.Builder award = PlatFormProto.Award.newBuilder();
                award.setId(type);
                award.setNum(price);
                builder.addAward(award);
            }
            s2c.setBody(builder.build().toByteArray());
        }
        return s2c;
    }


    /**
     * vip 每日衰退
     * @param player 玩家
     */
    public void declineVip(Player player) {
        PlayerVip playerVip = player.getPlayerVip();
        if(playerVip!=null && playerVip.getPoints()>0) {
            if( playerVip.getDeclineTime()==null ) {
                playerVip.setDeclineTime(new Date());
            }
            int t =  (int)DateKit.between(playerVip.getDeclineTime(),new Date());
            if( t>0) {
                MemberConfig config = BaseExcelMgr.MEMBER_MAP.get(getVipLv(player));
                if( config != null ) {
//                    log.info("玩家{} vip 衰退：{}",player.getId(),config.getDecline()*t);
                    int after = playerVip.getPoints()-config.getDecline()*t;
                    after = Math.max(after, 0);
                    playerVip.setPoints(after);
                    player.setVip(getVipLv(player));
                    pushVipPoints(player);
                    memberService.updateMember(playerVip);
                }
            }
        }
    }

    /**
     * 每日衰退
     */
    public void declineVip() {
        //重置缓存里的任务
        Map<Integer, Player> playerMap = playerMgr.getIdMap();
        if (playerMap != null) {
            for (Player player : playerMap.values()) {
                //重置任务进度
                declineVip(player);
            }
        }
    }
}