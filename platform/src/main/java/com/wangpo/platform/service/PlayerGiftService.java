package com.wangpo.platform.service;

import java.util.List;

import com.wangpo.platform.dao.PlayerGift;

public interface PlayerGiftService {
	
	List<PlayerGift> selectPlayerGift(int playerId);
	
	PlayerGift selectPlayerGiftById(int id,int playerId);
	
	int insertPlayerGift(PlayerGift playerGift);
	
	int updatePlayerGift(PlayerGift playerGift);
	
	int deletePlayerGiftById(long id);

}
