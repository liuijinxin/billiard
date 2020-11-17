package com.wangpo.billiard.logic.util;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.billiard.bean.Player;

/**
 *  战力公式计算辅助类
 */
public class FightUtil {
	public static double streakWeight = 0.3;
	public static double winWeight = 0.5;
	public static double cueWeight = 0.2;
	public static int isOpen = 0;//战力匹配开关0关闭，1开启

	// 初始战力json
	public static JSONObject initFightData() {
		JSONObject fightJson = new JSONObject();

		fightJson.put("streak",0);// 连胜
		fightJson.put("streakWeight",streakWeight);// 连胜权重
		fightJson.put("win",0);// 胜利场次
		fightJson.put("total",0);// 总场次
		fightJson.put("winWeight",winWeight);// 胜率权重 默认为0.5，支持2位小数，通过后台配置，最大值为1，最小值为0.01
		fightJson.put("cueWeight",cueWeight);// 杆数权重：默认为0.2，支持2位小数，通过后台配置，最大值为1，最小值为0.01
		fightJson.put("cue",0); //总杆数

		fightJson.put("fight",fight(fightJson));
		return fightJson;
	}

	/**
	 * 场次转游戏玩法
	 * @param chang 四位场次
	 * @return  游戏玩法 => 1，经典黑八，2，红球玩法，3，15张抽牌玩法，4，52张抽牌玩法
	 */
	public static int chang2game(int chang){
		int gameId =  (chang/10)%100;
		//抽牌场次分开
		//gameId= Math.min(gameId, 3);
		return gameId;
	}

	//刷新战力参数,
	public static void refresh(Player player,int cueNum,int chang,boolean win) {
		int gameId = chang2game(chang);
		String key = String.valueOf(gameId);
		JSONObject gameJson = player.getFight().getJSONObject(key );
		//54张抽牌未创建，则新建游戏数据
		if (gameJson==null) {
			gameJson = FightUtil.initFightData();
			player.getFight().put(String.valueOf(gameId), gameJson);
		}
		FightUtil.addCue(gameJson,cueNum);
		FightUtil.addTotal(gameJson );
		if( win) {
			FightUtil.addWin(gameJson );
			FightUtil.addStreak(gameJson,1);
		} else {
			FightUtil.addStreak(gameJson,-1);
		}
		//更新战力
		gameJson.put("fight",fight(gameJson));
		player.getFight().put(key,gameJson);
	}

	public static void modifyGameTimes(Player player, int chang, int num,int moneyType,int result) {
		String key = String.valueOf(chang);
		JSONObject gameData = null;
		if( player.getChang().containsKey(String.valueOf(chang)) ) {
			Object obj = player.getChang().get(String.valueOf(chang));
			if( !(obj instanceof JSONObject)) {
				initGameData(player,key, 0);
			}
		} else {
			initGameData(player,key, 1);
		}
		gameData = (JSONObject) player.getChang().get(String.valueOf(chang));
		if( result>0) {
			if(!gameData.containsKey("win_times")) {
				gameData.put("win_times", num);
			} else {
				gameData.put("win_times",gameData.getInteger("win_times")+num);
			}

		}
		gameData.put("game_times",gameData.getInteger("game_times")+num);
		gameData.put("moneyType",moneyType);
		gameData.put("total_money",gameData.getInteger("total_money")+result);
	}

	public static void initGameData(Player player, String chang, int v) {
		JSONObject gameData = new JSONObject();
		gameData.put("win_times",0);
		gameData.put("game_times",0);
		gameData.put("lottery_times",0);
		gameData.put("moneyType",0);
		gameData.put("total_money",0);
		player.getChang().put(chang,gameData);
	}

	public static void modifyLotteryTimes(Player player,int chang) {
		String key = String.valueOf(chang);
		JSONObject gameData;
		if( player.getChang().containsKey(String.valueOf(chang)) ) {
			gameData = (JSONObject) player.getChang().get(String.valueOf(chang));
			gameData.put("lottery_times",gameData.getInteger("lottery_times")+3);
		}
	}

	public static int getLotteryTimes(Player player,int chang) {
		JSONObject gameData;
		if( player.getChang().containsKey(String.valueOf(chang)) ) {
			gameData = (JSONObject) player.getChang().get(String.valueOf(chang));
			return gameData.getInteger("game_times") - gameData.getInteger("lottery_times");
		}
		return 0;
	}

	//计算游戏最大输赢
	public static int calculateMaxLose(){
		return 0;
	}

	public static void addCue(JSONObject json, int cueNum) {
		json.put("cue",json.getInteger("cue")+cueNum);
	}

	public static void addTotal( JSONObject json ) {
		json.put("total",json.getInteger("total")+1);
	}

	public static void addWin( JSONObject json ) {
		json.put("win",json.getInteger("win")+1);
	}

	public static void addStreak(JSONObject json, int num) {
		if( num > 0 ) {
			json.put("streak",json.getInteger("streak")+1);
		} else {
			if( json.getInteger("streak") >=0 ) {
				json.put("streak",-1);
			} else {
				json.put("streak",json.getInteger("streak")-1);
			}
		}
	}

	// 战力公式：(场次连胜数量*连胜权重+场次胜率*10*胜率权重-场次杆数均值/3*杆数权重)*100
	public static int fight(JSONObject fightJson){
		//新手默认-200
		if( fightJson.getInteger("total")<=0) {
			return -200;
		}
		int streak = fightJson.getInteger("streak");
		double streakWeight = FightUtil.streakWeight;//fightJson.getDouble("streakWeight");
		// 场次胜率：每个场次单独统计胜利场次与总场次，再将胜利场次/总场次得出该值，支持2位小数，如该场次的游戏次数为0，则胜率默认为0
		double winRate = 0;
		if(fightJson.getInteger("total")>0) {
			winRate = fightJson.getInteger("win")/fightJson.getInteger("total");
		}
		double winWeight = FightUtil.winWeight;// fightJson.getDouble("winWeight");
		//	场次杆数均值：需要统计用户每一局游戏的出杆次数与该场次的游戏局数，而后出杆次数总和/场次游戏局数即为场次杆数均值。如该场次的出杆次数与游戏局数为0，则默认场次杆数均值为0
		double averageCue = 0;
		if( fightJson.getInteger("total")>0) {
			averageCue = fightJson.getInteger("cue")/fightJson.getInteger("total");
		}
		double cueWeight = FightUtil.cueWeight;// fightJson.getDouble("cueWeight");
		return (int)((streak*streakWeight+winRate*10*winWeight-averageCue/3*cueWeight)*100);
	}

	public static void main(String[] args) {
		System.out.println(5&4);
	}
}
