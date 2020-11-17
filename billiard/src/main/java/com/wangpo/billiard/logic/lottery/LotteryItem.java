package com.wangpo.billiard.logic.lottery;

import com.wangpo.base.bean.BilliardProto;
import lombok.Data;

@Data
public class LotteryItem {
	private int id;
	private int num;
	private int grade;
	private int base;
//	private int total;

	public BilliardProto.LotteryItem.Builder toProto(){
		return BilliardProto.LotteryItem.newBuilder().setId(id).setNum(num).setGrade(grade);
	}


}
