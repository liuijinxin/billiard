package com.wangpo.billiard.logic.lottery;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖池
 */

@Data
public class LotteryVo {
	private int chang;
	private List<LotteryItem> lotteryItems = new ArrayList<>();
	private int totalMoney;
	private LotteryItem first;
	private LotteryItem second;
	private LotteryItem third;
}
