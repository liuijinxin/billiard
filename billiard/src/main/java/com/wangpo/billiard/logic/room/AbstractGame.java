package com.wangpo.billiard.logic.room;

import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.billiard.consts.InitValue;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.room.ai.PointMgr;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Slf4j
public abstract class AbstractGame {
	protected Lock syncLock = new ReentrantLock();
	protected Lock snookerLock = new ReentrantLock();

	//击球后，未同步球位置的延迟时间
	public static final int SYNC_DELAY = 20;
	//操作延迟时间，默认20秒
	public static final int OPT_DELAY = 20;
	//最大加倍
	public static final int MAX_DOUBLE = 16;

	public static final GameBall shootForce = new GameBall(200, 0, 0);

	public static final double[] WHITE = {-19.293,-4.841};
	public static final double[][] POS = {
			{27.0291644,5.774879, -4.841}, {22.8635822, 5.774879,-7.246}, {24.9463733, 5.774879,-6.0435}, {24.9463733, 5.774879,-1.2335}, {27.0291644, 5.774879,-9.651},
			{27.0291644,5.774879, -2.436}, {20.7807911,5.774879, -3.6385}, {22.8635822,5.774879, -4.841}, {24.9463733,5.774879, -3.6385}, {24.9463733,5.774879, -8.4485},
			{20.7807911,5.774879, -6.0435}, {18.698, 5.774879,-4.841}, {27.0291644,5.774879, -0.031}, {27.0291644,5.774879, -7.246},{22.8635822, 5.774879,-2.436},
	};
	//球袋坐标
	protected final static GameBall[] bags = new GameBall[6];

	//游戏属性
	/** 是否机器人房间 **/
//	protected boolean isRobot = false;
	/** 开始时间，用于一开始就退出不再进来的球桌 **/
	/** 是否已经准备好 **/
	protected boolean isPrepared;
	protected long startTime = 0;
	protected long frame = 0;
	protected int roomNo;
	//场ID
	protected int chang;
	protected GamePlayer currentPlayer;
	protected boolean isGameOver = false;
	protected int round = 1;
	protected GameBall whiteBall = new GameBall();
	protected GamePlayer winner;
//	public ScheduledFuture future;
	public long lastFrameTime;
	//上杆是否空杆或者犯规，如果是，需要摆球
	protected boolean emptyRod;
	//1-正常结束，2-多次犯规，3-黑八进袋,4-玩家强退
	protected int overCode = 1;
	protected int doubleNum = 1;
	protected boolean refuseDouble = false;//是否拒绝翻倍
	protected boolean reqDouble = false;//是否有人请求翻倍
	protected int agreeDouble = 0;//同意翻倍的人数

	/**
	 * 当前杆开始计时时间
	 **/
	protected long ganTime;
	protected int rod ;
	/**
	 * 是否已经区分击球归属
	 **/
	protected volatile boolean isDivide;

	/**
	 * 当前第几杆，几点注意
	 * 1，从1开始。
	 * 2，每杆结束后(非操作后，而是要等球完全静止或者超时)
	 * 3，用于客户端同步超时调度
	 * 4，区别下面的opt操作码
	 */
	protected int gan;//当前第几杆
	/** 当前杆的玩家击球操作，保留用来客户端断线重连用。 **/
	protected BilliardProto.C2S_Batting proto;
	protected C2S syncC2S;
	//每一杆对应进球
	protected final Map<Integer, Set<Integer>> snookerMap = new ConcurrentHashMap<>();
	//总进球=按顺序
	protected List<Integer> snookerList = new ArrayList<>();
	//每一杆同步标志,防止一杆多次同步
	protected Map<Integer, Integer> syncFlag = new ConcurrentHashMap<>();
	protected Map<Integer, Integer> optFlag = new ConcurrentHashMap<>();
	/** 当前杆是否有犯规的标识，用于断线重连摆球 **/
	protected Map<Integer, Integer> foulFlag = new ConcurrentHashMap<>();
	protected List<GamePlayer> playerList = new ArrayList<>();
	protected List<GameBall> ballList = new ArrayList<>();
	/** 球拷贝，用于客户端断线重连，每次同步更新一次 **/
	protected List<GameBall> ballListCopy = new ArrayList<>();

	
	//新手引导局
	private boolean noviceGuide = false;
	//新手第几杆
	private int ganNum = 0;
	//ai随机
	protected Random aiRandom = new Random();

	//以下是抽象方法，各个玩法实现不同的游戏逻辑
	//



	abstract void opt();

	abstract void calculateNextPlayer(int illegality);

	abstract S2C buildGameInit();

	abstract boolean judgeFoul(int hitFirstBall);

	abstract boolean isOver(int illegality);

	public void initRobot() {
//		for(GamePlayer gp:playerList) {
//			if( gp.getId() < 0 ) {
//				isRobot = true;
//			}
//		}
	}

	public S2C buildGameOver(int difen, int moneyType, double cc, int winExp,int loseExp,int gameType){
			S2C c2s = new S2C();
			BilliardProto.S2C_GameSettle.Builder b = BilliardProto.S2C_GameSettle.newBuilder();
			b.setCode(overCode);
			if( winner != null ) {
				int winCardNumber = 1;//总赢牌张数
				if (gameType == 3 || gameType == 4) {
					winCardNumber = 0;
					for (GamePlayer gp : playerList) {
						if(gp.getId()!=winner.getId()) {
							winCardNumber+= gp.remainDrawCard();//gp.getNeedBall().size();
						}
					}
				}

				winner.setWin(true);
				winner.setWinNum(winner.getWinNum()+1);
				BilliardProto.GameSettlePlayer.Builder builder = BilliardProto.GameSettlePlayer.newBuilder();
				builder.setExp(winExp)
						.setMoneyType(moneyType)
						.setMoney((int) (difen * doubleNum *winCardNumber  - difen * doubleNum  *winCardNumber *cc ))
						.setId(winner.getId())
						.setHead(winner.getHead())
						.setNick(winner.getNick());
				if (gameType == 3 || gameType == 4) {
					builder.addAllCards(winner.getSet());
					if(winner.getNeedBall().size()>0) {
						for(GameBall gb:winner.getNeedBall()) {
							for(int card:winner.getSet()) {
								int num;
								if( card%13 ==0 ) {
									num = 13;
								} else {
									num =  card==53?14:(card==54?15:(card%13));
								}
								if( gb.getNumber() == num ) {
									builder.addNeedCards(card);
								}
							}
						}

					}
				}
				b.addWinner(builder.build());
			}

			for (GamePlayer gp : playerList) {
				if (winner==null || gp.getId() != winner.getId()) {
					gp.setWin(false);
					BilliardProto.GameSettlePlayer.Builder builder = BilliardProto.GameSettlePlayer.newBuilder();
					int winCardNumber = 0;//总赢牌张数
					if (gameType == 3 || gameType == 4) {
						builder.addAllCards(gp.getSet());
//						log.info("====gb'size:{}",gp.getNeedBall().size());
						if(gp.getNeedBall().size()>0) {
							winCardNumber = gp.remainDrawCard();//gp.getNeedBall().size();
							for(GameBall gb:gp.getNeedBall()) {
								for(int card:gp.getSet()) {
									int num;
									if( card%13 ==0 ) {
										num = 13;
									} else {
										num =  card==53?14:(card==54?15:(card%13));
									}
									if( gb.getNumber() == num ) {
										builder.addNeedCards(card);
									}
								}
//								log.info("====builder'size:{}",builder.getNeedCardsCount());
							}
						}
					}

					builder.setExp(loseExp)
							.setMoneyType(moneyType)
							.setMoney(-difen*doubleNum*winCardNumber)
							.setHead(gp.getHead())
							.setNick(gp.getNick())
							.setId(gp.getId());
					b.addLosers(builder.build());
				}
			}
			c2s.setCid(Cmd.GAME_SETTLE);
			c2s.setBody(b.build().toByteArray());
			return c2s;
	}

	/**
	 * 将所有玩家的一杆清台置为false
	 */
	public void resetClearance(){
		for(GamePlayer gp:playerList) {
			gp.setClearance(false);
		}
	}

	/**
	 * 将其他玩家的一杆清台置为false
	 * @param gamePlayer 胜利的玩家
	 */
	public void resetOtherClearance(GamePlayer gamePlayer){
		for(GamePlayer gp:playerList) {
			if (gp.getId() != gamePlayer.getId()) {
				gp.setClearance(false);
			}
		}
	}

	abstract void snooker(C2S c2s);

	abstract double aiOpt();

	abstract void gameOverDraw();

	abstract void gameOver(boolean isCurrent);

	abstract boolean syncGan(int gan);

	abstract boolean divide();

	abstract boolean canContinue();

	abstract boolean handleFoul(int illegal);

	/**
	 * 计算倒计时
	 *
	 * @return
	 */
	public int calculateTimer() {
		int t = (int) (System.currentTimeMillis() - this.ganTime) / 1000;

		if (t > rod) {
			return 0;
		}
		return rod - t - 2;
	}

	//玩家点击再来一局
	public synchronized boolean newRound(int uid) {
		boolean ok = true;
		for (GamePlayer gp : playerList) {
			if (gp.getId() == uid) {
				gp.setAgain(1);
			} else if (gp.getAgain() == 0) {
				ok = false;
			}
		}
		return ok;
	}

	//同步球之后的处理
	abstract void afterSync();
	// 以下是公用方法，多种玩法公用
	//
	public boolean isThreeFoul( ) {
		currentPlayer.addFoul();
		//测试改成30次
		if (currentPlayer.getFoul() >= 3) {
			overCode = 2;
			resetClearance();
			gameOver(false);
			return true;
		}
		return false;
	}

	/**
	 * 摆白球的时候，也需要修改白球的刚体
	 * @param c2s
	 * @throws Exception
	 */
	public void layBall(C2S c2s) throws Exception {
//		BilliardProto.C2S_LayBall proto = BilliardProto.C2S_LayBall.parseFrom(c2s.getBody());
//		whiteBall.setX(proto.getPosition().getX());
//		whiteBall.setY(-proto.getPosition().getZ());
//		whiteBall.setZ(proto.getPosition().getY());

//		whiteBall.setAx(proto.getAngle().getX());
//		whiteBall.setAy(proto.getAngle().getY());
//		whiteBall.setAz(proto.getAngle().getZ());
//		whiteBall.setAw(proto.getAngle().getW());
//
//		whiteBall.setBx(proto.getBody().getX());
//		whiteBall.setBy(proto.getBody().getY());
//		whiteBall.setBz(proto.getBody().getZ());

//		if( proto.getDropStatus() == 2) {
//			log.error("客户端摆球后白球位置:{}",whiteBall);
//		}
//		modifySyncC2S(1);
//
	}

	public void sleepLayBall(C2S c2s) throws Exception {
		BilliardProto.C2S_LayBall proto = BilliardProto.C2S_LayBall.parseFrom(c2s.getBody());
//		log.error("休眠摆球：{},{}",proto.getPosition().getX(),-proto.getPosition().getZ());
		whiteBall.setX(proto.getPosition().getX());
		whiteBall.setY(-proto.getPosition().getZ());
		whiteBall.setZ(proto.getPosition().getY());

		whiteBall.setAx(proto.getAngle().getX());
		whiteBall.setAy(proto.getAngle().getY());
		whiteBall.setAz(proto.getAngle().getZ());
		whiteBall.setAw(proto.getAngle().getW());

		whiteBall.setBx(proto.getBody().getX());
		whiteBall.setBy(proto.getBody().getY());
		whiteBall.setBz(proto.getBody().getZ());

//		if( proto.getDropStatus() == 2) {
//			log.error("客户端摆球后白球位置:{}",whiteBall);
//		}
//		modifySyncC2S(1);
//
	}

	public void modifySyncC2S(int flag) {
		//TODO 移除进球
		BilliardProto.C2S_SyncPos2 builder =null;
		BilliardProto.C2S_SyncPos2.Builder newBuilder =BilliardProto.C2S_SyncPos2.newBuilder();
		try{
			builder = BilliardProto.C2S_SyncPos2.parseFrom(syncC2S.getBody());
			newBuilder.setGan(gan);
			newBuilder.setHitFirstBall(builder.getHitFirstBall());
			newBuilder.setHitKu(builder.getHitKu());
			newBuilder.setPlayerID(builder.getPlayerID());
			for(BilliardProto.GameBall gb:builder.getBallsList()) {
				boolean exist = false;
				for(GameBall ball:ballList) {
					if( ball.getNumber() == gb.getId()) {
						exist = true;
						break;
					}
				}
				if( exist   ) {
					newBuilder.addBalls(gb);
				} else if( gb.getId()==0 ) {
					if( flag==1) {
						log.error("修改同步包白球位置：{}{}",gb.getPosition().getX(),gb.getPosition().getZ());
					}

					BilliardProto.GameBall.Builder whiteBuilder = BilliardProto.GameBall.newBuilder();
					whiteBuilder.setAngle(gb.getAngle());
					whiteBuilder.setBody(gb.getBody());
					whiteBuilder.setId(gb.getId());
					whiteBuilder.setPosition(BilliardProto.Vec3.newBuilder().setX(whiteBall.x).setY(gb.getPosition().getY()).setZ(-whiteBall.y));
					newBuilder.addBalls(whiteBuilder);
				}
			}
			log.error("超时同步{}，球数量:{},球桌球数量：{}",roomNo,newBuilder.getBallsCount(),ballList.size());
			syncC2S.setBody(newBuilder.build().toByteArray());
		} catch (Exception e) {

		}
	}

	//每局重置信息
	public void reset() {
//		log.info("新回合开始，房间号：{}，回合：{}",roomNo,round);
		this.isGameOver = false;
		this.overCode = 1;
		this.isPrepared = false;
//		this.future = null;
		this.winner = null;
		this.reqDouble = false;
		this.refuseDouble = false;
		this.agreeDouble = 0;
		this.isDivide = false;
		this.gan = 0;//重置当前杆数
		this.doubleNum = 1;//重置加倍倍数
		this.syncFlag.clear();
		this.optFlag.clear();
		this.foulFlag.clear();
		this.snookerMap.clear();
		this.snookerList.clear();
		initBall();

//		firstOpt();
		this.round++;//每轮加1，用于计算双方比分

		this.emptyRod = false;
		this.whiteBall.setX(WHITE[0]);
		this.whiteBall.setY(WHITE[1]);
		this.snookerMap.clear();

	}

	//同步球袋位置
	public void syncDeskInfo(C2S c2s){
		try {
			BilliardProto.C2S_SyncDesk proto = BilliardProto.C2S_SyncDesk.parseFrom(c2s.getBody());
			for (BilliardProto.GameBall gameBall : proto.getPocketsList()) {
//				log.info("syncDeskInfo,ball[{}][{}][{}]",
//						gameBall.getPosition().getX(),
//						gameBall.getPosition().getY(),
//						gameBall.getPosition().getZ());
				if( gameBall.getId() <1 ||gameBall.getId()>15) {
					continue;
				}
				bags[gameBall.getId() - 1] = new GameBall(gameBall.getPosition().getX(), -gameBall.getPosition().getZ());
			}
		} catch (Exception e) {
			log.error("解析syncDeskInfo异常：",e);
		}
	}

	//初始化球
	public void initBall(){
		ballList.clear();
		ballListCopy.clear();
		for (int i = 0; i < 15; i++) {
			GameBall ball = new GameBall();
			ball.setNumber(i + 1);
			ball.setX(POS[i][0]);
			ball.setY(POS[i][2]);
//			ball.setZ(POS[i][2]);
			ballList.add(ball);
		}

		for (int i = 0; i < 15; i++) {
			GameBall ball = new GameBall();
			ball.setNumber(i + 1);
			ball.setX(POS[i][0]);
			ball.setY(POS[i][2]);
			ball.setZ(POS[i][1]);
			ballListCopy.add(ball);
		}
	}

	//轮流下一个玩家操作
	public void robinOptPlayer(){
		int index = -1;
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).getId() == currentPlayer.getId()) {
				index = i;
				break;
			}
		}

		if (index == playerList.size() - 1) {
			currentPlayer = playerList.get(0);
			if( currentPlayer.isFoulFail() || currentPlayer.isExit() ) {
				currentPlayer = playerList.get(1);
			}
		} else {
			currentPlayer = playerList.get(index + 1);
			if( currentPlayer.isFoulFail() || currentPlayer.isExit()) {
				if(index==0) {
					currentPlayer = playerList.get(2);
				} else if( index==1) {
					currentPlayer = playerList.get(0);
				}
			}
		}
	}



	public S2C aiLayBall() {
		//TODO ai摆球，需要客户端桌面规范化后做
		GameBall gameBall = PointMgr.layBall(this.ballList, currentPlayer.getNeedBall(), bags);
		if (gameBall == null) {
			log.error("不摆球。");
			return null;
		} else {
//			whiteBall.setX(gameBall.getX());
//			whiteBall.setY(gameBall.getY());
			log.error("=====机器人摆球：{}", whiteBall);
			this.emptyRod = false;
//					aiMgr.submitAI(this);

			BilliardProto.C2S_LayBall.Builder b = BilliardProto.C2S_LayBall.newBuilder()
					.setPlayerID(currentPlayer.getId())
					.setDropStatus(2)
					.setPosition(BilliardProto.Vec3.newBuilder().setX(gameBall.getX()).setY(5.77488295429).setZ(-gameBall.getY()));

			S2C s2c = new S2C();
			s2c.setCid(Cmd.LAY_BALL);
			s2c.setBody( b.build().toByteArray());
			return s2c;
		}
	}

	public boolean redouble(int max){
		if( this.doubleNum>=max ) {
			return false;
		}
		this.doubleNum *=2;
		agreeDouble = 0;
		return true;
	}

	public C2S buildAIProto(double angle) {
		C2S c2s = new C2S();
		c2s.setCid(Cmd.PLAYER_OPT);

		//TODO 强弱AI命中几率
		int r = aiRandom.nextInt(100);
		boolean strongAi = currentPlayer.isStrongAI();
		boolean hit = false;
		if(noviceGuide && !strongAi && r < 5) {
			hit = true;
			log.info("新手引导弱AI随机到命中");
		}else if( (strongAi && r< InitValue.STRONG_AI_HIT_RATE) ) {
			hit = true;
			log.info("强AI随机到命中");
		} else if( (!strongAi && r< InitValue.WEAK_AI_HIT_RATE)) {
			hit = true;
			log.info("弱AI随机到命中");
		}
		if(!hit) {
			//随机角度，不命中
			r = aiRandom.nextInt(3)+1;
			angle = angle - r;
		}

		GameBall gameBall = new GameBall(0, 0, 0);
		double scale = aiRandom.nextInt(50)/100.0 + 0.5;
//		log.info("ai scale:{}",scale);
//		if (scale < 0.4) scale = 0.4;
		GameBall.rotateY(gameBall,
				GameBall.mul(shootForce, scale),
				new GameBall(0, 0, 0),
				PointMgr.degreeToRadians(angle));

//		log.info("ai 击球角度：{}",angle);
		c2s.setBody(BilliardProto.C2S_Batting.newBuilder()
				.setAngle(angle)
				.setPlayerID(currentPlayer.getId())
				.setPowerScale(scale)
//				.setVelocity(BilliardProto.Vec3.newBuilder().setX(gameBall.x).setY(gameBall.y).setZ(gameBall.z))
				.setForce(BilliardProto.Vec3.newBuilder().setX(400).setY(0).setZ(0).build())
				.setGasserAngle(0)
				.setContactPoint(BilliardProto.Vec2.newBuilder().setX(0).setY(0))
				.build().toByteArray());
		return c2s;
	}

	public C2S buildAngleProto(double angle) {
		C2S c2s = new C2S();
		c2s.setCid(1103);
		c2s.setBody(BilliardProto.C2S_CueMove.newBuilder()
				.setPlayerID(currentPlayer == null ? 0 : currentPlayer.getId())
				.setAngle(BilliardProto.Vec3.newBuilder().setX(0).setY(angle).setZ(0))
				.setPosition(BilliardProto.Vec3.newBuilder().setX(whiteBall.x).setY(5.774879).setZ(whiteBall.z))
				.build().toByteArray());
		return c2s;
	}

	public S2C buildOptPlayer(int foul) {
		S2C s2c = new S2C();
		BilliardProto.S2C_OptPlayer.Builder b = BilliardProto.S2C_OptPlayer.newBuilder();
		b.setId(currentPlayer.getId());
		b.setGan(gan);
		b.setLayBall(foul);


		b.setEndTime(calculateTimer());
//		log.info("倒计时：{}",b.getEndTime());
		s2c.setCid(Cmd.OPT_PLAYER);
		s2c.setBody(b.build().toByteArray());
		return s2c;
	}

	public C2S buildFoul(int illegality, int playerID,int repeatFoul) {
		C2S c2s = new C2S();
		c2s.setCid(1107);
		c2s.setBody(BilliardProto.S2C_Foul.newBuilder()
				.setFoul(illegality)
				.setRepeatFoul(repeatFoul)
				.setPlayerID(playerID)
				.build().toByteArray());
		return c2s;
	}

}
