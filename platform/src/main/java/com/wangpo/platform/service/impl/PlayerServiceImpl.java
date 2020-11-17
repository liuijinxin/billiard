package com.wangpo.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.mapper.PlayerMapper;
import com.wangpo.platform.service.PlayerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
    @Resource
    PlayerMapper playerMapper;


    @Override
    public Player selectPlayerById(int id) {
        return playerMapper.selectPlayerById(id);
    }

    @Override
    public Player selectPlayerByToken(String token) {
        return playerMapper.selectPlayerByToken(token);
    }

    @Override
    public Player selectPlayerByOpenid(String openId) {
        return playerMapper.selectPlayerByOpenid(openId);
    }
    
	@Override
	public Player selectPlayerByPhone(String phone) {
		return playerMapper.selectPlayerByPhone(phone);
	}



    @Override
    public int insertPlayer(Player player) {
        player.setUpdateTime(new Date());
        player.setCreateTime(new Date());
        return playerMapper.insertPlayer(player);
    }

    @Override
    public int updatePlayer(Player player) {
        player.setUpdateTime(new Date());
        return playerMapper.updatePlayer(player);
    }

    @Override
    public int updateGoldById(int id, int goldNum) {
        return playerMapper.updateGoldById(id,goldNum);
    }

    @Override
    public int updateDiamondById(int id, int diamondNum) {
        return playerMapper.updateDiamondById(id,diamondNum);
    }

    @Override
    public int updateIdcard(Player player){
        return playerMapper.updateIdcard(player);
    }

    @Override
    public int updateRedPacketById(int id, int redPacketNum) {
        return playerMapper.updateRedPacketById(id,redPacketNum);
    }

    @Override
    public int updateAlipayById(int id, JSONObject alipay) {
        return playerMapper.updateAlipayById(id,alipay);
    }
    
	@Override
	public int updatePhoneById(int id, String phone) {
		return playerMapper.updatePhoneById(id, phone);
	}

}
