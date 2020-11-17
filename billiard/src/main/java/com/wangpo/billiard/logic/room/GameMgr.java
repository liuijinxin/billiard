package com.wangpo.billiard.logic.room;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.pool.MyThreadPool;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.task.BilliardTaskType;
import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.billiard.bean.BilliardLog;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.cue.CueMgr;
import com.wangpo.billiard.logic.match.MatchPlayer;
import com.wangpo.billiard.logic.match.RobotMgr;
import com.wangpo.billiard.logic.player.PlayerHandler;
import com.wangpo.billiard.logic.role.RoleHandler;
import com.wangpo.billiard.logic.util.FightUtil;
import com.wangpo.billiard.logic.util.TaskUtil;
import com.wangpo.billiard.service.BilliardLogService;
import com.wangpo.billiard.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * 游戏房间管理类
 */
@Component
@Slf4j
public class GameMgr {
    public static final Map<Integer, AbstractGame> map = new ConcurrentHashMap<>();

    ScheduledExecutorService executor = MyThreadPool.createScheduled("GameMgr",8);
//    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);
//    private static final ExecutorService executor = MyThreadPool.create("GameMgr",4,8);
//    private static final ScheduledExecutorService frameExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
//    private static final ScheduledExecutorService frameExecutor = Executors.newScheduledThreadPool(1);

    private static final ScheduledExecutorService prepareExecutor = MyThreadPool.createScheduled("player-prepare-executor",1);

//    public static final int SCHEDULE_TIME = 21;

    @Resource
    GameHandler gameHandler;
    @Resource
    RoleHandler roleHandler;
    @DubboReference
    PlatformService platformService;
    @Resource
    PlayerMgr playerMgr;
    @Resource
    ExcelMgr excelMgr;
    @Resource
    PlayerHandler playerHandler;
    @Resource
    PlayerService playerService;
    @Resource
    CueMgr cueMgr;
    @Resource
    BilliardLogService billiardLogService;
//    @Resource
//    AIMgr aiMgr;
    private static final Random random = new Random();
    /**
     * 游戏结束，删除游戏房间
     * @param roomNo fjh
     */
    public void removeRoom(int roomNo) {
        map.remove(roomNo);
    }

    public AbstractGame get(int roomNo) {
        if( !map.containsKey(roomNo)) {
            return null;
        }
        return map.get(roomNo);
    }

//    public AbsGame newGame(GameProto.M2R_Match match) {
//        ClassicNineGame gr = new ClassicNineGame();
//        gr.init(match);
//        map.put(gr.getRoomNo(),gr);
//        return gr;
//    }

    public int randomAI(int changId,List<MatchPlayer> matchPlayers){
        //新手保护，前三局必定弱AI
        boolean hasAi = false;
        MatchPlayer human  = null;
        for(MatchPlayer mp:matchPlayers) {
            if(mp.getId()<0) {
                hasAi = true;
            } else {
                human = mp;
            }
        }
        if( hasAi && human!=null) {
            JSONObject gameJson = human.getGameJson();
            if( gameJson!=null && gameJson.containsKey("total")) {
                if( gameJson.getInteger("total") <=3 ) {
                    return 0;
                }
            }
        }

        int aiRate = 30;
        CmsChangConfig config = excelMgr.getCmsChangConfigMap().get(changId);
        if( config != null ) {
            aiRate = config.getStrongRate();
//            log.info("配置场次强ai几率：{}",aiRate);
        }
        int isStrong = 0;//强AI几率，根据场次配置
        int r = random.nextInt(100);
        if( r< aiRate ) {
            isStrong = 1;
//            log.info("随机到了强AI");
        } else {
//            log.info("随机到了弱AI");
        }
        return isStrong;
    }
    
	/**
	 * 创建房间入扣
	 * @param changId   场次ID 1000*
	 * @param matchPlayers  匹配成功的玩家
	 * @param roomNo    房间号，服务器唯一标识
	 */
	public void noviceGuideNewGame(int changId, List<MatchPlayer> matchPlayers, int roomNo) {
	    //场次配置
		BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
		int rod = 20;
		if( changConfig != null ) {
		    rod = changConfig.getRod();
		}
		int isStrong = 0;
		//经典九球
		ClassicNineGame gr = new ClassicNineGame();
		gr.setChang(changId);
		gr.init(matchPlayers,roomNo, isStrong);
		gr.setRod(rod);
		gr.setNoviceGuide(true);
		gr.setCurrentPlayer(gr.getPlayerList().get(0));
		//这里需要等到所有玩家进行第一次同步才处理
		    map.put(gr.getRoomNo(),gr);
	}

    /**
     * 创建房间入扣
     * @param changId   场次ID 1000*
     * @param matchPlayers  匹配成功的玩家
     * @param roomNo    房间号，服务器唯一标识
     */
    public void newGame(int changId, List<MatchPlayer> matchPlayers, int roomNo) {
        int gameId = FightUtil.chang2game(changId);//  (changId/10)%100;
        //TODO 随机强AI

        //场次配置
        BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(changId);
        int rod = 20;
        if( changConfig != null ) {
            rod = changConfig.getRod();
        }
//        gameId = 3;
        int isStrong = randomAI(changId,matchPlayers);
        if(gameId == 1) {
            //经典九球
            ClassicNineGame gr = new ClassicNineGame();
            gr.setStartTime(System.currentTimeMillis());
            gr.setChang(changId);
            gr.init(matchPlayers,roomNo, isStrong);
            gr.initRobot();
            gr.setRod(rod);
            //这里需要等到所有玩家进行第一次同步才处理
//            scheduleTimeOut(gr);
//            submitAI(gr);
            map.put(gr.getRoomNo(),gr);
            prepareSchedule(gr);
            //游戏开始，启动帧同步定时器
//        BilliardProto.S2C_Frame.newBuilder()
        } else if( gameId == 2){
            //红球玩法
            RedGame gr = new RedGame();
            gr.setStartTime(System.currentTimeMillis());
            gr.setChang(changId);
            gr.init(matchPlayers,roomNo, isStrong);
            gr.initRobot();
            gr.setRod(rod);
//            scheduleTimeOut(gr);
//            submitAI(gr);
            map.put(gr.getRoomNo(),gr);
            prepareSchedule(gr);
            //游戏开始，启动帧同步定时器
//        BilliardProto.S2C_Frame.newBuilder()
        } else if( gameId == 4 || gameId == 3 ) {
            //54张抽牌
            int mode = gameId == 3?2:1;
            DrawGame gr = new DrawGame(mode);
            gr.setStartTime(System.currentTimeMillis());
            gr.setChang(changId);
            gr.init(matchPlayers,roomNo, isStrong);
            gr.initRobot();
            gr.setRod(rod);
            map.put(gr.getRoomNo(),gr);
            prepareSchedule(gr);
            //抽牌
            draw(gr);
        }
    }

    /**
     * 玩家匹配成功后超过30秒未准备，直接退出房间
     * @param gr 房间
     */
    private void prepareSchedule(AbstractGame gr) {
        StringBuilder sb = new StringBuilder();
        sb.append("房间号：").append(gr.roomNo).append(",");
        for (GamePlayer gamePlayer : gr.playerList) {
            sb.append("玩家ID：").append(gamePlayer.getId()).append(",");
            if (gamePlayer.getId() > 0) {
                prepareExecutor.schedule(() -> {
                    if (!gamePlayer.isPrepare()) {
                        log.error("玩家超时未准备，自动退出房间，房间号{}",gr.roomNo);
                        prepareExitGame(gr,gamePlayer.getId());
                        if (!gr.isGameOver) {
                            boolean prepareOk = true;
                            for(GamePlayer gp:gr.playerList) {
                                if (gp.getId() > 0 && !gp.isPrepare() && !gp.isExit()) {
                                    prepareOk = false;
                                    break;
                                }
                            }
                            if( prepareOk ) {
                                gr.isPrepared = true;
                                gr.ganTime = System.currentTimeMillis();
                                log.error("{}全部准备OK，开始游戏。。。",gr.getRoomNo());
                                gameHandler.dispatch(gr.buildOptPlayer(0),gr);
                                afterOpt(gr);
                            }
                        }
                    }
                },30, TimeUnit.SECONDS);
            }
        }
        log.error("匹配成功，" + sb.substring(0,sb.length()-1));
    }

    public void prepareExitGame(AbstractGame game,int id) {
        Player player = playerMgr.getPlayerByID(id);
        if( player != null && player.getRoomNo()>0) {
            if (game != null) {
                player.setRoomNo(0);
                exitGame(game,id);
            }
        }
    }

    //抽牌
    private void draw(DrawGame gr) {
        gr.draw();
    }


//    @Deprecated
//    public void startFrame(AbsGame gr){
////        if( gr.future != null) {
////            log.error("已经开启了帧同步。");
////            return;
////        }
////        gr.future = frameExecutor.scheduleAtFixedRate(()->frame(gr),1000/60,17, TimeUnit.MILLISECONDS);
//    }

//    private void frame(AbsGame gr) {
//        if( gr.isGameOver ) {
//            gr.future.cancel(false);
//            log.info("游戏结束，停止调度，房间ID：{}",gr.getRoomNo());
//            return;
//        }
//        gr.setFrame(gr.getFrame()+1);
//        double interval = 0;
//        if (gr.lastFrameTime != 0) {
//            interval = System.currentTimeMillis() - gr.lastFrameTime;
//        }
//        gr.lastFrameTime = System.currentTimeMillis();
//        S2C s2c = new S2C();
//        s2c.setCid(Cmd.FRAME);
//        s2c.setBody(BilliardProto.S2C_Frame.newBuilder()
//                .setFrame(gr.getFrame())
//                .setInterval(interval)
//                .build().toByteArray());
//        gameHandler.dispatch(s2c,gr);
//    }

    /**
     * 台球同步，整个游戏的关键同步
     * 服务器等待所有客户端（非ai）同步才进行下一步操作判断，或者超时15秒走下一杆逻辑
     * 1，同步所有球的位置
     * 2，同步首个击球号码，用于判断犯规
     * 关于断线重连：
     * 为了保持客户端完全同步，目前采用以下断线重连规则=>
     * 1，每次击球会保存客户端的击球包：syncC2S
     * 2，断线重连后，先判断syncC2S是否有，如果有代表有人击球，此时，一直等，直到下一杆操作
     * 3，如果没有syncC2S包，则代表没有人击球，此时直接进入游戏。
     * 关于台球坐标
     * 1，客户端是3D坐标x,y,z，服务端是2D直角坐标系x,y
     * 2,客户端的x与服务器的x对应，客户端z与服务端的y取反
     *
     * @param id2   同步玩家id
     * @param c2s   同步对象
     * @param game  游戏对象
     */
    public void syncPos(int id2, C2S c2s, AbstractGame game) {
        if( game.isGameOver) {
//            log.error("游戏已经结束，同步消息忽略");
            return;
        }

        boolean prepared = false;
        if( game.getGan()==0 ) {
            if ( game.isPrepared ) {
                log.error("游戏已经准备好了，不需要再处理同步信息1111");
                gameHandler.dispatch(game.buildOptPlayer(0),game);
            } else {
                for(GamePlayer gp:game.playerList) {
                    if(gp.getId()==id2) {
                        gp.setPrepare(true);
                    }
                }
                boolean prepareOk = true;
                for(GamePlayer gp:game.playerList) {
                    if (gp.getId() > 0 && !gp.isPrepare() && !gp.isExit()) {
                        prepareOk = false;
                        break;
                    }
                }
                if( prepareOk ) {
                    game.isPrepared = true;
                    log.error("{}全部准备OK，开始游戏。。。",game.getRoomNo());
                    gameHandler.dispatch(game.buildOptPlayer(0),game);
                    afterOpt(game);
                }
            }
        }
//

        //等待所有玩家同步完成
        try{
            game.syncLock.lock();
            game.syncFlag.merge(game.gan, 1, Integer::sum);
            int max = 0;
            for(GamePlayer gp:game.playerList) {
                if( gp.isExit() ) {
                    //已经强退，不需要同步
                    continue;
                }
                if( gp.getId()>0 && gp.isNeedSyncGan()) {
                    max++;
                }
            }
            if( game.syncFlag.get(game.gan) < max ) {
                //等待所有需要同步的玩家的同步消息。
                return;
            }
        } finally {
            game.syncLock.unlock();
        }



        BilliardProto.C2S_SyncPos2 builder =null;
        try{
            builder = BilliardProto.C2S_SyncPos2.parseFrom(c2s.getBody());
        } catch (Exception e) {

        }
        if( builder != null) {
//            log.error("{}开始同步杆：{} ",game.getRoomNo(),game.getGan()  );
            if( builder.getGan() < game.gan ) {
//                log.error("服务器已经超时，同步杆异常,{},{}",builder.getGan(),game.gan);
            }
            //同步所有球位置
            for (BilliardProto.GameBall ballProto : builder.getBallsList()) {
                int number = ballProto.getId();// o.getInteger("number");
                double x = ballProto.getPosition().getX();// o.getJSONObject("position").getDouble("x");
                double y = ballProto.getPosition().getZ();//o.getJSONObject("position").getDouble("z");
                double z = ballProto.getPosition().getY();
                if (number == 0) {
//                    log.error("同步白球：{},{}",x,-y);
                    game.getWhiteBall().setX(x);
                    game.getWhiteBall().setY(-y);
                    game.getWhiteBall().setZ(z);
                    game.getWhiteBall().setAx(ballProto.getAngle().getX());
                    game.getWhiteBall().setAy(ballProto.getAngle().getY());
                    game.getWhiteBall().setAz(ballProto.getAngle().getZ());
                    game.getWhiteBall().setAw(ballProto.getAngle().getW());

                    game.getWhiteBall().setBx(ballProto.getBody().getX());
                    game.getWhiteBall().setBy(ballProto.getBody().getY());
                    game.getWhiteBall().setBz(ballProto.getBody().getZ());
                } else {
                    //同步球桌上的球
                    for (GameBall gb : game.getBallList()) {
                        if (gb.getNumber() == number) {
                            gb.setX(x);
                            gb.setY(-y);
                            gb.setZ(z);
                        }
                    }
                    for (GameBall gb : game.getBallListCopy()) {
                        if (gb.getNumber() == number) {
                            gb.setX(x);
                            gb.setY(-y);
                            gb.setZ(z);

                            gb.setAx(ballProto.getAngle().getX());
                            gb.setAy(ballProto.getAngle().getY());
                            gb.setAz(ballProto.getAngle().getZ());
                            gb.setAw(ballProto.getAngle().getW());

                            gb.setBx(ballProto.getBody().getX());
                            gb.setBy(ballProto.getBody().getY());
                            gb.setBz(ballProto.getBody().getZ());
                        }
                    }
                    for (GamePlayer gamePlayer : game.getPlayerList()) {
                        for (GameBall ball : gamePlayer.getNeedBall()) {
                            if (ball.getNumber() == number) {
                                if (ball.getX() != x || ball.getY() == y) {
                                    log.error("位置不同步，号码：{}", number);
                                    ball.setX(x);
                                    ball.setY(-y);
                                }
                            }
                        }
                    }
                }
            }
        }


        //第一个碰撞球，用于服务端验证犯规
        int hitFirstBall = builder==null?0:builder.getHitFirstBall();//json.getJSONObject("data").getInteger("hitFirstBall");
        int hitKu = builder==null?0:builder.getHitKu();
        Set<Integer> balls = game.getSnookerMap().get(game.getGan());
        FoulEnum foul = FoulEnum.NO_FOUL ;
        if (hitFirstBall == 0) {
            foul = FoulEnum.NULL_CUE;//空杆
        } else if (game.judgeFoul(hitFirstBall)) {
            foul = FoulEnum.HIT_ERROR;//打到别人的球
        }
        //判断是否白球进袋
        if (balls != null) {
            for (Integer ball : balls) {
                if (ball == 0) {
                    foul = FoulEnum.WHITE_SNOOKER;
                    break;
                }
            }
        }


        //判断是否有进球
        if (balls != null && balls.size() > 0) {
            //判断归属问题
            if( game.getGan()>1 && !game.isDivide) {
                boolean isDivide = game.divide();
                if( isDivide ) {
                    //单双球推送
                    for(GamePlayer gamePlayer:game.getPlayerList()) {
                        S2C s2c = new S2C();
                        s2c.setCid(Cmd.BIG_SMALL);
                        s2c.setBody(BilliardProto.S2C_BigSmall.newBuilder()
//                                .setBigOrSmall(gamePlayer.getNeedBall().get(0).getNumber() > 8 ? 2 : 1)
                                .setBigOrSmall(gamePlayer.getSide())
                                .build().toByteArray());
                        gameHandler.push(gamePlayer.getId(),s2c);
                    }
                }

            }


        }

        //判断游戏是否结束
        boolean isOver = game.isOver(foul.code);
        if( isOver ) {
            if( game.gan <= 1 ) {
                //TODO 首杆进黑八，重新摆球。
                game.reset();
                game.round--;
                S2C s2c = new S2C();
                s2c.setCid(Cmd.RESET_GAME);
                gameHandler.dispatch(s2c,game);
            } else {
                handleGameOver(game, 0, false);
            }
            return;
        }

        //转发
        gameHandler.transfer(c2s,game);
        //只需要收到一个玩家的同步信息，就可以重置proto
        game.setProto(null);

        game.setSyncC2S(c2s);
        log.error("设置同步包:{}",game.getRoomNo());
        //删除ballListCopy
        for(Set<Integer> set:game.getSnookerMap().values()) {
            if( set!=null && set.size()>0) {
                game.getBallListCopy().removeIf(gameBall -> set.contains(gameBall.getNumber()));
            }
        }

        if (game.getGan()>0) {
            if(game.handleFoul(foul.code)) {
                handleGameOver(game, 0, false);
                return;
            }
            int id = game.currentPlayer.getId();
            int repeatFoul = game.currentPlayer.getFoul();
//            log.error("同步完成{}",foul.code);
            handleNextOpt(game, foul.code);
            if (foul.code > 0) {
//                log.info("{}犯规，错误：{}，玩家ID:{}",game.getRoomNo(),foul.reason,id);
                gameHandler.transfer(game.buildFoul(foul.code, id, repeatFoul),game);
            }
        }
//        if( game.gan == 0 && game.isPrepared ) {
//            //直接将杆设置为1
//            game.gan = 1;
//        }
        game.afterSync();

    }

    //下一个操作
    private void handleNextOpt(AbstractGame game, int foul) {
        game.calculateNextPlayer(foul);
        game.foulFlag.put(game.gan,foul);
        log.error("{}下一个操作者：{},当前杆：{}",game.getRoomNo(),game.currentPlayer.getId(),game.gan);
        //如果玩家都离线，则转发同步消息
        boolean isAllOffline = true;
        for(GamePlayer gp:game.playerList) {
            if( gp.getId()>0 && gp.isNeedSyncGan() ) {
                    isAllOffline = false;
                    break;
            }
        }
        if(isAllOffline) {
            if( game.syncC2S != null ) {
                game.modifySyncC2S(2);
                gameHandler.transfer(game.syncC2S,game);
            } else {
                log.error("同时离线，找不到syncC2S.....:{}",game.getRoomNo());
            }
        } else {
            if( game.syncC2S != null ) {
//                log.error("非同时离线，不需要发送synC2S:{}",game.getRoomNo());
                game.modifySyncC2S(2);
                gameHandler.transfer(game.syncC2S,game);
            }
        }

        //转发同步消息，用于断线重连
        gameHandler.dispatch(game.buildOptPlayer(foul),game);
        //击球的后续操作
        afterOpt(game);
    }

    //当前操作完成
    private void afterOpt(AbstractGame gameRoom){
		if(gameRoom.isNoviceGuide() && gameRoom.getGanNum() <= 1) {
			log.info("新手引导第一杆和第三杆不需要超时调度:{}",gameRoom.gan);
		}else {
			//超时调度
			log.info("超时调度:{}",gameRoom.gan);
	    	scheduleTimeOut(gameRoom);
		}
        //超时调度
//        scheduleTimeOut(gameRoom);
        //执行ai
        if(  isCurrentAI(gameRoom)) {
            //不摆球。如果需要摆球，放开这段注释
//            log.info("是否空杆：{}",gameRoom.emptyRod);
//            log.error("机器人准备击球");
            if( gameRoom.emptyRod) {
                S2C s2c = gameRoom.aiLayBall();
                if( s2c != null ) {
                    gameHandler.dispatch(s2c, gameRoom);
                }
            }
            if(gameRoom.isNoviceGuide()) {
            	submitAI(gameRoom,2+random.nextInt(3));
            }else {
            	submitAI(gameRoom,5+random.nextInt(3));
            }
        }
        //重置玩家是否需要同步标识
        for(GamePlayer gp:gameRoom.getPlayerList()){
            if(gp.getId()<=0) {
                continue;
            }
            Player player = playerMgr.getPlayerByID(gp.getId());
            if( player.isOnline() ) {
                gp.resetGan();
            }
        }
    }

    //提交AI
    private void submitAI(AbstractGame gameRoom, int t) {
        if( gameRoom.getCurrentPlayer().getId() <0) {
            executor.schedule(()->ai(gameRoom),t,TimeUnit.SECONDS);
        }
    }

    private boolean isCurrentAI(AbstractGame game){
        return game.currentPlayer.getId()<0;
    }
    //执行AI
    private void ai(AbstractGame gameRoom){
//        log.info("调度AI：{}，roomNo:{}",gameRoom.currentPlayer.getId(),gameRoom.getRoomNo());
//        Random r =new Random();
        /*if(  isCurrentAI(gameRoom)) {
            //不摆球。如果需要摆球，放开这段注释
            log.info("是否空杆：{}",gameRoom.emptyRod);
            if( gameRoom.emptyRod) {
                S2C s2c = gameRoom.aiLayBall();
                if( s2c != null ) {
                    gameHandler.dispatch(s2c, gameRoom);
                    submitAI(gameRoom,3);
                } else {
                    aiOpt(gameRoom);
                }
            } else {
                aiOpt(gameRoom);
            }
        }*/
        aiOpt(gameRoom);
    }

    /**
     * AI操作
     * @param game 游戏
     */
    private void aiOpt(AbstractGame game)  {
        try {
            double angle = game.aiOpt();
            //第一步，先移杆
            C2S c2s = game.buildAngleProto(angle);
            gameHandler.transfer(c2s, game);
            //第二步，发送击球信息
            c2s = game.buildAIProto(angle);
            BilliardProto.C2S_Batting b = BilliardProto.C2S_Batting.parseFrom(c2s.getBody());
            game.setProto(b);
            //设置击球协议，供客户端用
            gameHandler.transfer(c2s, game);
            //模拟玩家操作
            game.opt();
            //等待同步超时
            scheduleSyncTimeOut(game);
        } catch (InvalidProtocolBufferException e) {
            log.error("解析异常。");
        }
    }

    //操作超时
    private void scheduleTimeOut(AbstractGame gameRoom) {
        final int g = gameRoom.getGan();
        final int r = gameRoom.getRound();
        final int roomNo = gameRoom.getRoomNo();

        executor.schedule(() -> timeOut(gameRoom,g, r,roomNo), gameRoom.rod, TimeUnit.SECONDS);
    }

    //等待同步超时
    public void scheduleSyncTimeOut(AbstractGame game) {
        final int g = game.getGan();
        final int id = game.getCurrentPlayer().getId();
        //TODO 写死15秒
        executor.schedule(() -> syncTimeOut(game,g, id), 20, TimeUnit.SECONDS);
    }

    //客户端同步超时
    public void syncTimeOut(AbstractGame game, int ganNum, long id) {
        if (game.isGameOver()) {
            return;
        }


        log.error("同步超时，gan:{},ganNum:{}",game.getGan(),ganNum);
        if (ganNum == game.getGan() && id == game.getCurrentPlayer().getId()) {
            //判断游戏是否结束
            boolean isOver = game.isOver(0);
            if( isOver ) {
                if( game.gan <= 1 ) {
                    //TODO 首杆进黑八，重新摆球。
                    game.reset();
                    game.round--;
                    S2C s2c = new S2C();
                    s2c.setCid(Cmd.RESET_GAME);
                    gameHandler.dispatch(s2c,game);
                } else {
                    handleGameOver(game, 0, false);
                }
                return;
            }

            //判断下一个操作玩家
            if (maxGan(game)) {
                log.error("游戏结束，同步超时且超过300杆，房间id:{}", game.getRoomNo());
                game.gameOverDraw();
                handleGameOver(game,0, false);
            } else {
                log.error("等待客户端同步球位置超时，当前杆：{},",game.gan);
                handleNextOpt(game,0);
            }
        }
    }

    //超过最大杆，直接结束
    private boolean maxGan(AbstractGame game){
        return game.getGan()>300;
    }

    //超时调度
    public void timeOut(AbstractGame game, int opt, int round, int roomNo) {
        if (game.isGameOver()) {
            return;
        }
        if (opt == game.getGan() && round == game.getRound() && game.getRoomNo()==roomNo) {
            if (maxGan(game)) {
                log.error("游戏结束，超过300杆，房间id:{}", game.getRoomNo());
                game.gameOverDraw();
                //默认赢家为第一个玩家，不扣金币
                game.winner = game.playerList.get(0);
                handleGameOver(game, 0, true);
            } else {
                //先判断玩家是已操作
                if( game.optFlag.containsKey(opt) && game.optFlag.get(opt)==1) {
                    //定时器到期，有玩家操作，取消定时
                    return;
                }
//                log.info("{}玩家操作超时，当前杆：{},",game.getRoomNo(),game.getGan());
                int id = game.getCurrentPlayer().getId();
                if(game.handleFoul(4)) {
                    handleGameOver(game, 0, false);
                    return;
                }

                //发送空杆消息给客户端
                int repeatFoul = game.currentPlayer.getFoul();
                log.error("操作超时");
                handleNextOpt(game,4);
                gameHandler.transfer(game.buildFoul(4, id,repeatFoul),game);
            }
        }
    }

    @Resource
    RobotMgr robotMgr;

    /**
     * 游戏结束处理。
     * @param game
     * @param uid
     * @param isDraw
     */
    private void handleGameOver(AbstractGame game, int uid, boolean isDraw) {
//      log.info("{}游戏结束,结束原因：{},强退玩家ID：{}",game.getRoomNo(),game.overCode,uid);
    	if(game.isNoviceGuide()) {
    		nociveGuideHandleGameOver(game,uid,isDraw);
    		return;
    	}
        game.setGameOver(true);
        int chang = game.getChang();
        int moneyType = (chang/1000);
//        int changId = chang%100;
        int gameType = FightUtil.chang2game(chang);// chang % 100 / 10;
        int base;// = 0;//changId==1?200:(changId==2)?1000:10000;

        double cc;// = 0.05;//抽成
        BilliardChangConfig config = excelMgr.getChangConfigMap().get(game.getChang());
        if(config!=null ) {
            cc = config.getPercentage();
            base = config.getBet();
        } else {
            log.error("游戏结算异常，场次配置不存在,场次ID：{}",game.getChang());
            return;
        }

        //和局，底分为0
        base= isDraw?0:base;
        //游戏结束后续处理
        int win = (int)(base*game.getDoubleNum()*(1-cc));

        //抽牌玩法，按剩余牌的张数结算
        if (gameType == 3 || gameType == 4) {
            int winCardNumber = 0;//game.winner.remainDrawCard();
            for (GamePlayer gp : game.playerList) {
                if(gp.getId()!=game.winner.getId()) {
                    winCardNumber+= gp.remainDrawCard();//gp.getNeedBall().size();
                }
            }
            win = (int)(base*game.getDoubleNum()*winCardNumber*(1-cc));
        }
        int aiMoney = 0;
        try {
            for(GamePlayer gp:game.playerList) {
                if(game.winner!=null && game.winner.getId()==gp.getId()) {
                    gp.setWin(true);
                }
                int lose = base*game.getDoubleNum() ;
                if (gameType == 3 || gameType == 4) {
//                    lose =  base*game.getDoubleNum() * gp.getNeedBall().size();
                    lose =  base*game.getDoubleNum() * gp.remainDrawCard();
                }
                if (gp.getId() < 0) {
                    robotMgr.giveBack(gp.getId(),moneyType,gp.isWin(),win,lose);
                    if(gp.isWin()) {
                        aiMoney = win;
                    } else {
                        aiMoney = -lose;
                    }
                    continue;
                }
                Player player = playerMgr.getPlayerByID(gp.getId());
                if (player == null) {
                    log.error("游戏结束，玩家为空，id:{}", gp.getId());
                    continue;
                }
                //扣除玩家金币钻石
                int result = 0;
                if(gp.isWin()) {
                    result = win;
                    if( moneyType==1) {
                        platformService.modifyGold(gp.getId(),win, GameEventEnum.BILLIARD_GAME.reason);
                    } else {
                        platformService.modifyDiamond(gp.getId(),win, GameEventEnum.BILLIARD_GAME.reason);
                    }

                    //更新角色经验
                    roleHandler.updateRole(player,config.getWinExp());
                } else {
                    result = -lose;
                    if( moneyType==1) {
                        platformService.modifyGold(gp.getId(),-lose, GameEventEnum.BILLIARD_GAME.reason);
                    } else {
                        platformService.modifyDiamond(gp.getId(),-lose, GameEventEnum.BILLIARD_GAME.reason);
                    }
                    //更新角色经验
                    roleHandler.updateRole(player,config.getLoseExp());
                }
                //1，刷新战力参数
//                log.info("是否强退：{},id:{}",gp.isExit(),gp.getId());
                if( !gp.isExit() && gp.getId() != uid ) {
                    FightUtil.refresh(player, gp.getCueNum(), game.chang, gp.isWin());
                    FightUtil.modifyGameTimes(player,chang, 1,moneyType,result);
                }
                playerHandler.pushGameTimes(player);
                playerService.updatePlayer(player);
                //重置匹配状态
                player.setMatchStatus(0);
                if( !gp.isExit() ) {
                    //2，完成任务
                    if (gp.isWin()) {
                        //赢多少局的任务
                        platformService.finishTask(gp.getId(), 3, TaskUtil.createTaskData(BilliardTaskType.WIN.code, 1, game.chang));
                    } else {
                        //玩多少局的任务
                        platformService.finishTask(gp.getId(), 3, TaskUtil.createTaskData(BilliardTaskType.GAME.code, 1, game.chang));
                    }
                    //一杆清，或者多少连杆的任务,排除进房即强退的情况
                    if( gp.isClearance() && uid ==0 ) {
                        platformService.finishTask(gp.getId(), 3, TaskUtil.createTaskData(BilliardTaskType.ONE_GAN.code, 1, game.chang));
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理结算信息异常：",e);
        }

        gameHandler.dispatch(game.buildGameOver(base,moneyType, cc,config.getWinExp(),config.getLoseExp(),gameType),game,uid);

        try {
            BilliardChangConfig changConfig = excelMgr.getChangConfigMap().get(chang);
            int totoalFee = 0;
            for(GamePlayer gp: game.getPlayerList()) {
                if ( gp.getId()>0) {
                    totoalFee += changConfig.getFee();
                }
            }

            //记录游戏日志
            BilliardLog billiardLog = new BilliardLog();
            billiardLog.setRoomNo(game.roomNo);
            //总台费：玩家总台费+赢家抽水
            billiardLog.setFee( (int)(base * game.doubleNum *cc) + totoalFee );
            billiardLog.setGameTime(new Date());
            billiardLog.setDoubleTimes(game.doubleNum);
            billiardLog.setChang(game.chang);
            billiardLog.setTotalCue(game.gan);
            billiardLog.setMoneyType(moneyType);
            billiardLog.setAiMoney(aiMoney);
            if( game.getPlayerList().size()==2) {
                billiardLog.setPlayer1(game.getPlayerList().get(0).getId());
                billiardLog.setPlayer2(game.getPlayerList().get(1).getId());
                billiardLog.setPlayer3(0);
            } else if( game.getPlayerList().size()==3) {
                billiardLog.setPlayer1(game.getPlayerList().get(0).getId());
                billiardLog.setPlayer2(game.getPlayerList().get(1).getId());
                billiardLog.setPlayer3(game.getPlayerList().get(2).getId());

            }
            billiardLogService.insertBilliardLog(billiardLog);
        } catch (Exception e) {
            log.error("保存游戏日志异常，房间id{}:",game.roomNo,e);
        }

        //一分钟后，双方不继续游戏，则解散房间
        final int round = game.round;
        executor.schedule(()->{
            if( game.round == round ) {
                log.error("一分钟后无人下一局，游戏解散,删除内存，房间id{}",game.roomNo);
                dismiss(0,game);
            } else {
                log.error("继续下一轮，房间id{}",game.roomNo);
//                log.info("已经开始下一轮了，游戏不解散");
            }
        },60*1000,TimeUnit.MILLISECONDS);
    }

    
		
    
    /**
	  * 游戏结束处理。
	  * @param game
	  * @param uid
	  * @param isDraw
	  */
	 private void nociveGuideHandleGameOver(AbstractGame game, int uid, boolean isDraw) {
	     log.info("{}游戏结束,结束原因：{},强退玩家ID：{}",game.getRoomNo(),game.overCode,uid);
	     game.setGameOver(true);
	     int chang = game.getChang();
	     int moneyType = (chang/1000);
	     int base;
	
	     double cc;// = 0.05;//抽成
	     BilliardChangConfig config = excelMgr.getChangConfigMap().get(game.getChang());
	     if(config!=null ) {
	         cc = config.getPercentage();
	         base = config.getBet();
	     } else {
	         log.error("游戏结算异常，场次配置不存在,场次ID：{}",game.getChang());
	         return;
	     }
	
	     //和局，底分为0
	     base= isDraw?0:base;
	     //游戏结束后续处理
	     int aiMoney = 0;
	     int playerId = 0;
	     try {
	         for(GamePlayer gp:game.playerList) {
	        	 if(gp.getId() > 0) {
	        		 playerId = gp.getId();
	        	 }
	             if(game.winner!=null && game.winner.getId()==gp.getId()) {
	                 gp.setWin(true);
	             }
	             Player player = playerMgr.getPlayerByID(gp.getId());
	             if (player == null) {
	                 log.error("新手引导游戏结束，玩家为空，id:{}", gp.getId());
	                 continue;
	             }
	             if(game.gan > 1) {
	             	FightUtil.modifyGameTimes(player,chang, 1,moneyType,0);
	             }
	             playerHandler.pushGameTimes(player);
	             //重置匹配状态
	             player.setMatchStatus(0);
	         }
	     } catch (Exception e) {
	         log.error("处理结算信息异常：",e);
	     }
	     S2C c2s = new S2C();
	     c2s.setCid(Cmd.GAME_SETTLE);
	     gameHandler.dispatch(c2s,game,uid);
	
	     try {
	         //记录游戏日志
	         BilliardLog billiardLog = new BilliardLog();
	         billiardLog.setRoomNo(game.roomNo);
	         billiardLog.setFee( (int)(base * game.doubleNum *cc));
	         billiardLog.setGameTime(new Date());
	         billiardLog.setDoubleTimes(game.doubleNum);
	         billiardLog.setChang(game.chang);
	         billiardLog.setTotalCue(game.gan);
	         billiardLog.setMoneyType(moneyType);
	         billiardLog.setAiMoney(aiMoney);
	         if( game.getPlayerList().size()==2) {
	             billiardLog.setPlayer1(game.getPlayerList().get(0).getId());
	             billiardLog.setPlayer2(game.getPlayerList().get(1).getId());
	             billiardLog.setPlayer3(0);
	         } else if( game.getPlayerList().size()==3) {
	             billiardLog.setPlayer1(game.getPlayerList().get(0).getId());
	             billiardLog.setPlayer2(game.getPlayerList().get(1).getId());
	             billiardLog.setPlayer3(game.getPlayerList().get(2).getId());
	
	         }
	         billiardLogService.insertBilliardLog(billiardLog);
	     } catch (Exception e) {
	         log.error("保存游戏日志异常，房间id{}:",game.roomNo,e);
	     }
	     //一分钟后，双方不继续游戏，则解散房间
	     final int round = game.round;
	     executor.schedule(()->{
	         if( game.round == round ) {
	//             log.info("一分钟后无人下一局，游戏解散。");
	             dismiss(0,game);
	         } else {
	//             log.info("已经开始下一轮了，游戏不解散");
	         }
	     },60*1000,TimeUnit.MILLISECONDS);
	     
//	     Player player = playerMgr.getPlayerByID(playerId);
//	     List<PlayerCue> collect = player.getCueList().stream().filter(playerCue -> playerCue.getIsUse() == 1).collect(Collectors.toList());
//	     if( collect!=null && collect.size()>0) {
//	         PlayerCue playerCue = collect.get(0);
//	         if (playerCue != null) {
//	             //判断球杆维护时间是否过期
//	             if (playerCue.getDefendDay() < System.currentTimeMillis()) {
//	                 int defendTimes = playerCue.getDefendTimes();
//	                 //判断球杆使用次数是否已用完，没有则扣减
//	                 if (defendTimes > 0 && game.getGanNum() > 1) {
//	                     playerCue.setDefendTimes(0);
//	                 }
//	                 //将损坏情况推送给客户端
//	                 S2C s2c = new S2C();
//	                 s2c.setCid(Cmd.DAMAGE_CUE);
//	                 BilliardProto.S2C_DefendCue.Builder builder = BilliardProto.S2C_DefendCue.newBuilder();
//	                 builder.setPlayerCue(playerCue.toProto());
//	                 s2c.setBody(builder.build().toByteArray());
//	                 gameHandler.push(uid,s2c);
//	             }
//	         }
//	     }
	     
	 }
	
	 /**
	* 玩家强制逃跑
	* @param game
	* @param uid
	*/
	public void noviceExitGame(AbstractGame game, int uid) {
		game.isGameOver=true;
		game.overCode = 4;
		for(GamePlayer gp:game.playerList) {
		if( gp.getId()!=uid) {
			   game.winner = gp;
			} else{
			   gp.setExit(true);
			}
		}
		game.resetClearance();
		handleGameOver(game, uid, false);
		dismiss(uid,game);
	}

    //玩家击球操作
	public boolean playerOpt(C2S c2s, AbstractGame gameRoom) throws Exception{
        if( c2s.getUid() != gameRoom.currentPlayer.getId() ) {
//            log.error("{}非当前玩家击球。。。。{}",gameRoom.getRoomNo(),c2s.getUid());
            return false;
        }
        if ( log.isInfoEnabled() ) {
            log.info("{}玩家操作：{}",gameRoom.getRoomNo(),gameRoom.currentPlayer.getId());
        }
        //损耗球杆
        Player player = playerMgr.getPlayerByID(gameRoom.currentPlayer.getId());
        if(player!=null) {
            cueMgr.modifyCueDefend(player);
        }
        if(gameRoom.isNoviceGuide()) {
        	gameRoom.setGanNum(gameRoom.getGanNum() + 1);
        }
        BilliardProto.C2S_Batting b = BilliardProto.C2S_Batting.parseFrom(c2s.getBody());
//        log.info("击球角度：{}，白球:{}",b.getAngle(),gameRoom.getWhiteBall());
        gameRoom.setProto(b);
        gameRoom.currentPlayer.setCueNum(gameRoom.currentPlayer.getCueNum()+1);
        gameRoom.opt();
        scheduleSyncTimeOut(gameRoom);
        return true;
	}

	//解散房间，有一个玩家退出则解散。
    public void dismiss(int uid, AbstractGame gameRoom) {
        //推送解散房间
        S2C s2c = new S2C();
        s2c.setCid(Cmd.EXIT_ROOM);
        s2c.setBody(BilliardProto.C2S_ExitRoom.newBuilder().setId(uid).build().toByteArray());
        for(GamePlayer gp:gameRoom.playerList) {
//            log.info("发送玩家{}，1110",gp.getId());
            gameHandler.push(gp.getId(),s2c);
        }
        //删除缓存
        removeRoom(gameRoom.roomNo);
    }

    //再来一局
    public void newRound(Integer id, AbstractGame game) {
        boolean ok = game.newRound(id);
        if( ok ) {
//            log.info("再来一局成功，继续下一轮游戏，房间号：{}",game.roomNo);
            for(GamePlayer gp:game.playerList) {
                gp.setAgain(0);
            }
            game.reset();
        }
    }

    //同步完成
    public void noSync(AbstractGame game, int id) {
        for(GamePlayer gp:game.getPlayerList()) {
            if( gp.getId()==id) {
                gp.setNeedSyncGan(false);
            }
        }
    }

    /**
     * 获取实时游戏场次人数数据
     * @return  jsonObject
     */
    public JSONObject data() {
        JSONObject json = new JSONObject();
        List<AbstractGame> rooms = new ArrayList<>();
        long t = 2*60*60*1000;
        for(AbstractGame game:map.values()) {
            //判断是否第一杆未开，且超时10分支
            if (  (System.currentTimeMillis() - game.getStartTime() ) > t) {

                rooms.add(game);
                continue;
            }
            String key = String.valueOf(game.getChang());

            int total = 0;
            for(GamePlayer gp:game.getPlayerList()) {
                if(gp.getId()>0) {
                    total++;
                }
            }
            if( json.containsKey(key)) {
                json.put(key,json.getInteger(key)+total);
            } else {
                json.put(key,total);
            }
        }
        log.error("实时游戏数据,场次->人数：{},删除房间数量：{}",json,rooms.size());

        if( rooms.size()>0 ) {
            for (AbstractGame room : rooms) {
                if( room.getGan() > 0 ) {
                    log.error("局时超过2小时，游戏又继续进行，房间号：{}",room.getRoomNo());
                } else {
                    log.error("局时超过2小时，删除房间，房间号：{}",room.getRoomNo());
                    for(GamePlayer gp:room.getPlayerList()) {
                        Player player = playerMgr.getPlayerByID(gp.getId());
                        if( player != null ) {
                            player.setRoomNo(0);
                        }
                    }
                    removeRoom(room.getRoomNo());
                }
            }
        }
        return json;
    }

    public void printData() {
        for(AbstractGame game:map.values()) {

        }
    }

    /**
     * 玩家强制逃跑
     * @param game
     * @param uid
     */
    public void exitGame(AbstractGame game, int uid) {
        if( game.playerList.size()==2) {
            game.isGameOver=true;
            game.overCode = 4;
            for(GamePlayer gp:game.playerList) {
                if( gp.getId()!=uid) {
                    game.winner = gp;
                } else{
                    gp.setExit(true);
                    gp.setNeedSyncGan(false);
                }
            }
            game.resetClearance();
            handleGameOver(game, uid, false);
            dismiss(uid,game);
        } else if (game.playerList.size()==3) {
            int exit = 0;
            int robot = 0;
            for(GamePlayer gp:game.playerList) {
                if( gp.getId()==uid) {
                    gp.setExit(true);
                    gp.setNeedSyncGan(false);
                    exit++;
                } else if( gp.isExit()) {
                    exit++;
                }
                if( gp.getId()<0) {
                    robot++;
                }
                else if( gp.getId()< 0) {
                    //有一个人退出，则机器人也退出
                    gp.setExit(true);
                    exit++;
                }
            }
            if( exit >=2   ) {
                game.isGameOver=true;
                game.overCode = 4;
                for(GamePlayer gp:game.playerList) {
                    if( !gp.isExit()) {
                        game.winner = gp;
                    }
                }
                game.resetClearance();
                handleGameOver(game,uid, false);
            }
        } else {
            log.error("强退游戏异常。。。。，uid：{}",uid);
        }

    }
}

