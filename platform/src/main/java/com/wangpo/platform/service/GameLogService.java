package com.wangpo.platform.service;


import com.wangpo.platform.bean.GameLog;

import java.util.List;

public interface GameLogService {

    List<GameLog> selectGameLogByPlayerId(int playerId);

    int insertGameLog(GameLog gameLog);


}
