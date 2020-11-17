package com.wangpo.billiard.logic.room;

import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.base.pool.MyThreadPool;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GameHandler {
	@Resource
	GameMgr roomMgr;
	@Resource
	PlayerMgr playerMgr;
	@DubboReference
	BilliardPushService billiardPushService;

	@Resource
	ExcelMgr excelMgr;

	/**
	 * 获取房间信息
	 * @param c2s  请求
	 * @return
	 */
	public S2C getRoomInfo(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if( gameRoom != null ) {
				if( gameRoom.isGameOver) {
					S2C s2c = new S2C();
					s2c.setUid(c2s.getUid());
					s2c.setSid(c2s.getSid());
					s2c.setCid(c2s.getCid());
					s2c.setCode(1);
					return s2c;
				} else {
					pushGameInit(c2s.getUid(),gameRoom);
					return null;
				}
				/*boolean allPrepare = true;
				for(GamePlayer gp:gameRoom.getPlayerList()) {
					if( gp.getId() == c2s.getUid() ) {
						gp.setPrepare(true);
					} else if( !gp.isPrepare() && gp.getId()>0){
						//机器人不需要
						allPrepare = false;
					}
				}*/
				/*if( allPrepare) {
					log.info("都准备好了，开始帧同步.....");
					roomMgr.startFrame(gameRoom);
				}*/
			}
		}

		S2C s2c = new S2C();
		s2c.setUid(c2s.getUid());
		s2c.setSid(c2s.getSid());
		s2c.setCid(c2s.getCid());
		s2c.setCode(2);
		return s2c;
	}

	/**
	 * 推送游戏开始
	 * @param uid      玩家ID
	 * @param game  房间
	 */
	public void pushGameInit(int uid, AbstractGame game) {
		S2C s2c = game.buildGameInit();
		s2c.setUid(uid);
		billiardPushService.push(s2c);
		//如果是第一杆不发
		if( game.getGan() > 0) {
			int foul = 0;
			if( game.foulFlag.containsKey(game.gan) ) {
				foul = game.foulFlag.get(game.gan);
			}
			s2c =( game).buildOptPlayer( foul );
			s2c.setUid(uid);
			billiardPushService.push(s2c);
		}
		//断线重连回来后，需要置玩家的同步为true
		for (GamePlayer gamePlayer : game.getPlayerList()) {
			if (gamePlayer.getId() == uid && game.currentPlayer.getId() == uid) {
				gamePlayer.setNeedSyncGan(true);
				break;
			}
		}
	}

	public S2C layBall(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				gameRoom.layBall(c2s);
				transfer(c2s,gameRoom);
			}
		}
		return null;
	}

	public S2C sleepLayBall(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				gameRoom.sleepLayBall(c2s);
//				transfer(c2s,gameRoom);
			}
		}
		return null;
	}

	/**
	 * 转发客户端消息
	 * @param c2s   消息包
	 * @param gameRoom  游戏房间
	 */
	public void transfer(C2S c2s, AbstractGame gameRoom) {
//		log.info("转发消息：{}",c2s.getCid());
		for (GamePlayer gp : gameRoom.getPlayerList()) {
			if( !gp.isExit() ) {
				push(gp.getId(), c2s);
			}
		}
	}

	public void transfer(C2S c2s,int code, AbstractGame gameRoom) {
		for (GamePlayer gp : gameRoom.getPlayerList()) {
			if( !gp.isExit() ) {
				push(gp.getId(), code,c2s);
			}
		}
	}

	public void dispatch(S2C s2c, AbstractGame gameRoom) {
		for (GamePlayer gp : gameRoom.getPlayerList()) {
			if( !gp.isExit() ) {
				push(gp.getId(), s2c);
			}
		}
	}

	public void dispatch(S2C s2c, AbstractGame gameRoom, int uid) {
		for (GamePlayer gp : gameRoom.getPlayerList()) {
			if(gp.isExit() || gp.getId()==uid) {
				continue;
			}
			push(gp.getId(), s2c);
		}
	}

	public S2C playerOpt(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				boolean ok = roomMgr.playerOpt(c2s ,gameRoom);
				BilliardProto.C2S_Batting proto = BilliardProto.C2S_Batting.parseFrom(c2s.getBody());
//				log.info("击球：{},{}",proto.getAngle(),proto.getVelocity());
//				gameRoom.opt();
				if( ok ) {
					transfer(c2s, gameRoom);
				}
			}
		}
		return null;
	}

	public S2C transfer(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				transfer(c2s, gameRoom);
			}
		}
		return null;
	}

	//推送c2s
	public void push(int uid,C2S c2s) {
		S2C s2c = new S2C();
		s2c.setCid(c2s.getCid());
		s2c.setUid(uid);
		s2c.setBody(c2s.getBody());
		billiardPushService.push(s2c);
	}

	//推送c2s
	public void push(int uid,int code,C2S c2s) {
		S2C s2c = new S2C();
		s2c.setCid(c2s.getCid());
		s2c.setUid(uid);
		s2c.setCode(code);
		s2c.setBody(c2s.getBody());
		billiardPushService.push(s2c);
	}

	public void push(int id,S2C s2c) {
		s2c.setUid(id);
		billiardPushService.push(s2c);
	}

	public S2C syncPos(C2S c2s) throws Exception {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				roomMgr.syncPos(c2s.getUid(),c2s,gameRoom);
				transfer(c2s, gameRoom);
			}
		}
		return null;
	}

	public S2C snooker(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				gameRoom.snooker( c2s);
				transfer(c2s, gameRoom);
			}
		}
		return null;
	}

	ScheduledExecutorService executor = MyThreadPool.createScheduled("GameHandler",1);

	public S2C reqDouble(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame game = roomMgr.get(player.getRoomNo());
			if (game != null) {
				for(GamePlayer gp:game.getPlayerList()) {
					if( gp.getId()<0) {
						//机器人直接拒绝。
						executor.schedule(()->{
							push(c2s.getUid(),2,c2s);
						},2, TimeUnit.SECONDS);
						return null;
					}
				}
				int maxDouble = 1;

				BilliardChangConfig config = excelMgr.getChangConfigMap().get(game.getChang());
				if( config != null ) {
					maxDouble = config.getDoubling();
				}else {
					log.error("请求翻倍场次配置不存在，场次：{}",game.getChang());
				}
				//判断金币或钻石是否足够
				if( config.getMoneyType()==1) {
					int lose = (config.getBet()*game.getDoubleNum()*2 );
					for(GamePlayer gp:game.getPlayerList()) {
						Player p = playerMgr.getPlayerByID(gp.getId());
						if( p.getUser().getGold() < lose ) {
							//判断是否为自己金币不足，自己金币不足，返回金币不足，对方金币不足，返回对方拒绝加倍
							if (p.getId() == c2s.getUid()) {
								push(c2s.getUid(),3,c2s);
							} else {
								push(c2s.getUid(),2,c2s);
							}
							return null;
						}
					}
				} else if( config.getMoneyType()==2){
					int lose = (config.getBet()*game.getDoubleNum()*2 );
					for(GamePlayer gp:game.getPlayerList()) {
						Player p = playerMgr.getPlayerByID(gp.getId());
						if( p.getUser().getDiamond() < lose ) {
							if (p.getId() == c2s.getUid()) {
								push(c2s.getUid(),4,c2s);
							} else {
								push(c2s.getUid(),2,c2s);
							}
							return null;
						}
					}
				}

				if( game.isRefuseDouble() ) {
					push(c2s.getUid(),2,c2s);
				} else if (game.getDoubleNum()>=maxDouble){
					push(c2s.getUid(),1,c2s);
				}else {
					game.setReqDouble(true);
					transfer(c2s, game);
				}
			}
		}
		return null;
	}


	public S2C respDouble(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame game = roomMgr.get(player.getRoomNo());
			if (game != null) {
				BilliardProto.C2S_RespDouble proto = BilliardProto.C2S_RespDouble.parseFrom(c2s.getBody());
				if (!game.isReqDouble()){
					//没有人提出翻倍
					push(c2s.getUid(),0,c2s);
				} else if( proto.getFlag() == 1 ) {
					game.setAgreeDouble(game.getAgreeDouble()+1);
					if( game.getAgreeDouble()>= (game.getPlayerList().size()-1)) {
						//同意翻倍

						BilliardChangConfig config = excelMgr.getChangConfigMap().get(game.getChang());
						game.redouble(config.getDoubling());
						log.error("翻倍成功，当前倍数：{}",game.doubleNum);
					}
					transfer(c2s, game);
				} else {
					//拒绝翻倍
					game.setRefuseDouble(true);
					//通知对方
					for(GamePlayer gp:game.getPlayerList()) {
						if( gp.getId() != c2s.getUid()) {
							//对方拒绝翻倍
							push(gp.getId(),1,c2s);
						}
					}
				}
			}
		}
		return null;
	}

	public S2C syncDeskInfo(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				gameRoom.syncDeskInfo(c2s);
			}
		}
		return null;
	}

	public S2C exitRoom(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if(player==null) {
			log.error("游戏结束离开房间异常，玩家为空：{}",c2s.getUid());
			return null;
		}
//		log.info("离开房间，玩家ID：{}，房间好：{}",player.getId(),player.getRoomNo());
		if( player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				roomMgr.dismiss(player.getId(),gameRoom);
			}
			player.setRoomNo(0);
		}
		return null;
	}

	public S2C newRound(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if (gameRoom != null) {
				roomMgr.newRound(player.getId(),gameRoom);
				for(GamePlayer gp:gameRoom.playerList) {
					c2s.setBody(BilliardProto.C2S_NewRound.newBuilder().setId(player.getId()).build().toByteArray());
//					log.info("再来一局：{}",gp.getId());
					push(gp.getId(),c2s);
				}
			}
		}
		return null;
	}


	/**
	 * 玩家逃跑
	 * @param c2s
	 * @return
	 */
	public S2C exitGame(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame game = roomMgr.get(player.getRoomNo());
			if (game != null) {
				player.setRoomNo(0);
				roomMgr.exitGame(game,c2s.getUid());
			}
		}
		return null;
	}
	
	
	public S2C noviceExitGame(int id) {
		Player player = playerMgr.getPlayerByID(id);
		if( player != null && player.getRoomNo()>0) {
			AbstractGame game = roomMgr.get(player.getRoomNo());
			if (game != null) {
				player.setRoomNo(0);
				roomMgr.exitGame(game,id);
			}
		}
		return null;
	}

	public S2C emoji(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null && player.getRoomNo()>0) {
			AbstractGame game = roomMgr.get(player.getRoomNo());
			if (game != null) {
				BilliardProto.C2S_Chat b= BilliardProto.C2S_Chat.parseFrom(c2s.getBody());
				S2C s2c = new S2C();
				s2c.setCid(Cmd.S2C_EMOJI);
				s2c.setBody(BilliardProto.S2C_Chat.newBuilder().setEmoji(b.getEmoji()).setId(c2s.getUid()).build().toByteArray());
				for(GamePlayer gp:game.playerList) {
					push(gp.getId(),s2c);
				}
			}
		}
		return null;
	}
}
