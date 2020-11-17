package com.wangpo.billiard.service.impl;

import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.mapper.BilliardPlayerMapper;
import com.wangpo.billiard.service.PlayerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
	@Resource
	BilliardPlayerMapper mapper;

	@Override
	public int insertPlayer(Player player) {
		player.setUpdateTime(new Date());
		player.setCreateTime(new Date());
		return mapper.insertPlayer(player);
	}

	/**
	 * 测试任务调度，
	 */
//	@Scheduled(cron = "30 * * * * ?")
//	public void cronScheduled(){
//		System.out.println("=====================每分钟调度一次。。。。");
//	}

	@Override
	public int updatePlayer(Player player) {
		player.setUpdateTime(new Date());
		return mapper.updatePlayer(player);
	}

	@Override
	public Player selectPlayerByID(Integer id) {
		return mapper.selectPlayerByID(id);
	}

	@Override
	public List<Player> selectRobot() {
		return mapper.selectRobot();
	}
}
