package com.wangpo.platform.framework;

import org.apache.poi.ss.usermodel.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class YYYYMMStrategy implements Strategy {

	@Override
	public String returnTableName(String tableName, Object param) {
		try {
			// 结果类似 20190601
			return tableName+"_"+ get_MMStr(param);
		} catch (ParseException e) {
			e.printStackTrace();
			return tableName;
		}
	}

	public static String get_MMStr(Object dataStr) throws ParseException {
		return getStrByDateFormat("yyyyMM", (Date)dataStr );
	}


	public static String getStrByDateFormat(String formater, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(formater);
		return sdf.format(date);
	}

}
