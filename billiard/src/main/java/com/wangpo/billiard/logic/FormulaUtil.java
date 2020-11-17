package com.wangpo.billiard.logic;

/**
 *
 */
public class FormulaUtil {
	/**
	 * 构造台球场次。
	 * @param money 金币类型
	 * @param chang 玩法类型
	 * @param level 初中高级场次
	 * @return
	 */
	public static int genChang(int money,int chang,int level){
		return money*1000+chang*10+level;
	}
}
