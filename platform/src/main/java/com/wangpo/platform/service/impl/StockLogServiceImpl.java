package com.wangpo.platform.service.impl;

import com.wangpo.platform.bean.LoginLog;
import com.wangpo.platform.bean.StockLog;
import com.wangpo.platform.mapper.StockLogMapper;
import com.wangpo.platform.service.StockLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class StockLogServiceImpl implements StockLogService {
	@Resource
	StockLogMapper stockLogMapper;

	@Override
	public StockLog selectStockLogByPlayerId(String logDay) {
		return stockLogMapper.selectStockLogByLogDay(logDay);
	}

	@Override
	public StockLog sumStock(String logDay) {
		return stockLogMapper.sumStock(logDay);
	}

	@Override
	public StockLog sumActive(String logDay) {
		return stockLogMapper.sumActive(logDay);
	}

	@Override
	public int insertStockLog(StockLog stockLog) {
		return stockLogMapper.insertStockLog(stockLog);
	}
}
