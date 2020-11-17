package com.wangpo.billiard.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.CommonUser;
import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.base.cms.MatchConfig;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.excel.BilliardFileCodeConfig;
import com.wangpo.base.excel.SystemConfig;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.bean.PlayerCue;
import com.wangpo.billiard.config.ConfigMgr;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.excel.ExcelHandler;
import com.wangpo.billiard.logic.item.ItemMgr;
import com.wangpo.billiard.logic.lottery.LotteryHandler;
import com.wangpo.billiard.logic.lucky.LuckyCueHandler;
import com.wangpo.billiard.logic.match.MatchHandler;
import com.wangpo.billiard.logic.match.MatchPool;
import com.wangpo.billiard.logic.player.PlayerHandler;
import com.wangpo.billiard.logic.role.RoleHandler;
import com.wangpo.billiard.logic.room.AbstractGame;
import com.wangpo.billiard.logic.room.GameHandler;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.BilliardService;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.room.GameMgr;
import com.wangpo.billiard.logic.room.GamePlayer;
import com.wangpo.billiard.logic.util.FightUtil;
import com.wangpo.billiard.service.BilliardCueService;
import com.wangpo.billiard.service.PlayerService;
import com.wangpo.billiard.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 网关服 -> 台球服
 * 请求处理 RPC Service
 */
@DubboService
@Component
@Slf4j
public class BilliardServiceImpl implements BilliardService {
    @DubboReference
    private BilliardPushService service ;
    @Resource
    PlayerHandler playerHandler;
    @Resource
    MatchHandler matchHandler;
    @Resource
	GameHandler gameHandler;
    @Resource
    LuckyCueHandler luckyCueHandler;
    @Resource
    GameMgr gameMgr;
    @Resource
    ExcelHandler excelHandler;
    @Resource
    PlayerMgr playerMgr;
    @Resource
    ItemMgr itemMgr;
    @Resource
    PlayerService playerService;
    @Resource
    RoleHandler roleHandler;
    @Resource
    RoleService roleService;
    @Resource
    BilliardCueService cueService;
    @Resource
    LotteryHandler lotteryHandler;
    @Resource
    ExcelMgr excelMgr;
    @Resource
    ConfigMgr configMgr;
    @Resource
    MatchPool matchPool;

    @Override
    public void logout(int uid) {
        try {
            Player player = playerMgr.getPlayerByID(uid);
            if( player != null && player.isOnline()) {
//                log.info("玩家离线，玩家ID:{}",player.getId());
                player.setOnline(false);


                //1,如果在游戏中，游戏结束则解散房间，2，记录离线时间
                //3,当前杆不再需要同步
                if(player.getRoomNo()>0) {
                    AbstractGame game = gameMgr.get(player.getRoomNo());
                    if( game!=null ) {
                    	if(game.isNoviceGuide()) {
                    		if(game.isGameOver()) {
                    			player.setRoomNo(0);
                    		}else {
                    			log.info("新手引导时删除房间:"+player.getId());
                        		gameHandler.noviceExitGame(player.getId());
                    		}
                    	}else if( game.isGameOver() ) {
                            player.setRoomNo(0);
                            gameMgr.dismiss(player.getId(),game);
                        } else {
                            gameMgr.noSync(game,player.getId());
                            //如果有玩家请求加倍，直接拒绝
                            if( game.isReqDouble() ) {
                                game.setReqDouble(false);
                                S2C s2c=new S2C();
                                s2c.setCid(Cmd.RESP_DOUBLE);
                                s2c.setCode(1);
                                for(GamePlayer gp:game.getPlayerList()) {
                                    s2c.setUid(gp.getId());
                                    service.push( s2c );
                                }
                            }
                        }
                    }
                }
                //同步球杆信息
                List<PlayerCue> cueList = player.getCueList();
                for (PlayerCue playerCue : cueList) {
                    cueService.updateCue(playerCue);
                }
                //如果玩家正在匹配，则取消匹配
                if( player.getMatchStatus()==1 && player.getMatchPoolId()>0) {
                    matchPool.cancelPlayer(player.getMatchPoolId(),player.getId());
                }

                player.setOfflineTime(System.currentTimeMillis());
                //提交在线玩家缓存删除
                playerMgr.submitOfflinePlayer(player.getId());
                //记录离线时间
                player.setLogoutTime(new Date());
                playerService.updatePlayer(player);
            }
        } catch (Exception e) {
            log.error("玩家退出登录处理异常：",e);
        }
    }

    @Override
    public S2C request(C2S c2s) {
        try {
            int cid = c2s.getCid();
//            log.info("台球服收到rpc，指令：{}",cid);
            switch (cid) {
                case Cmd.LOGIN:
                    return playerHandler.login(c2s);
                case Cmd.MATCH:
                    //请求匹配
                    return matchHandler.match(c2s);
                case Cmd.CANCEL_MATCH:
                    //请求匹配
                    return matchHandler.cancelMatch(c2s);
                case Cmd.INIT_ROOM:
                    //请求获取房间信息
                    return gameHandler.getRoomInfo(c2s);
                case Cmd.CUE_MOVE:
                case Cmd.SYNC_POS:
                    //移杆
                    return gameHandler.transfer(c2s);
                case Cmd.SNOOKER:
                    //台球进袋
                    return gameHandler.snooker(c2s);
                case Cmd.LAY_BALL:
                    //玩家摆球
                    return gameHandler.layBall(c2s);
                case Cmd.SLEEP_LAY_BALL:
                    return gameHandler.sleepLayBall(c2s);
                case Cmd.PLAYER_OPT:
                    //玩家击球
                    return gameHandler.playerOpt(c2s);
                case Cmd.DESK_INFO:
                    return gameHandler.syncDeskInfo(c2s);
                case Cmd.SYNC_POS2:
                    //台球完全静止后同步
                    return gameHandler.syncPos(c2s);
                case Cmd.REQ_DOUBLE:
                    return gameHandler.reqDouble(c2s);
                case Cmd.RESP_DOUBLE:
                    return gameHandler.respDouble(c2s);
                case Cmd.NEW_ROUND:
                    return gameHandler.newRound(c2s);
                case Cmd.EXIT_ROOM:
                    return gameHandler.exitRoom(c2s);
                case Cmd.REQ_CONFIG:
                    return excelHandler.reqConfig(c2s);
                case Cmd.MY_CUE:
                    return playerHandler.myCue(c2s);
                case Cmd.BUY_CUE:
                    return playerHandler.buyCue(c2s);
                case Cmd.SELL_CUE:
                    return playerHandler.sellCue(c2s);
                case Cmd.UPGRADE_CUE:
                    return playerHandler.upgradeCue(c2s);
                case Cmd.USE_CUE:
                    return playerHandler.useCue(c2s);
                case Cmd.DEFEND_CUE:
                    return playerHandler.defendCue(c2s);
                case Cmd.ALL_ITEM:
                    return playerHandler.getAllItem(c2s);
                case Cmd.BILLIARD_INFO:
                    return playerHandler.billiardInfo(c2s);
                case Cmd.GET_ROLE:
                    return roleHandler.getRole(c2s);
                case Cmd.USE_ROLE:
                    return roleHandler.useRole(c2s);
                case Cmd.LOTTERY:
                    return lotteryHandler.lottery(c2s);
                case Cmd.LUCKY_CUE:
                    return luckyCueHandler.luckyCue(c2s);
                case Cmd.LUCKY_CUE_GO:
                    return luckyCueHandler.go(c2s);
                case Cmd.EXIT_GAME:
                    return gameHandler.exitGame(c2s);
                case Cmd.EMOJI:
                    return gameHandler.emoji(c2s);
                case Cmd.NOCIVE_GUIDE_MATCH:
                    //新手引导请求匹配
                	log.info("新手匹配");
                    return matchHandler.nociveGuideMatch(c2s);
                case Cmd.NOCIVE_GUIDE_LOTTERY:
                	log.info("新手抽奖");
                    return lotteryHandler.nociveGuideLottery(c2s);
                default:
                    break;
            }
            return null;
        } catch (Exception e) {
            log.error("处理台球rpc异常，cmd：{}，错误：{}",c2s.getCid(),e);
            return null;
        } finally {

        }
    }

    @Override
    public void syncUser(CommonUser user) {
        Player player = playerMgr.getPlayerByID(user.getId());
        if( player !=null ) {
//            log.info("平台服主动同步用户信息");
            player.setUser(user);
        }
    }

    @Override
    public void syncItem(int uid, int modelId, int num, GameEventEnum gameEventEnum) {
        Player player = playerMgr.getPlayerByID(uid);
        if( player !=null ) {
//            log.info("平台服主动同步道具信息");
            itemMgr.addItem(player,modelId,num, gameEventEnum);
        } else {
            player = playerService.selectPlayerByID(uid);
            if (player != null) {
                itemMgr.addItem(player,modelId,num,gameEventEnum);
            }
        }
    }

    @Override
    public void syncSystemConfig(Map<Integer, SystemConfig> map) {
        ExcelMgr.SYSTEM_CONFIG_MAP = map;
    }

    @Override
    public void updateRole(int uid, int exp) {
        Player player = playerMgr.getPlayerByID(uid);
        if( player !=null ) {
//            log.info("平台服主动同步角色信息");
            roleHandler.updateRole(player,exp);
        }
    }

    @Override
    public void addGameTimes(int id, int chang) {
        Player player = playerMgr.getPlayerByID(id);
        if (player != null) {
            FightUtil.modifyGameTimes(player,chang, 1,1,0);
        }
    }

    @Override
    public Map<String, Integer> getGameData() {
        Map<String, Integer> map = new TreeMap<>();
        JSONObject data = gameMgr.data();
        Map<Integer, BilliardFileCodeConfig> fileCodeMap = excelMgr.getFileCodeMap();
        fileCodeMap.forEach((chang,fileCode) -> {
            if (fileCode.getPlayType() != null && fileCode.getMoneyType() != null && fileCode.getGrade() != null) {
                if (data.containsKey(String.valueOf(chang))) {
                    map.put(fileCode.getPlayType() + fileCode.getMoneyType() + fileCode.getGrade(),(data.getInteger(String.valueOf(chang))));
                } else {
                    map.put(fileCode.getPlayType() + fileCode.getMoneyType() + fileCode.getGrade(),0);
                }
            }
        });
        return map;
    }

    @Override
    public void modifyMatchConfig(int type, MatchConfig matchConfig) {
        configMgr.modifyMatchConfig(type,matchConfig);
    }

    @Override
    public void modifyLotteryConfig(int type, CmsLotteryConfig cmsLotteryConfig) {
        configMgr.modifyLotteryConfig(type,cmsLotteryConfig);
    }

    @Override
    public void modifyChangConfig(int type, CmsChangConfig cmsChangConfig) {
        configMgr.modifyChangConfig(type,cmsChangConfig);
    }


}
