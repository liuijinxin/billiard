package com.wangpo.billiard.logic.room;

import com.wangpo.billiard.logic.Cmd;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.match.MatchPlayer;
import com.wangpo.billiard.logic.room.ai.PointMgr;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 抽牌玩法
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
public class DrawGame extends AbstractGame {



	private static final Random random = new Random();

	private int mode = 1;//1-52张牌，2-15张牌模式。

	/**
	 * 当前杆开始计时时间
	 **/
//	private long ganTime;

	public DrawGame() {
	}

	public DrawGame(int mode) {
		this.mode = mode;
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

	public void init(List<MatchPlayer> matchPlayers, int roomNo,int aiRate) {
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
		//抽牌的时候，已经设置好了ball，这里不再需要添加
		//不需要区分大小球
		this.isDivide = true;
		//当前操作者
		currentPlayer = playerList.get(random.nextInt(playerList.size()));
		//TODO 测试让机器人先击球
//		for(GamePlayer gp:playerList) {
//			if( gp.getId() < 0 ) {
//				currentPlayer = gp;
//				break;
//			}
//		}
//		this.gan++; {
		ganTime = System.currentTimeMillis();
	}

	@Override
	public boolean handleFoul(int illegality){
		return false;
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
			if( currentPlayer.getNeedBall().size() < currentPlayer.getSet().size() ) {
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
			if( currentPlayer.getNeedBall().size()<1) {
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
		GamePlayer gp = getGamePlayer(c2s.getUid());
		if (gp == null) {
			log.error("1斯诺克进球，找不到玩家ID：{}", c2s.getUid());
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
			log.error("1斯诺克-解析异常：", e);
		}
		if( balls.size()!= 1) {
			return;
		}

//		log.info("1进球：{}", balls);
		log.error("进球：{},id:{}", balls,currentPlayer.getId());
		if (balls.size() > 0) {
			Set<Integer> oldBalls = this.snookerMap.get(this.gan);
			if (oldBalls != null) {
				oldBalls.addAll(balls);
				this.snookerMap.put(this.gan, oldBalls);
			} else {
				this.snookerMap.put(this.gan, balls);
			}
		}  else {
			log.error("斯诺克进球，进球为空！！！！");
			return ;
		}


		//移除球桌和玩家身上的台球
		removeBall(ballList, balls);
		removeBall(ballListCopy,balls);
		for (GamePlayer gamePlayer : playerList) {
			removeBall(gamePlayer.getNeedBall(), balls);
		}
//		log.info("抽牌进球：{},剩余进球数量：{}",number,currentPlayer.getNeedBall().size());
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
		if(proto!=null) {
			b.setProto(proto);
		}
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
		for (GamePlayer gp :playerList) {
			b.addPlayers(gp.toProto2(true));
		}
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
		//TODO 判断是否超过最大杆。
		if( this.gan>=300 ){
			log.error("超过最大杆，游戏结束：{}",this.gan);
			return true;
		}

		Set<Integer> set = ganSnooker();
		if( set!=null && set.size()>0) {
			for(Integer i:set) {
				if( i==0) {
					illegality = 5;//进了白球，犯规。
				}
			}
		}

		List<GamePlayer> winners = new ArrayList<>();
		int foulFail = 0;
		int exit = 0;
		for(GamePlayer gp:playerList) {
			//已经犯规，不再处理
			if(gp.isFoulFail() ) {
				foulFail++;
				continue;
			}
			if( gp.isExit() ) {
				exit++;
				continue;
			}

			if(gp.getNeedBall().size()<1) {
				if (gp.getId() == currentPlayer.getId()) {
					if (illegality > 0) {
						gp.setFoulFail(true);
						foulFail++;
//						log.info("进完了自己的球，但是犯规了，停止打球，看别人打。");
					} else {
						winners.add(gp);
					}
				} else {
					winners.add(gp);
				}
			}
		}
		if( winners.size()>0) {
			if( winners.size()==1) {
				winner = winners.get(0);
			}else {
				for(GamePlayer gp:winners) {
					if( gp.getId()==currentPlayer.getId()) {
						winner = gp;
					} else {
						//下一个玩家获胜
						robinOptPlayer();
						winner = currentPlayer;
					}
					break;
				}

			}
			//重置其他玩家的一杆清台
			resetOtherClearance(winner);
			log.error("正常结束，赢家id:{}",winner.getId());
			return true;
		} else if ( (exit+foulFail) >= 2) {
			StringBuilder sb = new StringBuilder();
			for(GamePlayer gp:playerList ) {
				if (gp.isExit()) {
					sb.append("玩家id :").append(gp.getId()).append("强退。");
				}else if (gp.isFoulFail()) {
					sb.append("玩家id :").append(gp.getId()).append("犯规结束。");
				}
			}
			for(GamePlayer gp:playerList ) {
				if ( !gp.isExit() && !gp.isFoulFail() ) {
					winner = currentPlayer;
					resetOtherClearance(winner);
					log.error(sb.toString() + "赢家id：" + winner.getId());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否犯规
	 *
	 * @param hitFirstBall
	 * @return
	 */
	@Override
	public boolean judgeFoul(int hitFirstBall) {
//		if (currentPlayer.getNeedBall().size() >0 ) {
//			for(Integer card:currentPlayer.getSet()) {
//				if( card2Number(card) == hitFirstBall ) return false;
//			}
//		}
//		return true;
//		Set<Integer> list = this.snookerMap.get(this.gan);
//		if (list != null && list.size() > 0) {
//			for(Integer card:currentPlayer.getSet()) {
//				if( list.contains(card2Number(card))) return false;
////				if( card2Number(card) == hitFirstBall ) return false;
//			}
//			return true;
//		}
		return false;
	}

	//进球之后的处理
	@Override
	public void afterSync(){

	}

	private int card2Number(int card){
		int number = 0;
		if( card%13 ==0 ) {
			number = 13;
		} else {
			number = card==53?14:(card==54?15:card%13);
		}
		return number;
	}

	/**
	 * 进球后是否继续击球
	 * @return
	 */
	@Override
	public boolean canContinue() {
		/*
			规则一：只有进了自己的球，才继续打
			if (this.snookerMap.containsKey(this.gan)) {
			Set<Integer> list = this.snookerMap.get(this.gan);
			if (list != null && list.size() > 0) {
				for(Integer ball:list) {
					for(Integer card:currentPlayer.getSet()) {
						if( card2Number(card) == ball) return true;
					}
				}
			}
		}*/



		//规则二，只要进了球就继续打。
		if (this.snookerMap.containsKey(this.gan)) {
			Set<Integer> list = this.snookerMap.get(this.gan);
			if (list != null && list.size() > 0) {
				for(Integer i:list) {
					if ( i==0 ) {
						return false;
					}
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


	public void draw() {
		Random r = new Random();
		List<Integer> cardList = new ArrayList<>();
		if( mode == 1 ) {
			for (int i=1;i<=54;i++) {
				cardList.add(i);
			}
		} else {
			for (int i=1;i<=13;i++) {
				cardList.add(i);
			}
			cardList.add(53);
			cardList.add(54);
		}
		assignCard(r, cardList);
	}

	private void assignCard(Random r, List<Integer> cardList) {
		for(GamePlayer gp:playerList) {
			for (int i = 0; i < 5; i++) {
				int index = r.nextInt(cardList.size());
				int card = cardList.get(index);
				//删除索引
				cardList.remove(index);
				//汇总抽牌，去除重复
				gp.getSet().add(card);
			}
			for(Integer card:gp.getSet()) {
				for(GameBall gb:ballList) {
					int num;
					if( card%13 ==0 ) {
						num = 13;
					} else {
						num =  card==53?14:(card==54?15:(card%13));
					}
					if( gb.getNumber() == num ) {
						//避免重复
						gp.getNeedBall().removeIf(gameBall -> gameBall.getNumber()==num);
						//加上
						gp.getNeedBall().add(gb);
					}
				}
			}
		}
	}
}
