package com.wangpo.billiard.logic.match;

import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.FormulaUtil;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.base.service.BilliardPushService;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.role.RoleHandler;
import com.wangpo.billiard.logic.room.AbstractGame;
import com.wangpo.billiard.logic.room.GameMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 匹配相关处理
 */
@Component
@Slf4j
public class MatchHandler {
	@Resource
	MatchPool matchPool;

	@Resource
	PlayerMgr playerMgr;

	@Resource
	ExcelMgr excelMgr;

	@Resource
	GameMgr gameMgr;

	@Resource
	RoleHandler roleHandler;

	@DubboReference
	BilliardPushService billiardPushService;

	/**
	 * 请求匹配
	 */
	public S2C match(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());

		if( player != null  ) {
			S2C s2c = new S2C();
			s2c.setUid(c2s.getUid());
			s2c.setCid(c2s.getCid());

			if( player.getMatchStatus() >= 1 ) {
				log.error("已经在匹配池中，无法再次匹配，房间ID：{}",player.getRoomNo());
				s2c.setCode(2);
				return s2c;
			}

			if( player.getRoomNo()>0) {
				AbstractGame game = gameMgr.get(player.getRoomNo());
				if( game!=null && !game.isGameOver()) {
					log.error("已经在游戏中，无法再次匹配，房间ID：{}",player.getRoomNo());
					s2c.setCode(1);
					return s2c;
				} else if(game!=null) {

					gameMgr.dismiss(player.getId(),game);
				}
				player.setRoomNo(0);
			}

			MatchPlayer matchPlayer = new MatchPlayer();
			BilliardProto.C2S_Match proto = BilliardProto.C2S_Match.parseFrom(c2s.getBody());
			int changId = proto.getChangId();
			int gameId = proto.getGameId();
			int moneyId = proto.getMoneyId();
			int poolId = FormulaUtil.genChang(moneyId,gameId,changId);

			//判断金币或钻石是否足够
			BilliardChangConfig config = excelMgr.getChangConfigMap().get(poolId);
			if(config==null || config.getMoneyType()<1 || config.getMoneyType()>2  ) {
				log.error("匹配异常，场次配置错误:{}",poolId);
				s2c.setCode(1);
				return s2c;
			}
			if(  config.getOpenFlag()!=1 ) {
				log.error("匹配异常，场次未开启:{}",poolId);
				s2c.setCode(2);
				return s2c;
			}
			if( config.getMoneyType() ==1 ) {
				if( player.getUser().getGold() < config.getLowerLimit() ) {
					log.error("匹配错误，金币异常，玩家金币：{}，下限：{}，上限：{}",player.getUser().getGold(),
							config.getLowerLimit(),config.getUpperLimit());
					s2c.setCode(101);
					return s2c;
				}
				if( config.getUpperLimit()>0 &&  player.getUser().getGold()>config.getUpperLimit()) {
					log.error("匹配错误，金币异常，玩家金币：{}，下限：{}，上限：{}",player.getUser().getGold(),
							config.getLowerLimit(),config.getUpperLimit());
					s2c.setCode(102);
					return s2c;
				}
			} else if( config.getMoneyType() ==2 ) {
				if( player.getUser().getDiamond() < config.getLowerLimit() ) {
					log.error("匹配错误，钻石异常，玩家钻石：{}，下限：{}，上限：{}",player.getUser().getGold(),
							config.getLowerLimit(),config.getUpperLimit());
					s2c.setCode(103);
					return s2c;
				}
				if( config.getUpperLimit()> 0 &&  player.getUser().getDiamond()>config.getUpperLimit()) {
					log.error("匹配错误，钻石异常，玩家钻石：{}，下限：{}，上限：{}",player.getUser().getGold(),
							config.getLowerLimit(),config.getUpperLimit());
					s2c.setCode(104);
					return s2c;
				}
			}

			matchPlayer.fromPlayer(player,poolId);
			matchPlayer.setRoleId(player.useRoleId());
			matchPlayer.setExp(roleHandler.useRole(player)==null?0:roleHandler.useRole(player).getExp());
			matchPool.newMatcher(poolId,matchPlayer);
			player.setMatchPoolId(poolId);
			player.setMatchStatus(1);
			s2c.setCode(0);
			s2c.setBody(c2s.getBody());
			return s2c;
		}
		return null;

	}


	public S2C cancelMatch(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null  ) {
			S2C s2c = new S2C();
			s2c.setUid(c2s.getUid());
			s2c.setCid(c2s.getCid());

			if( player.getMatchStatus() == 2 ) {
				log.error("已经申请取消匹配，无法再次取消" );
				s2c.setCode(2);
				return s2c;
			}

			if( player.getRoomNo()>0) {
				AbstractGame game = gameMgr.get(player.getRoomNo());
				if( game!=null ) {
					log.error("已经在游戏中，无法取消匹配，房间ID：{}",player.getRoomNo());
					s2c.setCode(1);
					return s2c;
				}
			}
			matchPool.cancelPlayer(player.getMatchPoolId(),player.getId());
			player.setMatchStatus(2);
		}
		return null;
	}

	//匹配失败
	public void matchTimeOut(int uid) {
		S2C s2c = new S2C();
		s2c.setUid(uid);
		s2c.setCid(Cmd.MATCH_TIME_OUT);
		billiardPushService.push(s2c);
		//匹配失败修改匹配状态
		Player player = playerMgr.getPlayerByID(uid);
		if( player != null ) {
			player.setMatchPoolId(0);
			player.setMatchStatus(0);
		}
	}

	public void matchOK(BilliardProto.S2C_MatchOK.Builder builder,int id) {
		S2C s2c = new S2C();
		s2c.setUid(id);
		s2c.setCid(Cmd.MATCH_OK);
		s2c.setBody(builder.build().toByteArray());
		billiardPushService.push(s2c);
	}

	public void pushCancel(Integer id) {
		S2C s2c = new S2C();
		s2c.setUid(id);
		s2c.setCid(Cmd.CANCEL_MATCH);
		billiardPushService.push(s2c);

		Player player = playerMgr.getPlayerByID(id);
		if( player != null  ) {
			player.setMatchStatus(0);
			player.setMatchPoolId(0);
		}
	}
	
	/**
	 * 新手引导请求匹配
	 * @param c2s
	 * @return
	 */
	public S2C nociveGuideMatch(C2S c2s) throws Exception{

		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player == null  ) {
			return null;
		}
		//默认场次
		int chang = 1011;
		int t = 0;
		if( player.getChang().containsKey(String.valueOf(chang)) ) {
			JSONObject gameData = (JSONObject) player.getChang().get(String.valueOf(chang));
			t = gameData.getInteger("game_times");
		}
		if ( t != 0) {
			log.error("玩家不是第一次游戏：{}",chang,t);
			return null;
		}
		S2C s2c = new S2C();
		s2c.setUid(c2s.getUid());
		s2c.setCid(c2s.getCid());

		log.info("玩家匹配，匹配状态：{}",player.getMatchStatus());
		if( player.getMatchStatus() == 1 ) {
			log.error("已经在匹配池中，无法再次匹配，房间ID：{}",player.getRoomNo());
			s2c.setCode(2);
			return s2c;
		}

		if( player.getRoomNo()>0) {
			AbstractGame game = gameMgr.get(player.getRoomNo());
			if( game!=null && !game.isGameOver()) {
				log.error("已经在游戏中，无法再次匹配，房间ID：{}",player.getRoomNo());
				s2c.setCode(1);
				return s2c;
			} else if(game!=null) {

				gameMgr.dismiss(player.getId(),game);
			}
			player.setRoomNo(0);
		}

		MatchPlayer matchPlayer = new MatchPlayer();
//		BilliardProto.C2S_NoviceGuideMatch proto = BilliardProto.C2S_NoviceGuideMatch.parseFrom(c2s.getBody());
//		int changId = proto.getChangId();
//		int gameId = proto.getGameId();
//		int moneyId = proto.getMoneyId();
//		int poolId = FormulaUtil.genChang(moneyId,gameId,changId);

		matchPlayer.fromPlayer(player,chang);
		matchPlayer.setRoleId(player.useRoleId());
		matchPlayer.setExp(roleHandler.useRole(player)==null?0:roleHandler.useRole(player).getExp());
		matchPool.noviceGuideMatchTask(matchPlayer, 1011);
		player.setMatchStatus(1);
		s2c.setCode(0);
		s2c.setBody(c2s.getBody());
		return s2c;
	}
}

