package com.wangpo.base.kits;

import java.time.Duration;
import java.util.Date;

public class DateKit {
	/**
	 * 日期差值
	 * @param old   较老的日期
	 * @param newer 较新的日期
	 * @return  相差的天数
	 */
	public static long between(Date old,Date newer) {
		return Duration.between(old.toInstant(),newer.toInstant()).toDays();
	}

	public static void main(String[] args) {
		Date d1 = new Date();
		Date d2 = new Date();
		d2.setTime(d2.getTime() - 49*60*60*1000);
		System.out.println(between(d2,d1));
	}
}
