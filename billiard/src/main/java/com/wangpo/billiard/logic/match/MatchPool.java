package com.wangpo.billiard.logic.match;

import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.GlobalEnum;
import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.base.excel.GlobalConfig;
import com.wangpo.base.pool.MyThreadPool;
import com.wangpo.base.service.PlatformService;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.config.GameConfig;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.room.GameMgr;
import com.wangpo.billiard.util.RandomUtils;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.kits.BindThree;
import com.wangpo.base.kits.BindTwo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author
 *                            _ooOoo_
 *                           o8888888o
 *                           88" . "88
 *                           (| -_- |)
 *                            O\ = /O
 *                        ____/`---'\____
 *                      .   ' \\| |// `.
 *                       / \\||| : |||// \
 *                     / _||||| -:- |||||- \
 *                       | | \\\ - /// | |
 *                     | \_| ''\---/'' | |
 *                      \ .-\__ `-` ___/-. /
 *                   ___`. .' /--.--\ `. . __
 *                ."" '< `.___\_<|>_/___.' >'"".
 *               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 *                 \ \ `-. \_ __\ /__ _/ .-` / /
 *         ======`-.____`-.___\_____/___.-`____.-'======
 *                            `=---='
 *
 *         .................................................
 */
@Slf4j
@Component
public class MatchPool {
    @Resource
    private GameConfig gameConfig;

    @Resource
    ExcelMgr excelMgr;

    @DubboReference
    PlatformService platformService;

    private int rule;
    private int timeout;

    private Random random = new Random();

    public MatchPool() {

        //gameConfig.getMatchStrategy();//new Random().nextInt(2);//匹配规则0-随机，1-按规则

    }

    @Resource
    RobotMgr robotMgr;
    @Resource
    GameMgr roomMgr;
    @Resource
    PlayerMgr playerMgr;
    @Resource
    MatchHandler matchHandler;

    private static final Map<Integer,ScheduledExecutorService> executorMap = new ConcurrentHashMap<>();

//    private static final Map<Long, MatchPlayer> playerMap = new ConcurrentHashMap<>();

    private static final Map<Integer, Map<Integer, MatchPlayer>> chang2playerMap = new ConcurrentHashMap<>();

    private static Map<Integer,Set<Integer>> cancelMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService noviceGuideExecutor = MyThreadPool.createScheduled("noviceGuideMatch",1);
    
    public void cancelPlayer(int chang,int uid) {
        Set<Integer> set;
        if( !cancelMap.containsKey(chang)) {
            set  = new HashSet<>();
        } else {
            set = cancelMap.get(chang);
        }
        set.add(uid);
        cancelMap.put(chang,set);
    }

    public void newMatcher(int chang, MatchPlayer player) {
//        chang = 1031;
        if (chang2playerMap.get(chang)!=null && chang2playerMap.get(chang).size()>100000) {
            log.error("当前场次匹配数量超过10000，提交匹配失败，场次ID：{}",chang);
            return;
        } else if (chang2playerMap.get(chang)!=null && chang2playerMap.get(chang).size()>2000) {
            log.warn("当前场次匹配数量超过2000，可能造成系统压力，场次ID：{}",chang);
        }

//        log.info("新增匹配玩家：{},场次：{}", player.getId(), chang);
        player.setSecond(0);
        chang2playerMap.get(chang).put(player.getId(), player);
    }

    /**
     * 初始化匹配池
     * 1，每个场次（最多18个场次）启动一个匹配线程池进行匹配
     * 2，匹配算法分随机和战力匹配，根据配置来选择
     * 3，每秒匹配一次
     */
    public void start() {
        log.info("启动match pool");
        GlobalConfig globalConfig = excelMgr.getGlobal(GlobalEnum.MATCH_WAY.code);
        if( globalConfig !=null) {
            rule = globalConfig.intValue();
        } else {
            rule = 0;
        }

        globalConfig = excelMgr.getGlobal(GlobalEnum.MATCH_TIMEOUT.code);
        if( globalConfig !=null) {
            timeout = globalConfig.intValue();
        } else {
            timeout = 15;
        }
        log.error("初始化匹配池，匹配规则：{}，匹配时间：{}",rule,timeout);

        int scheduleStrategy = 1;//1-单线程，2-多线程
        //金币，九球，初级场
//        for(int i=1;i<=2;i++) { //1-金币场，2-钻石场
//            for (int j=1;j<=4;j++) {//1-九球玩法，2-红球玩法，3-15张牌抽牌玩法，3-54张牌抽牌玩法。
//                for (int k=1;k<=3;k++) {//1-低级场，2-中级场，3-高级场。
//                    int id = FormulaUtil.genChang(i,j,k);
//                    chang2playerMap.put(id, new ConcurrentHashMap<>());
//                    ScheduledExecutorService executor = MyThreadPool.createScheduled("match-pool-"+id,1);//Executors.newSingleThreadScheduledExecutor();
//                    executor.scheduleAtFixedRate(() -> task(id,chang2playerMap.get(id)), 1000, 1000, TimeUnit.MILLISECONDS);
//                    executorMap.put(id,executor);
//                }
//            }
//        }

        for(int id: ChangKit.CHANG) {
            chang2playerMap.put(id, new ConcurrentHashMap<>());
            ScheduledExecutorService executor = MyThreadPool.createScheduled("match-pool-"+id,1);//Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> task(id,chang2playerMap.get(id)), 1000, 1000, TimeUnit.MILLISECONDS);
            executorMap.put(id,executor);
        }

//        chang2playerMap.put(111, new ConcurrentHashMap<>());
//        chang2playerMap.put(112, new ConcurrentHashMap<>());
//        chang2playerMap.put(113, new ConcurrentHashMap<>());
//
//        sec101.scheduleAtFixedRate(() -> task(101,chang2playerMap.get(101)), 1000, 1000, TimeUnit.MILLISECONDS);
//        sec102.scheduleAtFixedRate(() -> task(102,chang2playerMap.get(102)), 1000, 1000, TimeUnit.MILLISECONDS);
//        sec103.scheduleAtFixedRate(() -> task(103,chang2playerMap.get(103)), 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void close(){
        executorMap.forEach(((integer, executor) ->  closeExecutor(executor) ));
    }
    
    /**
     * 新手引导匹配Ai
     * @param matchPlayer
     * @param changId
     */
    public void noviceGuideMatchTask(MatchPlayer matchPlayer, int changId) {
    	log.info("新手匹配任务开始");
    	noviceGuideExecutor.schedule(() -> noviceGuideMatchAi(matchPlayer,changId), 3, TimeUnit.SECONDS);
    }

    /**
     * 新手引导匹配Ai
     * @param changId
     * @return
     */
    private void noviceGuideMatchAi(MatchPlayer noviceGuidePlayer, int changId) {
        CmsChangConfig config = excelMgr.getCmsChangConfigMap().get(changId);
        //先判断是否启动了机器人开关
        if( config==null ) {
            log.error("场次{}ai配置不存在",changId);
            return;
        }
        if(  config.getAiOpen() !=1  ) {
//            log.info("场次{}关闭了机器人匹配",changId);
            return ;
        }
        Player player = robotMgr.take();
        boolean robot = false;
        if( player != null ) {
            robot = true;
//            log.info("超过10秒，直接匹配机器人:{},{}", p.getId(),player.getId());
            MatchPlayer matchPlayer = toMatchPlayer(player);
            noviceGuideMatchOK(changId,noviceGuidePlayer,matchPlayer);
        }
        if(!robot) {
          matchHandler.matchTimeOut(noviceGuidePlayer.getId());
      }
		return;
	}

    private void closeExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                log.error("{} 关闭失败，强制关闭.", "executor");
                executor.shutdownNow();
            } else {
                log.error("{} 关闭成功.", "executor");
            }
        } catch (Exception e) {
            log.error("关闭异常：",e);
        }
    }

    public void task(int changId,final Map<Integer, MatchPlayer> playerMap) {
        try {
            if( playerMap.size()<1) {
                return;
            }
            //1,先判断是否有取消匹配的玩家
            Set<Integer> set = cancelMap.get(changId);
            if(set !=null && set.size()>0) {
                Iterator<Integer> it = set.iterator();
                while (it.hasNext()) {
                    Integer id = it.next();
                    if( id!=null && playerMap.containsKey(id) ) {
                        matchHandler.pushCancel(id);
                        playerMap.remove(id);
                        it.remove();
                    }
                }
            }
            long start = System.currentTimeMillis();
            playerMap.forEach((key, value) -> value.setSecond(value.getSecond() + 1));
            List<MatchPlayer> list = new ArrayList<>(playerMap.values());
            list.sort(Comparator.comparingLong(MatchPlayer::getSecond));

            //上轮匹配时间，用于预估下轮匹配时间
            long lastMatchTime = 0;


            int needNum = getNeedNum(changId);
//            log.info("随机规则：{},changId:{}",rule,changId);
            while (list.size() >1 && (System.currentTimeMillis() - start) < (900 - lastMatchTime)) {
                if (rule == 0) {
                    lastMatchTime = randomMatch(changId,playerMap, list);
                } else {
                    lastMatchTime = ruleMatch(changId,playerMap, list);
                }
            }

            //超时判断
            BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
            if( changConfig == null) {
                return;
            }
            CmsChangConfig config = excelMgr.getCmsChangConfigMap().get(changId);
            //先判断是否启动了机器人开关
            if( config==null ) {
                log.error("场次{}ai配置不存在",changId);
                return;
            }
            Iterator<Integer> it = playerMap.keySet().iterator();
            while (it.hasNext()) {
                int id = it.next();
                MatchPlayer p = playerMap.get(id);
                //TODO 匹配超时时间，读配置
                if( p.getSecond() >= changConfig.getMatchTime() ) {
                    matchHandler.matchTimeOut(p.getId());
                    it.remove();
                } else if ( p.getSecond() >= changConfig.getAiTime() && config.getAiOpen() ==1) {
                    //TODO 到达匹配机器人时间且ai匹配开启了。
                    if( needNum ==2) {
                        Player player = robotMgr.take();
                        if( player != null ) {
                            MatchPlayer matchPlayer = toMatchPlayer(player);
                            matchOK(changId,p,matchPlayer);
                            it.remove();
                        }
                    } else if (needNum == 3) {
                        Player p1 = robotMgr.take();
                        Player p2 = robotMgr.take();
                        if( p1!=null && p2!=null) {
                            BindThree<MatchPlayer> bt = new BindThree<>(p, toMatchPlayer(p1), toMatchPlayer(p2));
                            matchOK(changId,bt);
                            it.remove();
                        }
                    }

                }
            }

        } catch (Exception e) {
            log.error("MatchException ：", e);
        }
    }

    private static final int[] cues = {101,105,110,201,205,209,303,306,403,404,505,608};
    private MatchPlayer toMatchPlayer(Player player) {
        MatchPlayer matchPlayer = new MatchPlayer();
        matchPlayer.setId(player.getId());
        matchPlayer.setNick(player.getNick());
        if(player.getHead()!=null) {
	        matchPlayer.setHead(player.getHead());
        } else {
	        matchPlayer.setHead("test2.jpg");
        }
        matchPlayer.setRoleId(player.useRoleId());
        matchPlayer.setExp(player.getExp());
        if(player.getId()<0) {
            //TODO 随机机器人球杆
            int index = random.nextInt(cues.length);
            matchPlayer.setCueId(cues[index]);
        }

        return matchPlayer;
    }


    /**
     * 规则匹配
     */
    private long ruleMatch(int changId,Map<Integer, MatchPlayer> playerMap, List<MatchPlayer> list) {
        long lastMatchTime;
        long matchStart = System.currentTimeMillis();

        int needNum = getNeedNum(changId);
        List<MatchPlayer> okList = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            MatchPlayer p = list.get(i);
            okList.add(p);
            for (int j = 0; j < list.size(); j++) {
                //修复匹配不均衡
                if(i==j) {
	                continue;
                }
                MatchPlayer p2 = list.get(j);
                int f1 =0;
                int f2 = 0;
                if(p.getGameJson()!=null && p.getGameJson().containsKey("fight")) {
	                f1 = p.getGameJson().getInteger("fight");
                }
                if(p2.getGameJson()!=null && p2.getGameJson().containsKey("fight")) {
	                f2 = p.getGameJson().getInteger("fight");
                }
                int fightDiff = Math.abs(f1 - f2);
                if (p.getSecond() > 9) {
                    okList.add(p2);
                } else {
                    if ((p.getSecond() == 1 && fightDiff <= 10)
                            || p.getSecond() == 2 && fightDiff <= 10 && !p.getLastThree().contains(p2.getId())
                            || p.getSecond() == 3 && fightDiff <= 20 && !p.getLastThree().contains(p2.getId())
                            || p.getSecond() == 4 && fightDiff <= 30 && !p.getLastThree().contains(p2.getId())
                            || p.getSecond() == 5 && fightDiff <= 50 && !p.getLastThree().contains(p2.getId())
                            //第五秒开始，不排除3把遇到的对手
                            || p.getSecond() == 6 && fightDiff <= 100
                            || p.getSecond() == 7 && fightDiff <= 200
                            || p.getSecond() == 8 && fightDiff <= 300
                            || p.getSecond() == 9 && fightDiff <= 500) {
                        okList.add(p2);
                    }
                }

                 if(okList.size()==needNum) {
                    break;
                }
            }

            if (okList.size() >= needNum) {
                //匹配成功
                break;
            } else {
//                //2个玩家+1个机器人
                boolean isTwo = false;
                if( needNum == 3 ) {
                    if( okList.size() == 2) {
                        BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
                        if( okList.get(0).getSecond() >= changConfig.getAiTime() ||
                                okList.get(1).getSecond() >= changConfig.getAiTime()) {
                            isTwo= true;
                        }
                    }
                }
                if( !isTwo ) {
                        okList.clear();
                }
                //单人或三人匹配
//                okList.clear();
            }
        }

        if( okList.size() >= 1 ) {
            if(needNum==2) {
                BindTwo<MatchPlayer> bt = new BindTwo<>(okList.get(0), okList.get(1));
                afterMatch(changId,playerMap, list, bt);
            } else if (needNum==3) {
                //2个玩家+1个机器人
                if( okList.size() ==2 ) {
                    BindThree<MatchPlayer> bt = new BindThree<>( );
                    bt.setObj1(okList.get(0));
                    bt.setObj1(okList.get(1));
                    afterMatch(changId,playerMap, list, bt);
                } else {
                    BindThree<MatchPlayer> bt = new BindThree<>(okList.get(0), okList.get(1), okList.get(2));
                    afterMatch(changId,playerMap, list, bt);
                }
                //单人或三人匹配
//                BindThree<MatchPlayer> bt = new BindThree<>(okList.get(0), okList.get(1), okList.get(2));
//                afterMatch(changId,playerMap, list, bt);
            }
        }

        lastMatchTime = System.currentTimeMillis() - matchStart;
        if (lastMatchTime > 100) {
            log.warn("单循环匹配时间：{} ms", lastMatchTime);
        }
//        log.info("单循环匹配时间：{} ms", lastMatchTime);
        return lastMatchTime;
    }

    private int getNeedNum(int changId) {
        int wanfa = (changId/10)%10;
        return wanfa>2?3:2;
    }

    private BindTwo<MatchPlayer> randomTwo(List<MatchPlayer> list) {
        if( list.size()<2) {
	        return null;
        }
        BindTwo<MatchPlayer> bt = new BindTwo<>();
        List<Integer> indexList = new ArrayList<>();
        for(int i=0;i<list.size();i++) {
	        indexList.add(i);
        }

        List<Integer> btindex = RandomUtils.randomTwo(indexList);

        bt.setObj1(list.get(btindex.get(0)));
        bt.setObj2(list.get(btindex.get(1)));

        return bt;
    }

    public static void main(String[] args) {
//        Random random = new Random();
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        executor.scheduleAtFixedRate(()->{
//            System.out.print( " Start at:"+System.currentTimeMillis());
//            int r = random.nextInt(5)+1;
//            try {
//                System.out.println("=sleep :"+r);
                //模拟耗时任务
//                long sum = 0;
//                for( long i=0;i<10000000000L;i++) {
//                    sum+=i;
//                }
//                System.out.println( " finished.");
//            } catch ( Exception e) {
//                e.printStackTrace();
//            }
//        },1000,1000,TimeUnit.MILLISECONDS);
        /*Random random = new Random();
        List<MatchPlayer> list= new ArrayList<>();
        for(int i=0;i<2;i++) {
            MatchPlayer mp = new MatchPlayer();
            mp.setId(i+1);
            list.add(mp);
        }

        List<Integer> indexList = new ArrayList<>();
        for(int i=0;i<list.size();i++) indexList.add(i);

        List<Integer> btindex = RandomUtils.randomTwo(indexList);

        int index = random.nextInt(indexList.size());
        System.out.println(list.get(index).getId());

        indexList.remove(index);
        index = random.nextInt(indexList.size());
        System.out.println(list.get(index).getId());*/
    }

    private BindThree<MatchPlayer> randomThree(int changId,List<MatchPlayer> list) {
        if( list.size()<2) {
	        return null;
        }
        //2个玩家+1个机器人
        if ( list.size() == 2 ) {
            //超时判断
            BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
            if( changConfig == null) {
                return null;
            }
            if( changConfig.getAi() == 1 ) {
                if( list.get(0).getSecond() >= changConfig.getAiTime()
                        || list.get(1).getSecond()>= changConfig.getAiTime() ) {
                    BindThree<MatchPlayer> bt = new BindThree<>();
                    bt.setObj1(list.get(0));
                    bt.setObj2(list.get(1));
                    return bt;
                }
            }
            return null;
        }
        BindThree<MatchPlayer> bt = new BindThree<>();
        Random random = new Random();
        List<Integer> indexList = new ArrayList<>();
        for(int i=0;i<list.size();i++) {
	        indexList.add(i);
        }
        int index = random.nextInt(indexList.size());
        bt.setObj1(list.get(indexList.get(index)));

        indexList.remove(index);
        index = random.nextInt(indexList.size());
        bt.setObj2(list.get(indexList.get(index)));

        indexList.remove(index);
        index = random.nextInt(indexList.size());
        bt.setObj3(list.get(indexList.get(index)));
        return bt;
    }

    private long randomMatch(int changId,Map<Integer, MatchPlayer> playerMap, List<MatchPlayer> list) {
        long lastMatchTime;
        long matchStart = System.currentTimeMillis();

        int num = getNeedNum(changId);
        if( num==2) {
            BindTwo<MatchPlayer> bt = randomTwo(list);
            afterMatch(changId,playerMap,list,bt);
        } else if ( num==3) {
            BindThree<MatchPlayer> bt = randomThree(changId,list);
            afterMatch(changId,playerMap,list,bt);
        }

        /*Random random = new Random();
        MatchPlayer p1 = list.get(0);
        MatchPlayer p2 = null;
        int index = random.nextInt(list.size() - 1) + 1;
        if (list.size() > index)
            p2 = list.get(index);

        //匹配成功
        afterMatch(changId,playerMap, list, p1, p2);*/

        lastMatchTime = System.currentTimeMillis() - matchStart;
        if (lastMatchTime > 100) {
            log.error("单循环匹配时间：{} ms", lastMatchTime);
        }
//        log.info("单循环匹配时间：{} ms", lastMatchTime);
        return lastMatchTime;
    }

    /**
     * 匹配完成
     */
//    private void afterMatch(int changId,Map<Integer, MatchPlayer> playerMap, List<MatchPlayer> list, MatchPlayer p1, MatchPlayer p2) {
//        if (p1 != null && p2 != null) {
//            log.info("匹配成功，p1:{},p2:{}", p1.getId(), p2.getId());
//            playerMap.remove(p1.getId());
//            playerMap.remove(p2.getId());
////            RoomMgr.me().newRoom(p1, p2);
//            list.remove(p1);
//            list.remove(p2);
//            matchOK(changId,p1, p2);
//        }
//    }

    //双人匹配成功
    private void afterMatch(int changId,Map<Integer, MatchPlayer> playerMap, List<MatchPlayer> list, BindTwo<MatchPlayer> bt) {
        if ( bt != null ) {
            log.info("匹配成功，p1:{},p2:{}", bt.getObj1().getId(), bt.getObj2().getId());
            playerMap.remove(bt.getObj1().getId());
            playerMap.remove(bt.getObj2().getId());
            list.remove(bt.getObj1());
            list.remove(bt.getObj2());
            matchOK(changId,bt.getObj1(), bt.getObj2());
        }
    }

    //三人匹配成功
    private void afterMatch(int changId,Map<Integer, MatchPlayer> playerMap, List<MatchPlayer> list, BindThree<MatchPlayer> bt) {
        if ( bt != null ) {
            //需要匹配机器人 2个玩家+1机器人
            if( bt.getObj3() == null ) {
                log.error("2个玩家匹配一个机器人");
                Player player = robotMgr.take();
                if( player != null ) {
                    MatchPlayer matchPlayer = toMatchPlayer(player);
                    bt.setObj3(matchPlayer);

                    playerMap.remove(bt.getObj1().getId());
                    playerMap.remove(bt.getObj2().getId());
                    list.remove(bt.getObj1());
                    list.remove(bt.getObj2());
                    matchOK(changId,bt);
                } else {
                    //TODO 获取机器人失败，需要重置玩家状态
                    log.error("获取机器人失败。。。。");
                }
            } else {
                log.info("匹配成功，p1:{},p2:{},{}", bt.getObj1().getId(), bt.getObj2().getId(),bt.getObj3().getId());
                playerMap.remove(bt.getObj1().getId());
                playerMap.remove(bt.getObj2().getId());
                playerMap.remove(bt.getObj3().getId());
                list.remove(bt.getObj1());
                list.remove(bt.getObj2());
                list.remove(bt.getObj3());
                matchOK(changId,bt);
            }
            //单人或三人匹配
//            log.info("匹配成功，p1:{},p2:{},{}", bt.getObj1().getId(), bt.getObj2().getId(),bt.getObj3().getId());
//            playerMap.remove(bt.getObj1().getId());
//            playerMap.remove(bt.getObj2().getId());
//            playerMap.remove(bt.getObj3().getId());
//            list.remove(bt.getObj1());
//            list.remove(bt.getObj2());
//            list.remove(bt.getObj3());
//            matchOK(changId,bt);

        }
    }

    //两人场
//    private void matchOK(int changId,BindTwo<MatchPlayer> bt) {
//        int roomNo = IDGenerator.generateID();
//        List<MatchPlayer> matchPlayers = new ArrayList<>();
//        matchPlayers.add(bt.getObj1());
//        matchPlayers.add(bt.getObj2());
//        roomMgr.newGame(changId,matchPlayers,roomNo);
//        sendMatch(roomNo, matchPlayers);
//
//    }

    private void sendMatch(int roomNo, List<MatchPlayer> matchPlayers) {
        //匹配成功，发送消息给客户端
        BilliardProto.S2C_MatchOK.Builder okProto = BilliardProto.S2C_MatchOK.newBuilder();
        for (MatchPlayer p : matchPlayers) {
            okProto.addMatchPlayers(BilliardProto.MatchPlayer.newBuilder()
                    .setId(p.getId())
                    .setHead(p.getHead())
                    .setNick(p.getNick())
            );
        }

        for (MatchPlayer p : matchPlayers) {
            Player player = playerMgr.getPlayerByID(p.getId());
            if (player != null) {
                //重置匹配状态
                player.setMatchStatus(0);
                player.setRoomNo(roomNo);

                if(matchPlayers.size()==2) {
                    //三人场，不排除对手
                    matchPlayers.forEach(matchPlayer -> {
                        if(matchPlayer.getId()!=player.getId()) {
                            player.addRival(matchPlayer.getId());
                        }
                    });
                }

                matchHandler.matchOK(okProto, player.getId());
            } else if(p.getId()>0 ){
                log.error("匹配成功，玩家找不到缓存，玩家ID:{}",p.getId());
            }
        }
    }

    //三人场
    private void matchOK(int changId,BindThree<MatchPlayer> bt) {
        int roomNo = IDGenerator.generateID();
        List<MatchPlayer> matchPlayers = new ArrayList<>();
        matchPlayers.add(bt.getObj1());
        matchPlayers.add(bt.getObj2());
        matchPlayers.add(bt.getObj3());
        roomMgr.newGame(changId,matchPlayers,roomNo);

        //匹配成功，发送消息给客户端

        sendMatch(roomNo, matchPlayers);

        //TODO 匹配成功，直接扣台费
        BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
        if( changConfig != null && changConfig.getFee()>0) {
            if( changConfig.getMoneyType()==1) {
                platformService.modifyGold(bt.getObj1().getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
                platformService.modifyGold(bt.getObj2().getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
                platformService.modifyGold(bt.getObj3().getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
            } else if(  changConfig.getMoneyType()==2 ){
                platformService.modifyDiamond(bt.getObj1().getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
                platformService.modifyDiamond(bt.getObj2().getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
                platformService.modifyDiamond(bt.getObj3().getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
            }
        }
    }

    private void matchOK(int changId,MatchPlayer p1, MatchPlayer p2) {
//        Proto proto = new Proto();
//        proto.setCmd(HostCmd.M2R_MATCH);
        int roomNo = IDGenerator.generateID();
//        GameProto.M2R_Match.Builder b = GameProto.M2R_Match.newBuilder()
//                .setRoomNo(roomNo)
//                .setChang(101)
//                .addGp(p1.toProto())
//                .addGp(p2.toProto())
//                ;
//        proto.setBody(b.build().toByteArray());
//        clientMgr.sendRoom(proto);

        List<MatchPlayer> matchPlayers = new ArrayList<>();
        matchPlayers.add(p1);
        matchPlayers.add(p2);
        roomMgr.newGame(changId,matchPlayers,roomNo);
        log.info("匹配成功，玩家{}于第{}秒 和玩家{}于第{}秒成功匹配。",
                p1.getNick(),p1.getSecond(),
                p2.getNick(),p2.getSecond());
        //匹配成功，发送消息给客户端
        sendMatch(roomNo, matchPlayers);
        //TODO 匹配成功，直接扣台费
        BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
        if( changConfig != null && changConfig.getFee()>0) {
            if( changConfig.getMoneyType()==1) {
                platformService.modifyGold(p1.getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
                platformService.modifyGold(p2.getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
            } else if(  changConfig.getMoneyType()==2 ){
                platformService.modifyDiamond(p1.getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
                platformService.modifyDiamond(p2.getId(),-changConfig.getFee(), GameEventEnum.BILLIARD_GAME_FEE.reason);
            }
        }
    }


    
    private void noviceGuideMatchOK(int changId,MatchPlayer p1, MatchPlayer p2) {
        int roomNo = IDGenerator.generateID();
        List<MatchPlayer> matchPlayers = new ArrayList<>();
        matchPlayers.add(p1);
        matchPlayers.add(p2);
        roomMgr.noviceGuideNewGame(changId,matchPlayers,roomNo);
        log.info("匹配成功，玩家{}于第{}秒 和玩家{}于第{}秒成功匹配。",
                p1.getNick(),p1.getSecond(),
                p2.getNick(),p2.getSecond());
        //匹配成功，发送消息给客户端
        sendMatch(roomNo, matchPlayers);
    }

}
