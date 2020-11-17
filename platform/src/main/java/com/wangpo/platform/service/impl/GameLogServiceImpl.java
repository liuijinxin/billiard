package com.wangpo.platform.service.impl;

import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.mapper.GameLogMapper;
import com.wangpo.platform.service.GameLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class GameLogServiceImpl implements GameLogService {
    @Resource
    GameLogMapper mapper;


    @Override
    public List<GameLog> selectGameLogByPlayerId(int playerId) {
        return mapper.selectGameLogByPlayerId(playerId);
    }

    @Override
    public int insertGameLog(GameLog gameLog) {
        gameLog.setCreateTime(new Date());
        return mapper.insertGameLog(gameLog);
    }
}
