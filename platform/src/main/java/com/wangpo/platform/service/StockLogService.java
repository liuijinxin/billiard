package com.wangpo.platform.service;


import com.wangpo.platform.bean.LoginLog;
import com.wangpo.platform.bean.StockLog;

public interface StockLogService {

    StockLog selectStockLogByPlayerId( String logDay );

    StockLog sumStock( String logDay );

    StockLog sumActive( String logDay );

    int insertStockLog(StockLog stockLog);

}
