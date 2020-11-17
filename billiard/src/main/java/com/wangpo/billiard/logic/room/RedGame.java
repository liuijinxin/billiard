package com.wangpo.billiard.logic.room;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.match.MatchPlayer;
import com.wangpo.billiard.logic.room.ai.PointMgr;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 红球玩法
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
@Data
@Slf4j
public class RedGame extends AbstractGame {



	private static final Random random = new Random();



	/**
	 * 记录当前操作次数
	 * 1，从1开始，
	 * 2，每次击球后
	 * 3，用于玩家击球超时调度
	 */
//	private final AtomicInteger opt = new AtomicInteger(0);//当前第几次操作
	//    private long currentId;//当前操作玩家




	//    private final List<Integer> snookerList = new ArrayList<>();
	public RedGame() {
	}

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

	public void init(List<MatchPlayer> matchPlayers, int roomNo, int aiRate) {
		this.round = 1;
//        this.chang = match.getChang();
		for (MatchPlayer p : matchPlayers) {
			GamePlayer gp = new GamePlayer();
			gp.fromMatchPlayer(p, aiRate );
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
		//不需要区分大小球
		this.isDivide = true;
		//当前操作者
		currentPlayer = playerList.get(random.nextInt(playerList.size()));
//		log.info("游戏开始，下一个玩家：{}",currentPlayer.getId());
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

	@Override
	public void initBall() {
		super.initBall();

		//玩家进球为所有红球
		for(GamePlayer gp:playerList){
			for(GameBall gb:ballList) {
				if( gb.getNumber() != 8 ) {
					gp.getNeedBall().add(gb);
				}
			}
		}
	}

	public void firstOpt() {
		currentPlayer = playerList.get(random.nextInt(playerList.size()));
		this.gan++;
		ganTime = System.currentTimeMillis();
	}

	/**
	 * 计算下一轮操作者
	 *
	 * @param illegality 是否犯规
	 */
	@Override
	public void calculateNextPlayer(int illegality) {
		boolean isChange = canContinue();
		if (!isChange || illegality > 0) {
			if( currentPlayer.getSnookerList().size()>0) {
				//如果已经进了球，则中断一杆清
				currentPlayer.setClearance(false);
			}
			robinOptPlayer();
		}
		this.gan++;
		ganTime = System.currentTimeMillis();
		if( illegality > 0) {
			this.emptyRod = true;
		}
	}




	public boolean isCurrentAI(){
		return currentId()<50;
	}

	@Override
	public S2C aiLayBall() {
		GameBall gameBall = PointMgr.layBall(this.ballList, currentPlayer.getNeedBall(), bags);
		if (gameBall == null) {
			log.error("不摆球。");
			return null;
		} else {
//			whiteBall.setX(gameBall.getX());
//			whiteBall.setY(gameBall.getY());
//			log.info("=====机器人摆球：{}", whiteBall);
			this.emptyRod = false;
//					aiMgr.submitAI(this);
			S2C s2c = new S2C();
			s2c.setCid(Cmd.LAY_BALL);
			s2c.setBody(BilliardProto.C2S_LayBall.newBuilder()
					.setDropStatus(2)
					.setPlayerID(currentId())
					.setDropStatus(2)
					.setPosition(BilliardProto.Vec3.newBuilder().setX(gameBall.getX()).setY(5.77488295429).setZ(-gameBall.getY()))
					.build().toByteArray());
			return s2c;
		}
	}

	@Override
	public double aiOpt() {
		//如果有空杆，需要ai摆球
		double angle;
		if (this.gan == 1) {
			angle = (random.nextInt(100) - 50)/100.0;
		} else {
			//angle = PointMgr.calculateAngle(this.ballList, currentPlayer.getNeedBall(), whiteBall, bags);
//			log.info("ai 白球位置：{}",whiteBall);
			if( currentPlayer.getSnookerList().size()>=7) {
				List<GameBall> list = new ArrayList<>();
				list.add(this.ballList.stream().filter(gameBall -> gameBall.getNumber()==8).findFirst().get());
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
		GamePlayer gp2 = getGamePlayer(c2s.getUid());
		if (gp2 == null) {
			log.error("斯诺克进球，找不到玩家ID：{}", c2s.getUid());
			return;
		}


		Set<Integer> balls = new HashSet<>();
		int number = 0;
		try {
			BilliardProto.C2S_Snooker proto = BilliardProto.C2S_Snooker.parseFrom(c2s.getBody());
			for(Integer i:proto.getNumbersList()) {
				number = i;
				balls.add(i);
			}

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
		if( balls.size()!= 1) {
			log.error("进球异常，一次进了多个：{}",balls.size());
			return;
		}

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
		}  else {
			log.error("斯诺克进球，进球为空！！！！");
			return ;
		}

		if( number == 0 ) {

			return;
		}

		if( currentPlayer.isBlack() && !currentPlayer.getSnookerList().contains(number) ) {
			for(GamePlayer player:playerList) {
				if(player.isBlack()) {
					continue;
				}
				if( player.getId()!=currentId()) {
					if(!player.getSnookerList().contains( number)) {
						player.getSnookerList().add(number);
					}
					if( player.getSnookerList().size()>=7) {
						player.setBlack(true);
					}
					break;
				}
			}
		}
		if(!currentPlayer.getSnookerList().contains( number) ) {
			currentPlayer.getSnookerList().add(number);
		}
		if(currentPlayer.getSnookerList().size()>=7) {
			currentPlayer.setBlack(true);
		}

		//移除球桌和玩家身上的台球
		removeBall(ballList, balls);
		removeBall(ballListCopy,balls);
		for (GamePlayer gamePlayer : playerList) {
			removeBall(gamePlayer.getNeedBall(), balls);
//			log.info("进球后，玩家{} ,needBall:{}",gamePlayer.getId(),gamePlayer.getNeedBall().size());
		}

		log.info("玩家ID：{}，进球：{}",playerList.get(0).getId(),playerList.get(0).getSnookerList());
		log.info("玩家ID：{}，进球：{}",playerList.get(1).getId(),playerList.get(1).getSnookerList());

		if( syncC2S != null ) {
			try {
				BilliardProto.C2S_SyncPos2 b = BilliardProto.C2S_SyncPos2.parseFrom(syncC2S.getBody());
				BilliardProto.C2S_SyncPos2.Builder b2 = BilliardProto.C2S_SyncPos2.newBuilder();
				b2.setGan(b.getGan());
				b2.setHitFirstBall(b.getHitFirstBall());
				b2.setHitKu(b.getHitKu());
				b2.setPlayerID(b.getPlayerID());
				for(BilliardProto.GameBall gb:b.getBallsList()) {
//					if( gb.getId()==0 ) {
//						b2.addBalls(gb);
//					} else
						if( !balls.contains(gb.getId()) ) {
						b2.addBalls(gb);
					}
				}
//				b.getBallsList().removeIf(ball -> balls.contains(ball.getId()) );
				syncC2S.setBody(b2.build().toByteArray());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean isSync() {
		return this.gan>1 && syncFlag.get(this.gan) != null;
	}

//	public void ganSync() {
//		syncFlag.put(this.gan,1);
//	}

	public Set<Integer> ganSnooker(){
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
	 * 同步台球位置，不需要转发
	 * @param id  玩家id
	 * @param c2s 同步包
	 */
	/*public void syncPos(int id, C2S c2s) throws Exception {
		GamePlayer gp = getGamePlayer(id);
		if (this.gan > 1 && syncFlag.get(this.gan) != null) {
			log.error("===当前杆已经同步ok");
			return;
		}
//        System.out.println("同步："+s);
		//设置同步完成标志
		syncFlag.put(this.gan, 1);
		//if( this.isGameOver ) return;

//        JSONObject json = JSON.parseObject(s);
//        JSONArray array = json.getJSONObject("data").getJSONArray("balls");
		BilliardProto.C2S_SyncPos2 builder = BilliardProto.C2S_SyncPos2.parseFrom(c2s.getBody());
		for (BilliardProto.GameBall ballProto : builder.getBallsList()) {
//            JSONObject o  = (JSONObject)obj;
			int number = ballProto.getNubmer();// o.getInteger("number");
			double x = ballProto.getPosition().getX();// o.getJSONObject("position").getDouble("x");
			double y = ballProto.getPosition().getX();//o.getJSONObject("position").getDouble("z");
			if (number == 0) {
				whiteBall.setX(x);
				whiteBall.setY(-y);
			} else {
				//同步球桌上的球
				for (GameBall gb : ballList) {
					if (gb.getNumber() == number) {
						gb.setX(x);
						gb.setY(-y);
					}
				}
				for (GamePlayer gamePlayer : playerList) {
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

//        System.out.println(whiteBall);
//        System.out.println(ballList);
		//第一个碰撞球，用于服务端验证犯规
		int hitFirstBall = 0;//json.getJSONObject("data").getInteger("hitFirstBall");

		if (builder.getHitFirstBall() > 0)
			hitFirstBall = builder.getHitFirstBall();//json.getJSONObject("data").getInteger("hitFirstBall");
		System.out.println("hitFirstBall:" + hitFirstBall);

		List<Integer> balls = this.snookerMap.get(this.gan);
		int illegality = 0;
		if (hitFirstBall == 0) {
			illegality = 1;//空杆
		} else if (judgeFoul(hitFirstBall)) {
			illegality = 2;//打到别人的球
		}
		//判断是否白球进袋
		if (balls != null) {
			for (Integer ball : balls) {
				if (ball == 0) {
					illegality = 3;
					break;
				}
			}
		}
		//指令1111这里不需要转发
		transfer(c2s);
//        transfer(s);
		//处理之前，先判断是否有犯规
        *//*int illegality = 0;
        if( this.opt.get() >0 ) {
            if( json.getJSONObject("data").containsKey("illegality")) {
                illegality = json.getJSONObject("data").getInteger("illegality");
                System.out.println("illegality:"+illegality);
                if( illegality >0 ) {

                }
            } else {
                System.out.println("========================没有发犯规。。");
            }
        }
        log.info("犯规:{}",illegality);*//*

		//判断是否有进球
		if (balls != null && balls.size() > 0) {
			//判断归属问题
			isDivide(id, balls);

			//判断游戏是否结束
			isOver(id, gp, balls);
		}
		if (this.isGameOver) return;

		if (this.opt.get() > 0) {
//            log.info("同步球位置结束，当前操作者：{},当前杆：{}",currentId(),this.gan);
			if (illegality > 0) {
				emptyRod = true;
				currentPlayer.addFoul();
				if (currentPlayer.getFoul() >= 3) {
					this.gameOver(getReverseGamePlayer(id));
					return;
				}
			} else {
				currentPlayer.resetFoul();
			}
			handleNextOpt(illegality);

			if (illegality > 0) {
				String error = illegality == 1 ? "空杆" : (illegality == 2 ? "碰到别人的球" : "白球进洞");
				System.out.println("===============" + error);

				transfer(buildFoul(illegality, id));
			} else {

			}
		}

	}*/

	/**
	 * 判断归属问题
	 */
	@Override
	public boolean divide( ) {
		return false;
		/*if (!isDivide) {
			List<Integer> balls = getSnookerMap().get(this.gan);
			if (balls.size() > 0 && this.gan > 1 && balls.get(0) != 0 && balls.get(0) != 8) {
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
			}
		}
		return isDivide;*/
	}


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
		b.setRemainTime(calculateTimer());
		b.setDoubleNum(doubleNum);

		if(snookerList.size()>0) {
			//移除掉桌面的球？
			Iterator<Integer> it = snookerList.iterator();
			while(it.hasNext()) {
				int number = it.next();
				for(GameBall gb:ballList ) {
					if( gb.getNumber() == number) {
						it.remove();
					}
				}
			}
			b.addAllSnookerList(snookerList);
		}
		if(proto!=null) {
			b.setProto(proto);
		}
		for (GamePlayer gp :playerList) {
			BilliardProto.GamePlayer.Builder b2 = gp.toProto2(true);
//			log.info(:{},cueId:{}",b2.getId(),b2.getCueId());4
			b2.clearBalls();
			if( gp.isBlack() ) {
				for(GameBall gb:ballList) {
					if(gb.getNumber()==8) {
						b2.addBalls(gb.getNumber());
						break;
					}
				}
			} else {
				int entered = gp.getSnookerList().size();
				int need = 7-entered;
				if( need >0 ) {
					for(GameBall gb:gp.getNeedBall()) {
						b2.addBalls(gb.getNumber());
						need--;
						if( need<=0) {
							break;
						}
					}
				}
			}
//			log.info("剩余球个数：{}，玩家ID：{}",b2.getBallsCount(),gp.getId());
			b.addPlayers(b2);

		}
		for (GameBall ball : ballListCopy) {
			b.addBalls(BilliardProto.GameBall.newBuilder()
					.setPosition(BilliardProto.Vec3.newBuilder().setX(ball.x).setY(-ball.y).setZ(ball.z))
					.setAngle(BilliardProto.Vec4.newBuilder().setX(ball.ax).setY(ball.ay).setZ(ball.az).setW(ball.aw))
					.setBody(BilliardProto.Vec3.newBuilder().setX(ball.bx).setY(ball.by).setZ(ball.bz) )
					.setId(ball.getNumber()).build());
		}
//		log.error("初始化白球位置：{}",whiteBall);
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
		for(GameBall gb:ballList) {
			if( gb.getNumber() == 8) {
				return false;
			}
		}
		//1,先判断是否有归属
		if (balls !=null && balls.size() == 1 && currentPlayer.getSnookerList().size() >= 7) {
			//如果当前游戏杆数和玩家连杆一样，一杆清台
//			if ( currentPlayer.getManyCue() >= 7) {
//				currentPlayer.clearance = true;
//			}
			//进黑8，直接获胜
			if( illegality>0) {
				//犯规判负
				log.error("游戏结束，只进黑八，但是犯规【{}】",illegality);
				resetClearance();
				overCode = 3;
				this.gameOver(false);
			} else {
				resetOtherClearance(currentPlayer);
				this.gameOver(true);
			}
		} else {
			if( currentPlayer.getSnookerList().size() <7 ) {
				log.error("游戏结束，进了黑八但是自己目标球未进完");
				overCode = 3;
				resetClearance();
				this.gameOver(false);
			} else {
				if( illegality>0) {
					//犯规判负
					overCode = 3;
					resetClearance();
					this.gameOver(false);
				} else {
					if (balls != null) {
						if (currentPlayer.getSnookerList().size() - balls.size() < 7) {
							//犯规判负
							log.error("游戏结束，进黑八自己的球未打完。");
							overCode = 3;
							resetClearance();
							this.gameOver(false);
						} else {
							resetOtherClearance(currentPlayer);
							this.gameOver(true);
						}
					} else {
						//犯规判负
						log.error("游戏结束，居然进球为空。。");
						overCode = 3;
						resetClearance();
						this.gameOver(false);
					}
				}
			}
//			//进黑八，直接判负
//			overCode = 3;
//			resetClearance();
//			this.gameOver(false);
		}
		return true;
	}

	/**
	 * 判断是否犯规
	 *
	 * @param hitFirstBall
	 * @return true 犯规， false 不犯规
	 */
	@Override
	public boolean judgeFoul(int hitFirstBall) {
//		if (!isDivide) return false;
		//所有球都进了，就打黑8
		Set<Integer> s = ganSnooker();
		if ( currentPlayer.isBlack() ) {
			if(s!=null && s.size()>0) {
				if( ((currentPlayer.getSnookerList().size()-s.size()) >= 7)  ) {
					log.info("最后一杆进球，hitFirstBall：{},总进球，{}，当前进球：{}",hitFirstBall,currentPlayer.getSnookerList().size(),s.size());
					return hitFirstBall != 8;
				} else {
					return hitFirstBall == 8;
				}
			}
			if( hitFirstBall !=8 ) {
//				log.info("红球玩法，该打黑八，结果打到其他球，hitFirstBall：{}",hitFirstBall);
				return true;
			}
			return false;
		} else {
			//还有球未进
			return hitFirstBall==8;
		}
	}

	//进球之后的处理
	@Override
	public void afterSync(){

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
				if( (currentPlayer.getSnookerList().size()-list.size()) >= 7  ) {
					//已经进了7个球，再进不能继续打。
					return false;
				}
				return true;
			}
		}
		return false;
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
			for(GamePlayer gp:playerList) {
				if( gp.getId() != currentPlayer.getId()) {
					this.winner = gp;
					break;
				}
			}
		}
	}





	public int currentId() {
		return currentPlayer == null ? 0 : currentPlayer.getId();
	}

	@Override
	public int getRoomNo() {
		return this.roomNo;
	}

	@Override
	public boolean handleFoul(int illegality){
		if (illegality > 0) {
			if( isThreeFoul() ) {
				overCode = 2;
				return true;
			}
		} else {
			currentPlayer.resetFoul();
		}
		return false;
	}
	//玩家点击再来一局
	/*public boolean newRound(int playerId) {
		boolean ok = true;
		for (GamePlayer gp : playerList) {
			if (gp.getId() == playerId) {
				gp.setAgain(1);
				break;
			}
			if (gp.getAgain() != 0) {
				ok = false;
			}
		}
		if (ok) {
			for (GamePlayer gp : playerList) {
				gp.setAgain(0);
			}
			reset();
			sendInitRoom();
		}
		return false;
	}*/

	//是否游戏结束
	@Override
	public boolean isGameOver() {
		return this.isGameOver;
	}

}
