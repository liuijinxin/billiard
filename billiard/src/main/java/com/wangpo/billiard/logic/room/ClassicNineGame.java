package com.wangpo.billiard.logic.room;

import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.room.ai.PointMgr;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.match.MatchPlayer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 经典黑八玩法
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
 *         .............................................
 */
@Slf4j
public class ClassicNineGame extends AbstractGame {
	private static final Random random = new Random();
	/**
	 * 记录当前操作次数
	 * 1，从1开始，
	 * 2，每次击球后
	 * 3，用于玩家击球超时调度
	 */
//	private final AtomicInteger opt = new AtomicInteger(0);//当前第几次操作
	//    private long currentId;//当前操作玩家
	/**
	 * 当前杆开始计时时间
	 **/


	//    private final List<Integer> snookerList = new ArrayList<>();
	public ClassicNineGame() {
	}

	//只同步一次
//	public synchronized boolean  syncGan() {
//		if( syncFlag.get(this.gan) !=null) return true;
//		syncFlag.put(this.gan,1);
//		return false;
//	}

	//等待所有玩家同步后
	@Override
	public synchronized boolean  syncGan(int gan) {
		syncFlag.merge(gan, 1, Integer::sum);
//		if( syncFlag.get(gan) == 1) return true;
//		return false;
		int max = 0;
		for(GamePlayer gp:playerList) {
			if( gp.getId()>0) {
				max++;
			}
		}
		if( syncFlag.get(this.gan) >= max ) {
			return true;
		}
		return false;
	}
//	public void init(GameProto.M2R_Match match) {
//		this.round = 1;
//		for (GameProto.GamePlayer p : match.getGpList()) {
//			GamePlayer gp = new GamePlayer();
//			gp.fromProto(p);
//			playerList.add(gp);
//		}
//		roomNo = match.getRoomNo();
//		reset();
//	}
	
	public void noviceGuideInit(List<MatchPlayer> matchPlayers, int roomNo,int aiRate) {
	this.round = 1;
//    this.chang = match.getChang();
//	log.info("初始化房间：{}",roomNo);
	for (MatchPlayer p : matchPlayers) {
		GamePlayer gp = new GamePlayer();
		gp.fromMatchPlayer(p, aiRate);
//		log.info("初始化玩家：{},房间：{}",gp.getId(),roomNo);
		playerList.add(gp);
	}
	this.roomNo = roomNo;
	noviceGuideReset();
}

	/**
	 * 新一轮游戏初始化
	 */
	public void noviceGuideReset() {
		super.reset();
	
		for (GamePlayer gamePlayer : playerList) {
			gamePlayer.reset();
			gamePlayer.getNeedBall().addAll(this.ballList.stream().filter(gameBall -> gameBall.getNumber()!=8).collect(Collectors.toList()));
			if(gamePlayer.getId() > 0) {
				currentPlayer = gamePlayer;
			}
		}
	
		this.isDivide = false;
	
	//	this.gan++;
		ganTime = System.currentTimeMillis();
	}

	public void init(List<MatchPlayer> matchPlayers, int roomNo,int aiRate) {
		this.round = 1;
//        this.chang = match.getChang();
//		log.info("初始化房间：{}",roomNo);
		for (MatchPlayer p : matchPlayers) {
			GamePlayer gp = new GamePlayer();
			gp.fromMatchPlayer(p, aiRate);
//			log.info("初始化玩家：{},房间：{}",gp.getId(),roomNo);
			playerList.add(gp);
		}
		this.roomNo = roomNo;
		reset();
	}

	/**
	 * 新一轮游戏初始化
	 */
	@Override
	public void reset() {
		super.reset();

		for (GamePlayer gamePlayer : playerList) {
			gamePlayer.reset();
			gamePlayer.getNeedBall().addAll(this.ballList.stream().filter(gameBall -> gameBall.getNumber()!=8).collect(Collectors.toList()));
		}

		this.isDivide = false;

//      测试机器人先开球
//		currentPlayer = null;
//		for(GamePlayer gp:playerList) {
//			if( gp.getId() < 0 ) {
//				currentPlayer = gp;
//				break;
//			}
//		}
//		if( currentPlayer == null ) {
			currentPlayer = playerList.get(random.nextInt(playerList.size()));
//		}

//		this.gan++;
		ganTime = System.currentTimeMillis();
	}

	//玩家操作，非AI
	@Override
	public void opt() {
		if (this.isGameOver) {
			return;
		}
		//操作之后，重置空杆
		this.emptyRod = false;
		optFlag.put(this.gan,1);
		if( this.gan==0) {
			this.gan =1;
		}
		//操作码递增，解除操作超时
//		this.opt.incrementAndGet();
		//等待客户端同步位置
//		scheduleSyncTimeOut();
	}

	/**
	 * 计算下一轮操作者
	 *
	 * @param foul 是否犯规
	 */
	@Override
	public void calculateNextPlayer(int foul) {
		boolean isChange = canContinue();
		if (!isChange || foul > 0) {
			if( currentPlayer.getNeedBall().size()<7) {
				//如果已经进了球，则中断一杆清
				currentPlayer.setClearance(false);
			}
			robinOptPlayer();
		}
		this.gan++;
		ganTime = System.currentTimeMillis();
		if( foul > 0 ) {
			this.emptyRod = true;
		}
	}




	@Override
	public double aiOpt() {
		//如果有空杆，需要ai摆球
		double angle;
		if (this.gan == 1) {
			angle = (random.nextInt(100) - 50)/100.0;
		} else {
//			log.info("ai 白球位置：{}",whiteBall);
			if( currentPlayer.getNeedBall().size()<1) {
				List<GameBall> list = new ArrayList<>();
				Optional<GameBall> heiba = this.ballList.stream().filter(gameBall -> gameBall.getNumber()==8).findFirst() ;
				heiba.ifPresent(list::add);
				angle = PointMgr.calculateAngle(this.ballList, list, whiteBall, bags);
			} else {
				angle = PointMgr.calculateAngle(this.ballList, currentPlayer.getNeedBall(), whiteBall, bags);
			}
			//转换成客户端角度
			angle = angle > 180 ? angle - 360 : angle;
		}
		return angle;

//			if (currentPlayer.getId() == 21)
//				log.info("机器人发送击球信息,击球角度：{}", angle);
	}




	/**
	 * 台球落袋，不需要转发
	 */
	@Override
	public void snooker(C2S c2s) {
		GamePlayer gp = getGamePlayer(c2s.getUid());
		if (gp == null) {
			log.error("斯诺克进球，找不到玩家ID：{}", c2s.getUid());
			return;
		}


		Set<Integer> balls = new HashSet<>();
		try {
			BilliardProto.C2S_Snooker proto = BilliardProto.C2S_Snooker.parseFrom(c2s.getBody());
			balls.addAll(proto.getNumbersList());
			if(proto.getNumbersList()!=null && proto.getNumbersList().size()>0) {
				if(!snookerList.contains(proto.getNumbersList().get(0))) {
					if(proto.getNumbersList().get(0)!=0) {
						snookerList.add(proto.getNumbersList().get(0));
					}
				}
			}
		} catch (Exception e) {
			log.error("斯诺克-解析异常：", e);
		}

//		log.error("进球：{}", balls);
		log.error("进球：{},id:{}", balls,currentPlayer.getId());
		if (balls.size() > 0) {
			Set<Integer> oldBalls = this.snookerMap.get(this.gan);
			if (oldBalls != null) {
				oldBalls.addAll(balls);
				this.snookerMap.put(this.gan, oldBalls);
			} else {
				this.snookerMap.put(this.gan, balls);
			}

//            snookerList.addAll(balls);
		} else {
			log.error("斯诺克进球，进球为空！！！！");
			return ;
		}
		if (balls.contains(8)) {
			log.error("进黑八，当前玩家id:{}，是否分球：{},剩余球：{}",currentPlayer.getId(),
					isDivide,currentPlayer.getNeedBall().size());
		}
		//移除球桌和玩家身上的台球
//		log.info("进球前：{}",ballList.size());
		removeBall(ballList, balls);
		removeBall(ballListCopy,balls);
//		log.info("进球后：{}",ballList.size());
		for (GamePlayer gamePlayer : playerList) {
			removeBall(gamePlayer.getNeedBall(), balls);
		}
	}

	public GamePlayer getReverseGamePlayer(int id) {
		for (GamePlayer gamePlayer : playerList) {
			if (gamePlayer.getId() != id) {
				return gamePlayer;
			}
		}
		return null;
	}

	public int gan(){
		return this.gan;
	}

	public boolean isSync() {
		return this.gan>1 && syncFlag.get(this.gan) != null;
	}

//	public void ganSync() {
//		syncFlag.put(this.gan,1);
//	}

	public Set<Integer> ganSnooker(){
		if( !this.snookerMap.containsKey(this.gan)) {
			return null;
		}
		return this.snookerMap.get(this.gan);
	}

	//获取玩家对象
	public GamePlayer getGamePlayer(int id) {
		GamePlayer gp = null;
		for (GamePlayer gamePlayer : playerList) {
			if (gamePlayer.getId() == id) {
				gp = gamePlayer;
				break;
			}
		}
		return gp;
	}

	//移除已进的球
	private void removeBall(List<GameBall> gameBalls, Set<Integer> balls) {
		try{
			snookerLock.lock();
			gameBalls.removeIf(next -> balls.contains(next.getNumber()));
		} finally {
			snookerLock.unlock();
		}
	}

	/**
	 * 判断归属问题
	 */
	@Override
	public boolean divide( ) {
		if (!isDivide) {
			Set<Integer> balls = getSnookerMap().get(this.gan);
			boolean hasBig = false;
			boolean hasSmall = false;
			for(Integer i:balls) {
				if( i==0 || i==8) {
					return false;
				}
				if(i>8) {
					hasBig = true;
				}
				if( i<8) {
					hasSmall = true;
				}
			}
			if( hasBig && hasSmall) {
				return false;
			}

			GamePlayer other =  other();
			currentPlayer.getNeedBall().clear();
			other.getNeedBall().clear();
			if( hasBig ) {
				currentPlayer.setSide(2);
				other.setSide(1);
				ballList.forEach((ball) -> {
					if ( ball.getNumber() > 8  ) {
						currentPlayer.getNeedBall().add(ball);
					} else if( ball.getNumber()<8) {
						other.getNeedBall().add(ball);
					}
				});

			} else {
				currentPlayer.setSide(1);
				other.setSide(2);
				ballList.forEach((ball) -> {
					if ( ball.getNumber() < 8  ) {
						currentPlayer.getNeedBall().add(ball);
					} else if( ball.getNumber()>8) {
						other.getNeedBall().add(ball);
					}
				});
			}
			isDivide = true;

		}
		return isDivide;
	}
/*if (balls.size() > 0 && this.gan > 1 && balls.get(0) != 0 && balls.get(0) != 8) {
				//开始解决归属问题，未解决归属问题前，need ball为所有求
				for (GamePlayer gamePlayer : playerList) {
					gamePlayer.getNeedBall().clear();
					if (gamePlayer.getId() == currentPlayer.getId()) {
						if (balls.get(0) < 8) {
							gamePlayer.setSide(1);
						} else {
							gamePlayer.setSide(2);
						}
						ballList.forEach((ball) -> {
							if ((ball.getNumber() < 8 && balls.get(0) < 8)
									|| (ball.getNumber() > 8 && balls.get(0) > 8)) {
								gamePlayer.getNeedBall().add(ball);
							}
						});
					} else {
						if (balls.get(0) < 8) {
							gamePlayer.setSide(2);
						} else {
							gamePlayer.setSide(1);
						}
						ballList.forEach((ball) -> {
							if ((ball.getNumber() > 8 && balls.get(0) < 8)
									|| (ball.getNumber() < 8 && balls.get(0) > 8)) {
								gamePlayer.getNeedBall().add(ball);
							}
						});
					}
				}
				isDivide = true;
			}*/

	@Override
	public S2C buildGameInit( ) {
		S2C s2c = new S2C();
		BilliardProto.S2C_RoomInfo.Builder b = BilliardProto.S2C_RoomInfo.newBuilder();
		if (isPrepared) {
			b.setOptPlayer(currentPlayer.getId());
		}
		b.setRoomNo(roomNo);
		b.setGan(this.gan);
		b.setChangId(chang);
		b.setDoubleNum(doubleNum);
		b.setDivide(isDivide?1:0);

		b.setRemainTime(calculateTimer()-1);
		if( proto!=null) {
			b.setProto(proto);
		}


		if(snookerList.size()>0) {
			log.error("重连，落袋球数量：{}",snookerList.size());
			b.addAllSnookerList(snookerList);
		}
		for (GamePlayer gp :playerList) {
//			log.info("头像：{}，id：{}",gp.getHead(),gp.getId());
			b.addPlayers(gp.toProto2(isDivide));
//			log.error("初始化玩家待进球：{}",gp.toProto2().getBallsList());
		}
//		log.error("ballListCopy:{}",ballListCopy.size());
		for (GameBall ball : ballListCopy) {
			b.addBalls(BilliardProto.GameBall.newBuilder()
					.setPosition(BilliardProto.Vec3.newBuilder().setX(ball.x).setY(-ball.y).setZ(ball.z))
					.setAngle(BilliardProto.Vec4.newBuilder().setX(ball.ax).setY(ball.ay).setZ(ball.az).setW(ball.aw))
					.setBody(BilliardProto.Vec3.newBuilder().setX(ball.bx).setY(ball.by).setZ(ball.bz) )
					.setId(ball.getNumber()).build());
		}
		b.addBalls(BilliardProto.GameBall.newBuilder()
				.setPosition(BilliardProto.Vec3.newBuilder().setX(whiteBall.x).setY(-whiteBall.y).setZ(whiteBall.z))
				.setAngle(BilliardProto.Vec4.newBuilder().setX(whiteBall.ax).setY(whiteBall.ay).setZ(whiteBall.az).setW(whiteBall.aw))
				.setBody(BilliardProto.Vec3.newBuilder().setX(whiteBall.bx).setY(whiteBall.by).setZ(whiteBall.bz) )
				.setId(whiteBall.getNumber()).build());

		s2c.setCid(Cmd.INIT_ROOM);
		s2c.setBody(b.build().toByteArray());
		return s2c;
	}

	/**
	 * 判断游戏是否结束
	 */
	@Override
	public boolean isOver(int illegality) {
		Set<Integer> balls = ganSnooker();
//		if( balls==null || balls.size()<1) return false;
		for(GameBall gb:ballList) {
			if( gb.getNumber() == 8) {
				return false;
			}
		}
		//1,先判断是否有归属
		if (balls!=null && isDivide && balls.size() == 1 && currentPlayer.getNeedBall().size() < 1) {
			//如果当前游戏杆数和玩家连杆一样，一杆清台
//			if ( currentPlayer.getManyCue() >= 7) {
//				currentPlayer.clearance = true;
//			}
			//进黑8，直接获胜
			if( illegality>0) {
				log.error("游戏结束，只进黑八，但是犯规【{}】",illegality);
				resetClearance();
				//犯规判负
				overCode = 3;
				this.gameOver(false);
			} else {
				resetOtherClearance(currentPlayer);
				this.gameOver(true);
			}
		} else {
			//如果自己的球还没打完，判负
			if( currentPlayer.getNeedBall().size() >= 1 ) {
				log.error("游戏结束，进了黑八但是自己目标球未进完");
				overCode = 3;
				resetClearance();
				this.gameOver(false);
			} else {
				//自己的球已经打完，犯规判负
				if( illegality>0) {
					//犯规判负
					log.error("游戏结束，进黑八，但是犯规【{}】",illegality);
					overCode = 3;
					resetClearance();
					this.gameOver(false);
				} else {
					if (balls != null) {
						//进球中自己的球
						int totalBall = 0;
						int side = currentPlayer.getSide();
						//如果结算还没有分球，直接判负
						if (side == 0) {
							log.error("未分球进了黑八，直接判负，房间号：{}",roomNo);
							overCode = 3;
							resetClearance();
							this.gameOver(false);
						} else if (side == 1) {
							for (Integer ballNum : balls) {
								if (ballNum < 8) {
									totalBall++;
								}
							}
						} else if (side == 2) {
							for (Integer ballNum : balls) {
								if (ballNum > 8) {
									totalBall++;
								}
							}
						}
						//自己需要进的球，加上打进自己的球数量，如果大于1，此回合前球未进完，判负
						if ( currentPlayer.getNeedBall().size() + totalBall >= 1) {
							//犯规判负
							log.error("最后一杆进了自己球加黑八判负，房间号：{}",roomNo);
							overCode = 3;
							resetClearance();
							this.gameOver(false);
						} else {
							resetOtherClearance(currentPlayer);
							this.gameOver(true);
						}
					} else {
						log.error("游戏结束，居然没有进球，犯规【{}】",illegality);
						overCode = 3;
						resetClearance();
						this.gameOver(false);
					}
				}
			}

//			//进黑八，同时进了其他球，包括白球，直接判负
//			overCode = 3;
//			resetClearance();
//			this.gameOver(false);
		}
		return true;
	}


	@Override
	public boolean handleFoul(int illegality){
		if (illegality > 0) {
			if( isThreeFoul() ) {
				return true;
			}
		} else {
			currentPlayer.resetFoul();
		}
		return false;
	}

	/**
	 * 进球后是否继续击球
	 * @return
	 */
	@Override
	public boolean canContinue() {
		if (this.snookerMap.containsKey(this.gan)) {
			Set<Integer> list = this.snookerMap.get(this.gan);
			if( list!=null && list.size()>0) {
				for(Integer i:list) {
					if ( i==0 ) {
						return false;
					}
				}

				if(isDivide) {
					//进多个球，只要有自己的球即可
					boolean hasOwn = false;
					for (Integer i : list) {
						if( (currentPlayer.getSide() == 1 && i<8)||(currentPlayer.getSide() == 2 && i>8) ) {
							hasOwn = true;
							break;
						}
					}
					return hasOwn;
				} else {
					return true;
				}

			}
		}
		return false;
	}

	private boolean isOwnBall(int i) {
		if( currentPlayer.getSide() == 1 ) {
			return i<8;
		} else {
			return i>8;
		}
	}

//						if( currentPlayer.getNeedBall().size()>0) {
//							//打黑八的特殊情况
//							if (currentPlayer.getNeedBall().size() == 0) {
//								if( currentPlayer.getSide() == 1 ) {
//									return i<8;
//								} else {
//									return i>8;
//								}
//							} else if (i > 8 && currentPlayer.getNeedBall().get(0).getNumber() > 8) {
//								return true;
//							} else if (i < 8 && currentPlayer.getNeedBall().get(0).getNumber() < 8) {
//								return true;
//							}
//						}
	/**
	 * 判断是否犯规
	 *
	 * @param hitFirstBall
	 * @return
	 */
	@Override
	public boolean judgeFoul(int hitFirstBall) {
		if (!isDivide) {
			//没有归属，第一次击球为黑八时犯规
			return hitFirstBall == 8;
		}
		//所有球都进了，就打黑8
		Set<Integer> s = ganSnooker();
		if (currentPlayer.getNeedBall().size() < 1) {
			if( s==null || s.size()<1 ) {
				return hitFirstBall != 8;
			} else {
				if( !currentPlayer.isBlack() ) {
					if( currentPlayer.getSide()==1 ) {
						//进最后一个球，结果是先碰大球
						return hitFirstBall>=8;
					} else {
						//进最后一个球，结果是先碰大球
						return hitFirstBall<=8;
					}
				} else {
					//已经进黑八了，就只判断碰球是否是黑八
					return hitFirstBall != 8;
				}
			}
		} else {
			//TODO 判断是否进了黑八
			if ( s!=null) {
				for(Integer i:s) {
					if( i==8 ) {
						return true;
					}
				}
			}
		}
		//判断是否进了白球
		/*boolean whiteBall = false;
		Set<Integer> s = ganSnooker();
		if( s!=null && s.size()>0 ) {
			for(Integer id:s) {
				if( id==0) {
					whiteBall = true;
					break;
				}
			}
		}
		if( whiteBall ) return true;*/
		if (hitFirstBall < 8 && currentPlayer.getNeedBall().get(0).getNumber() < 8) {
			return false;
		}
		if (hitFirstBall > 8 && currentPlayer.getNeedBall().get(0).getNumber() > 8) {
			return false;
		}
		return true;
	}

	//进球之后的处理
	@Override
	public void afterSync(){
		for(GamePlayer gp:playerList) {
			//设置是否开始打黑八
			if( gp.getNeedBall().size()<1) {
				gp.setBlack(true);
			}
		}
	}




	//和局
	@Override
	public void gameOverDraw() {
		this.isGameOver = true;
	}

	/**
	 * 游戏结算
	 *
	 * @param isCurrent 是否当前操作者胜利
	 */
	@Override
	public void gameOver(boolean isCurrent) {
		this.isGameOver = true;
		if( isCurrent ) {
			this.winner = currentPlayer;
		} else {
			this.winner = other();
		}
	}

	private GamePlayer other(){
		for(GamePlayer gp:playerList) {
			if( gp.getId() != currentPlayer.getId()) {
				return gp;
			}
		}
		return null;
	}





	public int currentId() {
		return currentPlayer == null ? 0 : currentPlayer.getId();
	}

	@Override
	public int getRoomNo() {
		return this.roomNo;
	}




	//是否游戏结束
	@Override
	public boolean isGameOver() {
		return this.isGameOver;
	}

}
