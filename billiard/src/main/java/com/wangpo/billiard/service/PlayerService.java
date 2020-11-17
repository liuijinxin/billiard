package com.wangpo.billiard.service;

import com.wangpo.billiard.bean.Player;

import java.util.List;

public interface PlayerService {
	int insertPlayer(Player player);
	int updatePlayer(Player player);
	Player selectPlayerByID(Integer id);

	List<Player> selectRobot();
}
