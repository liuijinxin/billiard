package com.wangpo.billiard.framework;

public interface Strategy {

	/**
	 * 传入表名 和分表参数
	 * @param tableName
	 * @param splitParam
	 * @return
	 */
	String returnTableName(String tableName,Object splitParam);

}
