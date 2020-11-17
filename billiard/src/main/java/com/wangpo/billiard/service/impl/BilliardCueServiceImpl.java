package com.wangpo.billiard.service.impl;

import com.wangpo.billiard.service.BilliardCueService;
import com.wangpo.billiard.bean.PlayerCue;
import com.wangpo.billiard.mapper.BilliardCueMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class BilliardCueServiceImpl implements BilliardCueService {
    @Resource
    BilliardCueMapper cueMapper;

    @Override
    public List<PlayerCue> selectCueByPlayerId(Integer playerId) {
        return cueMapper.selectCueByPlayerId(playerId);
    }

    @Override
    public int deleteCueById(int id) {
        return cueMapper.deleteCueById(id);
    }

    @Override
    public int addCue(PlayerCue playerCue) {
        playerCue.setUpdateTime(new Date());
        playerCue.setCreateTime(new Date());
        return cueMapper.addCue(playerCue);
    }

    @Override
    public PlayerCue selectCueById(Integer id, int cueId) {
        return cueMapper.selectCueById(id,cueId);
    }

    @Override
    public int upgradeCue(PlayerCue playerCue) {
        playerCue.setUpdateTime(new Date());
        return cueMapper.upgradeCue(playerCue);
    }

    @Override
    public int updateUseCueByPlayerId(Integer id) {
        return cueMapper.updateUseCueByPlayerId(id);
    }

    @Override
    public int updateUseCue(PlayerCue playerCue) {
        playerCue.setUpdateTime(new Date());
        return cueMapper.updateUseCue(playerCue);
    }

    @Override
    public int updateCueDefend(PlayerCue playerCue) {
        playerCue.setUpdateTime(new Date());
        return cueMapper.updateCueDefend(playerCue);
    }

    @Override
    public int updateCue(PlayerCue playerCue) {
        playerCue.setUpdateTime(new Date());
        return cueMapper.updateCue(playerCue);
    }
}
