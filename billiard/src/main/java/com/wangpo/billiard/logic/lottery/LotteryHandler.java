package com.wangpo.billiard.logic.lottery;

import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.base.excel.BilliardFileCodeConfig;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.PlatformService;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.billiard.bean.LotteryResult;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.enums.PropsEnum;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.item.ItemMgr;
import com.wangpo.billiard.logic.match.ChangKit;
import com.wangpo.billiard.logic.player.PlayerHandler;
import com.wangpo.billiard.logic.util.FightUtil;
import com.wangpo.billiard.service.LotteryResultService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 抽奖模块
 */

@Component
@Slf4j
public class LotteryHandler {
	@Resource
	PlayerMgr playerMgr;
	@Resource
	LotteryMgr lotteryMgr;
	@DubboReference
	BilliardPushService pushService;
	@Resource
	ItemMgr itemMgr;
	@DubboReference
	PlatformService platformService;
	@Resource
	PlayerHandler playerHandler;
	@Resource
	ExcelMgr excelMgr;
	@Resource
	LotteryResultService lotteryResultService;

	/**
	 * 抽奖
	 */
	public S2C lottery(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player!=null ) {
			int chang = BilliardProto.C2S_Lottery.parseFrom(c2s.getBody()).getChang();
			BilliardChangConfig billiardChangConfig = excelMgr.getChangConfigMap().get(chang);
			if(billiardChangConfig.getLottery() == 0) {
				log.error("场次{} 抽奖开关未开启,指令异常",chang);
				return null;
			}

			//判断次数是否达到要求
			String key = ChangKit.toChang(chang);
			String lkey = ChangKit.toLotteryChang(chang);
			if( player.getChang().containsKey(key) ) {
				int t = FightUtil.getLotteryTimes(player,chang);
				if ( t < 3) {
					log.error("未达到抽奖次数，当前{}场次总游戏数：{}",chang,t);
					return null;
				}
//				FightUtil.modifyGameTimes(player,chang,-3);
				FightUtil.modifyLotteryTimes(player,chang);
			} else {
				log.error("找不到当前场次的游戏数：{}",chang);
				return null;
			}
			//存放抽奖结果
			LotteryResult lotteryResult = new LotteryResult();
			lotteryResult.setPlayerId(player.getId());
			lotteryResult.setNick(player.getNick());
			//抽奖
			LotteryItem item = lotteryMgr.lottery(chang,lotteryResult);
			if( item == null ) {
				log.error("抽奖失败，奖励为空，场次：{}",chang);
				return null;
			}
			platformService.addItem(player.getId(),item.getId(),item.getNum(),GameEventEnum.LOTTERY);
			lotteryResult.setAwardType(item.getId());
			lotteryResult.setAwardNum(item.getNum());
			lotteryResult.setBase(item.getBase());
			//获取场次
			BilliardFileCodeConfig fileCode = excelMgr.getFileCodeById(chang);
			if (fileCode != null) {
				lotteryResult.setChang(fileCode.getPlayType() + fileCode.getMoneyType() + fileCode.getGrade());
			}
			//抽奖结果插入数据库
			lotteryResultService.insertLotteryResult(lotteryResult);
			//其他显示的奖励
			List<LotteryItem> lotteryItems = lotteryMgr.otherLottery(chang);
			BilliardProto.S2C_LotteryAward.Builder builder = BilliardProto.S2C_LotteryAward.newBuilder();
			builder.setLotteryItem(item.toProto().build());
			lotteryItems.forEach(lotteryItem -> {
				builder.addLotteryItems(lotteryItem.toProto().build());
			});
			S2C s2c = new S2C();
			s2c.setCid(c2s.getCid());
			s2c.setUid(c2s.getUid());
			s2c.setBody(builder.build().toByteArray());
			pushService.push(s2c);

			//推送游戏次数
			playerHandler.pushGameTimes(player);
			//如果抽中红包券，新增到红包墙中
			if (item.getId() == PropsEnum.RED_PACKET.getCode()) {
				platformService.addRedPacket(player.getId(),chang,item.getNum());
			}
		}
		return null;
	}

	public S2C nociveGuideLottery(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player!=null ) {
			int chang = BilliardProto.C2S_NoviceGuideLottery.parseFrom(c2s.getBody()).getChang();
			if(chang != 1011) {
				log.info("新手领奖只能领取1011场次：{}",chang);
				return null;
			}
			BilliardChangConfig billiardChangConfig = excelMgr.getChangConfigMap().get(chang);
			if(billiardChangConfig.getLottery() == 0) {
				log.error("场次{} 抽奖开关未开启,指令异常",chang);
				return null;
			}

			//判断次数是否达到要求
			String key = ChangKit.toChang(chang);
			String lkey = ChangKit.toLotteryChang(chang);
			if( player.getChang().containsKey(key) ) {
				int t = 0;
				JSONObject gameData;
				if( player.getChang().containsKey(String.valueOf(chang)) ) {
					gameData = (JSONObject) player.getChang().get(String.valueOf(chang));
					t = gameData.getInteger("game_times") - gameData.getInteger("lottery_times");
				}
				if ( t != 1) {
					log.error("新手引导未达到指定抽奖次数，当前{}场次总游戏数：{}",chang,t);
					return null;
				}
				if( player.getChang().containsKey(String.valueOf(chang)) ) {
					gameData = (JSONObject) player.getChang().get(String.valueOf(chang));
					gameData.put("lottery_times",gameData.getInteger("lottery_times")+1);
				}
			} else {
				log.error("找不到当前场次的游戏数：{}",chang);
				return null;
			}
			//存放抽奖结果
			LotteryResult lotteryResult = new LotteryResult();
			lotteryResult.setPlayerId(player.getId());
			lotteryResult.setNick(player.getNick());
			//抽奖
			LotteryItem item = lotteryMgr.lottery(chang,lotteryResult);
			if( item == null ) {
				log.error("抽奖失败，奖励为空，场次：{}",chang);
				return null;
			}
			platformService.addItem(player.getId(),item.getId(),item.getNum(),GameEventEnum.LOTTERY);
			lotteryResult.setAwardType(item.getId());
			lotteryResult.setAwardNum(item.getNum());
			lotteryResult.setBase(item.getBase());
			//获取场次
			BilliardFileCodeConfig fileCode = excelMgr.getFileCodeById(chang);
			if (fileCode != null) {
				lotteryResult.setChang(fileCode.getPlayType() + fileCode.getMoneyType() + fileCode.getGrade());
			}
			//抽奖结果插入数据库
			lotteryResultService.insertLotteryResult(lotteryResult);
			//其他显示的奖励
			List<LotteryItem> lotteryItems = lotteryMgr.otherLottery(chang);
			BilliardProto.S2C_LotteryAward.Builder builder = BilliardProto.S2C_LotteryAward.newBuilder();
			builder.setLotteryItem(item.toProto().build());
			lotteryItems.forEach(lotteryItem -> {
				builder.addLotteryItems(lotteryItem.toProto().build());
			});
			S2C s2c = new S2C();
			s2c.setCid(Cmd.LOTTERY);
			s2c.setUid(c2s.getUid());
			s2c.setBody(builder.build().toByteArray());
			pushService.push(s2c);

			//推送游戏次数
			playerHandler.pushGameTimes(player);
			//如果抽中红包券，新增到红包墙中
			if (item.getId() == PropsEnum.RED_PACKET.getCode()) {
				platformService.addRedPacket(player.getId(),chang,item.getNum());
			}
		}
		return null;
	}
}
