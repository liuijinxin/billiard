package com.wangpo.billiard.service.impl;

import com.wangpo.billiard.bean.LuckyCue;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.mapper.BilliardLuckyCue;
import com.wangpo.billiard.mapper.BilliardPlayerMapper;
import com.wangpo.billiard.service.LuckyCueService;
import com.wangpo.billiard.service.PlayerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class LuckyCueServiceImpl implements LuckyCueService {
	@Resource
	BilliardLuckyCue mapper;

	@Override
	public int insertLuckyCue(LuckyCue luckyCue) {
		return mapper.insertLuckyCue(luckyCue);
	}

	/**
	 * 测试任务调度，
	 */
//	@Scheduled(cron = "30 * * * * ?")
//	public void cronScheduled(){
//		System.out.println("=====================每分钟调度一次。。。。");
//	}

	@Override
	public int updateLuckyCue(LuckyCue luckyCue) {
		return mapper.updateLuckyCue(luckyCue);
	}

	@Override
	public LuckyCue selectLuckyCueByID(Integer id) {
		return mapper.selectLuckyCueByID(id);
	}
}
