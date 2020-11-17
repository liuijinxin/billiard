package com.wangpo.billiard.logic.match;

import com.wangpo.base.excel.SystemConfig;
import com.wangpo.billiard.bean.Player;
import com.wangpo.base.bean.CommonUser;
import com.wangpo.billiard.consts.InitValue;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
public class RobotMgr {
    private List<Player> playerList = new CopyOnWriteArrayList<>();
    private static final Map<Integer,Player> map = new ConcurrentHashMap<>();
    private int currentId = -1;

    private static final Random r = new Random();
    @Resource
    PlayerService playerService;
    @Resource
    ExcelMgr excelMgr;

    String prefix = null;

    public void init() {
        //机器人头像前缀，系统配置。
        String prefix = "http://billiards.nbrfwl.cn:8066/pic/";
        for(SystemConfig sc:ExcelMgr.SYSTEM_CONFIG_MAP.values()) {
            if("robot_head_url".equals(sc.getSystemKey())) {
                prefix = sc.getSystemValue();
                break;
            }
        }

        playerList = playerService.selectRobot();
        log.info("游戏启动，初始化机器人数量：{}",playerList.size());
        if(playerList.size()>0) {
            for(Player player:playerList) {
                player.setHead(prefix+player.getHead());
                map.put(player.getId(),player);
                if(player.getId()<=currentId) {
                    currentId = player.getId()-1;
                }
            }
        }
    }

    //机器人取名随机
    private void increment() {
        Player player = new Player();
        CommonUser user = new CommonUser();

        user.setHead(prefix+"D"+Math.abs(currentId)+".jpg");
        user.setNick(RandomName.randomName(true,r.nextInt(2)+2));
        user.setGold(InitValue.ROBOT_GOLD);
        user.setDiamond(InitValue.ROBOT_DIAMOND);
        player.setUser(user);
        player.setHead(user.getHead());
        player.setNick(user.getNick());
        player.setGold(InitValue.ROBOT_GOLD);
        player.setDiamond(InitValue.ROBOT_DIAMOND);
        int exp = 200+ r.nextInt(500);
        player.setExp(exp);
        player.setId(currentId);
        currentId--;
        playerService.insertPlayer(player);
        playerList.add(player);
        map.put(player.getId(),player);
//        log.info("======新增机器人：{}",playerList.size());
    }

    //TODO 统计机器人的输赢金币和钻石
    public synchronized Player take() {
        if( playerList.size()<1) {
            increment();
        }
        if( playerList.size()<1) {
            log.error("ERROR-INFO:机器人数量不足....");
            return null;
        }
        Player robot = playerList.get(0);
//        int index = new Random().nextInt(playerList.size());
//        Player robot = playerList.remove(index);
        if( robot == null) {
            log.error("匹配机器人居然失败，机器人数量：{} ",playerList.size());
        }
        playerList.remove(0);
//        log.info("从机器人池获取机器人，id:{}", robot.getId());
        return robot;
    }

    public void giveBack(int id,int moneyType,boolean isWin,int win,int lose) {
        if(map.containsKey(id)) {
            Player player = map.get(id);
            if(isWin ) {
                if (moneyType==2) {
                    player.setDiamond(player.getDiamond()+win);
                } else {
                    player.setGold(player.getGold()+win);
                }
            } else {
                if (moneyType==2) {
                    player.setDiamond(player.getDiamond()-lose);
                } else {
                    player.setGold(player.getGold()-lose);
                }
            }
            playerService.updatePlayer(player);
//            log.info("机器人{} 用完归还，当前机器人总数量：{},可用数量：{}",id,map.size(),playerList.size());
            playerList.add(map.get(id));
        }
    }

    public static void main(String[] args) {
//        DBMgr.me().init();
//        com.wangpo.login.logic.match.RobotMgr.me().init();
//        Player p = com.wangpo.login.logic.match.RobotMgr.me().take();
//        log.info("take:{},id:{}",p.getNick(),p.getId());
//        log.info("after take,size:{}", com.wangpo.login.logic.match.RobotMgr.me().playerList.size());
//        com.wangpo.login.logic.match.RobotMgr.me().giveBack(p);
//        log.info("after giveback,size:{}", com.wangpo.login.logic.match.RobotMgr.me().playerList.size());
    }
}
