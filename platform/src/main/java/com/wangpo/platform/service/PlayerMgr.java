package com.wangpo.platform.service;

import com.wangpo.platform.bean.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PlayerMgr {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    public static final Map<Integer, Player> idMap = new ConcurrentHashMap<>();

    public static final Map<String, Player> tokenMap = new ConcurrentHashMap<>();

    public Map<Integer, Player> getIdMap() {
        return idMap;
    }

    public void addPlayerByID(Player player) {
//        log.error("添加在线缓存，玩家id:{}",player.getId());
        idMap.put(player.getId(),player);
    }

    public void removePlayer(int id) {
//        log.error("移除在线缓存，玩家id:{}",id);
        idMap.remove(id);
//        log.error("当前在线缓存玩家数量：{}",idMap.size());
    }

    public Player getPlayerByID(Integer id) {
        return idMap.get(id);
    }

    public void addPlayerByToken(Player player) {
        tokenMap.put(player.getOpenId(),player);
    }

    public void removePlayerByToken(String token) {
        tokenMap.remove(token);
    }

    public Player getPlayerByToken(String token) {
        if(token==null || !tokenMap.containsKey(token)) {
            return null;
        }
        return tokenMap.get(token);
    }

    public Map<Integer,Player> allPlayer(){
        return idMap;
    }

    /**
     * 提交离线玩家
     */
    public void submitOfflinePlayer(final int uid) {
        //2小时未登录则删除缓存
        final long delay =  2*60*60*1000;
        executor.schedule(()->{
//            log.info("玩家离线调度，玩家iD：{}",player.getId());
            Player player = idMap.get(uid);
            if( player == null ) {
//                log.error("离线调度异常，玩家缓存已删除，玩家id:{}",uid);
                return ;
            }
            if ( !player.isOnline() && player.offlineTime()>=(delay/1000-3) ) {//
                //移除在线缓存
                removePlayer(player.getId());
//                UserDao.updateUser(player);
                log.error("移除在线缓存，玩家ID：{},离线时长：{} 秒,在线玩家数量：{}",
                        player.getId(),
                        player.offlineTime(),
                        idMap.size()
                );
            }
        },delay, TimeUnit.MILLISECONDS);
    }

}

