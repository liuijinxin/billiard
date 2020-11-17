package com.wangpo.billiard.logic.player;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.GlobalEnum;
import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.base.excel.GlobalConfig;
import com.wangpo.billiard.bean.LuckyCue;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.bean.Role;
import com.wangpo.billiard.consts.InitValue;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.base.excel.BilliardCueConfig;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.cue.CueMgr;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.bean.*;
import com.wangpo.billiard.bean.PlayerCue;
import com.wangpo.billiard.logic.item.ItemMgr;
import com.wangpo.billiard.logic.lucky.LuckyCueHandler;
import com.wangpo.billiard.logic.match.ChangKit;
import com.wangpo.billiard.logic.room.AbstractGame;
import com.wangpo.billiard.logic.room.GameHandler;
import com.wangpo.billiard.logic.room.GameMgr;
import com.wangpo.billiard.logic.room.GamePlayer;
import com.wangpo.billiard.logic.util.FightUtil;
import com.wangpo.billiard.service.BilliardCueService;
import com.wangpo.billiard.service.LuckyCueService;
import com.wangpo.billiard.service.PlayerService;
import com.wangpo.billiard.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 玩家相关处理器
 */
@Component
@Slf4j
public class PlayerHandler {
	@DubboReference
	private PlatformService platformService;
	@Resource
	PlayerMgr playerMgr;
	@Resource
	PlayerService playerService;
	@Resource
	CueMgr cueMgr;
	@DubboReference
	BilliardPushService billiardPushService;
	@Resource
	GameHandler gameHandler;
	@Resource
	GameMgr roomMgr;
	@Resource
	ExcelMgr excelMgr;
	@Resource
	BilliardCueService cueService;
	@Resource
	ItemMgr itemMgr;
	@Resource
	RoleService roleService;
	@Resource
	LuckyCueService luckyCueService;

	@Resource
	LuckyCueHandler luckyCueHandler;

	public S2C
	login(C2S c2s) {
		log.error("玩家登录：{}",c2s.getUid());
		//平台获取用户信息
		CommonUser user = platformService.queryUserByID(c2s.getUid());
		//加入游戏缓存
		Player player = playerMgr.getPlayerByID(user.getId());
		if( player == null ) {
			//缓存没有，先从数据库查询
			player = playerService.selectPlayerByID(user.getId());
			if( player == null ) {
				player = new Player();
				player.setId(user.getId());
				player.setNick(user.getNick());
				player.setSex(user.getSex());
				player.setHead(user.getHead());
				player.setGold(user.getGold());
				player.setDiamond(user.getDiamond());
				//初始化战力
				for(int i=1;i<=4;i++){
					player.getFight().put(String.valueOf(i), FightUtil.initFightData());
				}
				//初始化场次游戏次数，抽奖模块用
				//金币，九球，初级场
				for(int chang: ChangKit.CHANG) {
					FightUtil.initGameData(player,String.valueOf(chang),0);
				}
				/*for(int i=1;i<=2;i++) { //1-金币场，2-钻石场
					for (int j = 1; j <= 4; j++) {//1-九球玩法，2-红球玩法，3-15张牌抽牌玩法，4-54张牌抽牌玩法。
						if(i==1 && j>2) continue;
						for (int k = 1; k <= 3; k++) {//1-低级场，2-中级场，3-高级场。
							int chang = FormulaUtil.genChang(i,j,k);
							player.getChang().put(String.valueOf(chang), 0);
						}
					}
				}*/

				//默认分配一个球杆给玩家
				GlobalConfig globalConfig = excelMgr.getGlobal(GlobalEnum.CUE_ID.code);
				if (globalConfig != null) {
					int cueId = globalConfig.intValue();
					BilliardCueConfig cueConfig = excelMgr.getPlayerCue(cueId);
					if (cueConfig != null) {
						PlayerCue playerCue = new PlayerCue();
						playerCue.setCueID(cueConfig.getId());
						playerCue.setPlayerID(player.getId());
						playerCue.setStar(cueConfig.getStar());
						playerCue.setDefendTimes(InitValue.CUE_DEFEND_TIMES);
						playerCue.setIsUse(1);
						cueService.addCue(playerCue);
					}
				}
				playerService.insertPlayer(player);
			} else {
				if( player.getFight() == null) {
					for(int i=1;i<=3;i++){
						player.getFight().put(String.valueOf(i), FightUtil.initFightData());
					}
				} else if( !player.getFight().containsKey("1") ) {
					//清除之前的旧数据
					player.getFight().clear();
					for(int i=1;i<=3;i++){
						player.getFight().put(String.valueOf(i), FightUtil.initFightData());
					}
				}
			}
			//球杆
			List<PlayerCue> cueList = cueMgr.getCueList(player.getId());
			player.setCueList(cueList);
			//角色
			List<Role> roleList = roleService.selectRoleById(player.getId());
			player.setRoleList(roleList);

		}
		player.setUser(user);
		player.setId(user.getId());
		//更新登录时间
		player.setLoginTime(new Date());
		playerService.updatePlayer(player);

		playerMgr.addPlayerByID(player);
		player.setOnline(true);
		player.setOnlineTime(System.currentTimeMillis());

		//改成客户端自己取，判断玩家是否已经在游戏
		/*if( player.getRoomNo()>0) {
			log.error("断线重连进入房间，玩家id：{}",player.getId());
			AbstractGame gameRoom = roomMgr.get(player.getRoomNo());
			if( gameRoom != null && !gameRoom.isGameOver()) {
				gameHandler.pushGameInit(player.getId(), gameRoom);
			} else {
				log.error("断线重连，游戏已经结束。");
			}
		}*/

		//推送游戏次数
		pushGameTimes(player);
		//幸运一杆
		//幸运一杆数据
		LuckyCue luckyCue = player.getLuckyCue();
		if( luckyCue == null ) {
			luckyCue = luckyCueService.selectLuckyCueByID(player.getId());
			if(luckyCue == null) {
				//插入数据库
				luckyCue = new LuckyCue();
				luckyCue.setPlayerId(player.getId());
				luckyCue.setLevel(1);
				luckyCue.setVipTimes(InitValue.LUCKY_CUE_VIP_TIMES);
				luckyCue.setFreeTimes(InitValue.LUCKY_CUE_FREE_TIMES);//默认给2次免费
				luckyCueService.insertLuckyCue(luckyCue);
			}
		}
		player.setLuckyCue(luckyCue);
		luckyCueHandler.pushLuckyCue(player);

		log.error("玩家{}登录成功，总在线人数：{},总缓存人数：{}",c2s.getUid(),playerMgr.online(),playerMgr.allPlayer().size());
		//应答
//		S2C s2c = new S2C();
//		s2c.setCid(Cmd.LOGIN);
//		s2c.setUid(user.getId());
//		s2c.setBody(player.toProto().build().toByteArray());
		return null;
	}

	/**
	 * 推送游戏次数
	 * @param player 玩家
	 */
	public void pushGameTimes(Player player) {
		BilliardProto.S2C_GameTimes.Builder b = BilliardProto.S2C_GameTimes.newBuilder();
		Iterator<String> it = player.getChang().keySet().iterator();
		while(it.hasNext()) {
			String key= it.next();
			if( key.startsWith("L_")) {
				it.remove();
				continue;
			}
			if( !(player.getChang().get(key) instanceof JSONObject)) {
				FightUtil.initGameData(player,key, 0);
			}
			JSONObject gameData = (JSONObject) player.getChang().get(key);
			if( gameData != null ) {
				int chang = Integer.parseInt(key);
				int times = gameData.getInteger("game_times")-gameData.getInteger("lottery_times");
				b.addGameTimes(BilliardProto.GameTime.newBuilder().setChang(chang).setTimes(times));
			}
//			if(key!=null && ChangKit.isLotteryChang(key)) continue;
//			if(key==null)continue;

		}
//		log.error("推送抽奖：{}",b);
		S2C s2c = new S2C();
		s2c.setCid(Cmd.GAME_TIMES);
		s2c.setBody(b.build().toByteArray());
		gameHandler.push(player.getId(),s2c);
	}

	/**
	 * 获取台球信息
	 */
	public S2C billiardInfo(C2S c2s) {
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.BILLIARD_INFO);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		if (player == null) {
			log.error("找不到玩家,{}",uid);
			s2c.setCode(1);
			return s2c;
		}
		BilliardProto.C2S_BilliardInfo.Builder builder = BilliardProto.C2S_BilliardInfo.newBuilder();
		//发送四种玩法的数据，具体说明见FightUtil
		for(int i=1;i<=4;i++){
			JSONObject gameJson = player.getFight().getJSONObject(String.valueOf(i));
			if (gameJson==null) {
				gameJson = FightUtil.initFightData();
				player.getFight().put(String.valueOf(i), gameJson);
			}
			int streak = gameJson.getInteger("streak");
			int win = gameJson.getInteger("win");
			int total = gameJson.getInteger("total");
			BilliardProto.BilliardInfo.Builder billiardInfo = BilliardProto.BilliardInfo.newBuilder();
			billiardInfo.setChang(i);
			billiardInfo.setStreak(streak);
			billiardInfo.setWin(win);
			billiardInfo.setTotal(total);
			builder.addBilliardInfos(billiardInfo.build());
		}
		s2c.setBody(builder.build().toByteArray());
		return s2c;
	}

	public S2C getAllItem(C2S c2s) {
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.ALL_ITEM);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		if (player == null) {
			log.error("找不到玩家,{}",uid);
			s2c.setCode(1);
			return s2c;
		}
		S2C s2C = itemMgr.getAllItem(player, s2c);
		return s2C;
	}

	/**
	 * 我的球杆
	 */
	public S2C myCue(C2S c2s) throws Exception {
		BilliardProto.C2S_MyCue proto = BilliardProto.C2S_MyCue.parseFrom(c2s.getBody());
		int playerID = proto.getPlayerID();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.MY_CUE);
		s2c.setUid(playerID);
		Player player = playerMgr.getPlayerByID(playerID);
		if (player == null) {
			log.error("找不到玩家,{}",playerID);
			s2c.setCode(1);
			return s2c;
		}
		s2c = cueMgr.myCue(player,s2c);
		return s2c;
	}

	/**
	 * 购买球杆
	 */
	public S2C buyCue(C2S c2s) throws Exception {
		BilliardProto.C2S_BuyCue proto = BilliardProto.C2S_BuyCue.parseFrom(c2s.getBody());
		int playerID = proto.getPlayerID();
		int cueID = proto.getCueID();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.BUY_CUE);
		s2c.setUid(playerID);
		Player player = playerMgr.getPlayerByID(playerID);
		if (player == null) {
			log.error("找不到玩家,{}",playerID);
			s2c.setCode(1);
			return s2c;
		}
		s2c = cueMgr.buyCue(player, cueID, s2c);
		return s2c;
	}

	/**
	 * 出售球杆
	 */
	public S2C sellCue(C2S c2s) throws Exception {
		BilliardProto.C2S_SellCue proto = BilliardProto.C2S_SellCue.parseFrom(c2s.getBody());
		int playerID = proto.getPlayerID();
		int cueID = proto.getId();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.SELL_CUE);
		s2c.setUid(playerID);
		Player player = playerMgr.getPlayerByID(playerID);
		if (player == null) {
			log.error("找不到玩家,{}",playerID);
			s2c.setCode(1);
			return s2c;
		}
		s2c = cueMgr.sellCue(player, cueID, s2c);
		return s2c;
	}

	/**
	 * 升级球杆
	 */
	public S2C upgradeCue(C2S c2s) throws Exception {
		BilliardProto.C2S_UpgradeCue proto = BilliardProto.C2S_UpgradeCue.parseFrom(c2s.getBody());
		int playerID = proto.getPlayerID();
		int cueID = proto.getId();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.UPGRADE_CUE);
		s2c.setUid(playerID);
		Player player = playerMgr.getPlayerByID(playerID);
		if (player == null) {
			log.error("找不到玩家,{}",playerID);
			s2c.setCode(1);
			return s2c;
		}
		s2c = cueMgr.upgradeCue(player,cueID,s2c);
		return s2c;
	}

	/**
	 * 使用球杆
	 */
	public S2C useCue(C2S c2s) throws Exception {
		BilliardProto.C2S_UseCue proto = BilliardProto.C2S_UseCue.parseFrom(c2s.getBody());
		int playerID = proto.getPlayerID();
		int cueID = proto.getId();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.USE_CUE);
		s2c.setUid(playerID);
		Player player = playerMgr.getPlayerByID(playerID);
		if (player == null) {
			log.error("找不到玩家,{}",playerID);
			s2c.setCode(1);
			return s2c;
		}
		s2c = cueMgr.useCue(player,cueID,s2c);
		return s2c;
	}

//	/**
//	 * 查看所有球杆
//	 */
//	public S2C seeAllCue(C2S c2s) throws Exception {
//		BilliardProto.C2S_AllCue proto = BilliardProto.C2S_AllCue.parseFrom(c2s.getBody());
//		int playerID = proto.getPlayerID();
//		S2C s2c = new S2C();
//		s2c.setCid(Cmd.ALL_CUE);
//		s2c.setUid(playerID);
//		Player player = playerMgr.getPlayerByID(playerID);
//		if (player == null) {
//			log.error("找不到玩家,{}",playerID);
//			s2c.setCode(1);
//			return s2c;
//		}
//		s2c = cueMgr.seeAllCue(player,s2c);
//		return s2c;
//	}

	/**
	 * 维护球杆
	 */
	public S2C defendCue(C2S c2s) throws Exception {
		BilliardProto.C2S_DefendCue proto = BilliardProto.C2S_DefendCue.parseFrom(c2s.getBody());
		int playerId = proto.getPlayerId();
		int cueId = proto.getId();
		int defendType = proto.getDefendType();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.DEFEND_CUE);
		s2c.setUid(playerId);
		Player player = playerMgr.getPlayerByID(playerId);
		if (player == null) {
			log.error("找不到玩家,{}",playerId);
			s2c.setCode(1);
			return s2c;
		}
		s2c = cueMgr.defendCue(player,cueId,defendType,s2c);
//		log.info("球杆维护s2c:{}",s2c);
		return s2c;
	}

	/**
	 * 修改玩家道具
	 * @param player 玩家
	 * @param s2c	s2c
	 * @param payType	支付类型
	 * @param payPrice	支付金额
	 * @param flag 不需要判断传false，需要判断传true
	 * @param gameEventEnum	事件
	 * @return 道具是否足够
	 */
	public boolean usePlayerItem(Player player, S2C s2c, int payType, int payPrice, boolean flag, GameEventEnum gameEventEnum) {
		if (payType > 0) {
			if (payType == 1) {
				boolean flag1 = false;
				if (player.getUser().getGold() < Math.abs(payPrice)) {
					flag1 = true;
				}
				if (flag && flag1) {
//					log.info("玩家金币不足");
					s2c.setCode(3);
					return true;
				} else {
					//如果游戏中，需要判断扣除最大输赢后是否还能购买。
					int maxLose = maxGameLose(player,1);
					if( flag && player.getUser().getGold()< (maxLose + Math.abs(payPrice))) {
//						log.info("玩家金币不足");
						s2c.setCode(101);
						return true;
					}

					CommonUser cu = platformService.modifyGold(player.getId(),payPrice,gameEventEnum.reason);
					if( cu != null) {
						player.setUser(cu);
						return false;
					} else {
						log.error("平台服修改金币失败，金币不足，uid:{}",player.getId());
						return true;
					}
//					platformService.addItem(player.getUser().getId(),payType,payPrice,gameEventEnum);
				}
			} else if (payType == 2){
				boolean flag1 = false;
				if (player.getUser().getDiamond() < Math.abs(payPrice)) {
					flag1 = true;
				}
				if (flag && flag1) {
//					log.info("玩家钻石不足");
					s2c.setCode(4);
					return true;
				} else {
					int maxLose = maxGameLose(player,2);
					if( flag && player.getUser().getDiamond()< (maxLose + Math.abs(payPrice))) {
//						log.info("玩家钻石不足");
						s2c.setCode(101);
						return true;
					}
//					platformService.addItem(player.getUser().getId(),payType,payPrice,gameEventEnum);

					CommonUser cu = platformService.modifyDiamond(player.getId(),payPrice,gameEventEnum.reason);
					if( cu != null) {
						player.setUser(cu);
						return false;
					} else {
						log.error("平台服修改钻石失败，金币不足，uid:{}",player.getId());
						return true;
					}
				}
			} else {
				boolean use = itemMgr.use(player, payType, -payPrice);
				if (!use) {
//					log.info("道具不足");
					s2c.setCode(6);
					return true;
				}
			}
		}
		return false;
	}

	private int maxGameLose(Player player,int moneyType) {
		int maxLose = 0;
		if(player.getRoomNo()>0) {
			AbstractGame game = roomMgr.get(player.getRoomNo());
			if( game != null && !game.isGameOver() ) {
				BilliardChangConfig config = excelMgr.getChangConfigMap().get(game.getChang());
				if( config != null && config.getMoneyType() == moneyType) {
					int base = config.getBet();
					maxLose = base*game.getDoubleNum() ;
					int gameType = FightUtil.chang2game(game.getChang());
					if(gameType==3 || gameType == 4 ) {
						maxLose = maxLose*5;//抽牌玩法最大输5张
					}
				}
			}
		}
		return maxLose;
	}

	/**
	 * 游戏公告
	 * @param msg 公告信息
	 * @param PlayerId	玩家id
	 */
	public void notice(String msg, Integer PlayerId){
		BilliardProto.S2C_Notice.Builder builder = BilliardProto.S2C_Notice.newBuilder();
		builder.setBody(msg);

		S2C s2c = new S2C();
		s2c.setCid(Cmd.Notice);
		s2c.setUid(PlayerId);
		s2c.setBody(builder.build().toByteArray());
		billiardPushService.push(s2c);
	}

}
