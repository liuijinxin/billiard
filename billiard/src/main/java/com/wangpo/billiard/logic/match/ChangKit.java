package com.wangpo.billiard.logic.match;

import com.wangpo.billiard.logic.FormulaUtil;

public class ChangKit {
//	for(int i=1;i<=2;i++) { //1-金币场，2-钻石场
//		for (int j = 1; j <= 4; j++) {//1-九球玩法，2-红球玩法，3-15张牌抽牌玩法，4-54张牌抽牌玩法。
//			if(i==1 && j>2) continue;
//			for (int k = 1; k <= 3; k++) {//1-低级场，2-中级场，3-高级场。
//				int chang = FormulaUtil.genChang(i,j,k);
//				player.getChang().put(String.valueOf(chang), 0);
//			}
//		}
//	}
	/**
	 * 场次定义
	 */
	public static final int[] CHANG = {
			//金币
			1011,1012,1013,
			1021,1022,1023,
			1031,1032,1033,
			1041,1042,1043,
			//钻石
			2011,2012,2013,
			2021,2022,2023,
			2031,2032,2033,
			2041,2042,2043
	};

	public static String toChang(int chang) {
		return String.valueOf(chang);
	}

	public static String toLotteryChang(int chang) {
		return "L_"+chang;
	}

	public static boolean isLotteryChang(String chang) {
		return chang.startsWith("L_");
	}
}
