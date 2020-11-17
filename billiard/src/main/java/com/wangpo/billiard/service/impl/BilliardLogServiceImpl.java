package com.wangpo.billiard.service.impl;

import com.wangpo.billiard.bean.BilliardLog;
import com.wangpo.billiard.mapper.BilliardLogMapper;
import com.wangpo.billiard.service.BilliardLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BilliardLogServiceImpl implements BilliardLogService {

	@Resource
	BilliardLogMapper billiardLogCue;
	@Override
	public int insertBilliardLog(BilliardLog log) {
		return billiardLogCue.insertBilliardLog(log);
	}
}
