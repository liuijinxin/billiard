package com.wangpo.platform.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.wangpo.platform.dao.PlayerGift;
import com.wangpo.platform.mapper.PlayerGiftMapper;
import com.wangpo.platform.service.PlayerGiftService;

@Service
public class PlayerGiftServiceImpl implements PlayerGiftService{
	
    @Resource
    PlayerGiftMapper playerGiftMapper;

	@Override
	public List<PlayerGift> selectPlayerGift(int playerId) {
		return playerGiftMapper.selectPlayerGift(playerId);
	}

	@Override
	public int insertPlayerGift(PlayerGift playerGift) {
		return playerGiftMapper.insertPlayerGift(playerGift);
	}

	@Override
	public int updatePlayerGift(PlayerGift playerGift) {
		return playerGiftMapper.updatePlayerGift(playerGift);
	}

	@Override
	public int deletePlayerGiftById(long id) {
		return playerGiftMapper.deletePlayerGiftById(id);
	}

	@Override
	public PlayerGift selectPlayerGiftById(int id,int playerId) {
		return playerGiftMapper.selectPlayerGiftById(id,playerId);
	}

}
