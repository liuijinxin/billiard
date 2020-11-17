package com.wangpo.billiard.logic;

import com.wangpo.base.pool.MyThreadPool;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.logic.room.GameMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PlayerMgr {
    private static final ScheduledExecutorService executor = MyThreadPool.createScheduled("player-offline-executor",1);
    // Executors.newSingleThreadScheduledExecutor();

    public static final Map<Integer, Player> idMap = new ConcurrentHashMap<>();
    public static final Map<String, Player> tokenMap = new ConcurrentHashMap<>();

    @Resource
    GameMgr gameMgr;

    public void addPlayerByID(Player player) {
        idMap.put(player.getId(),player);
    }

    public void removePlayer(int id) {
        idMap.remove(id);
    }

    public int totalPlayerSize() {
        return idMap.size();
    }

    public Player getPlayerByID(Integer id) {
        return idMap.get(id);
    }

    public Map<Integer,Player> allPlayer(){
        return idMap;
    }

    public int online() {
        int total = 0;
        for(Player player:idMap.values()) {
            if( player.isOnline()) {
                total++;
            }
        }
        return total;
    }

//    public void calculateExp(Player player) {
//        int exp = player.getExp();
//        int total=0;
//        for(int i=1;i<exps.length;i++) {
//            total+=exps[i];
//            if( exp <total ) {
//                player.setLevel(i);
//                player.setCurrentExp(exp-(total-exps[i]));
//                player.setCurrentNeedExp(exps[i]-player.getCurrentExp());
//                break;
//            }
//        }
//    }


    public void submitOfflinePlayer(final int uid) {
        //2小时未登录则删除缓存
        long delay =  2*60*60*1000;
        executor.schedule(()->{
//            log.info("玩家离线调度，玩家iD：{}",player.getId());
            Player player = idMap.get(uid);
            if( player == null ) {
//                log.error("离线调度异常，玩家缓存不存在，玩家id:{}",uid);
                return ;
            }
            if ( !player.isOnline() && player.offlineTime()>=(delay/1000-3)) {//
                //移除在线缓存
                removePlayer(player.getId());
//                UserDao.updateUser(player);
//                gameMgr.data();
                log.error("移除在线缓存，玩家ID：{},离线时长：{} 秒,在线玩家数量：{}",
                        player.getId(),
                        player.offlineTime(),
                        online()
                );
            }
        },delay, TimeUnit.MILLISECONDS);
    }
    /**
     * 提交离线玩家
     */
    public void submitOfflinePlayer2(Player player) {
        //2小时未登录则删除缓存
        long delay = 2*60*60*1000;
        executor.schedule(()->{
            if ( !player.isOnline() ) {//&& player.offlineTime()>=delay
                //移除在线缓存
                removePlayer(player.getId());
            }
        },delay, TimeUnit.MILLISECONDS);
    }


}

