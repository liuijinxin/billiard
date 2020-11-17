package com.wangpo.billiard.service;

import com.wangpo.billiard.bean.PlayerCue;
import java.util.List;

public interface BilliardCueService {

    List<PlayerCue> selectCueByPlayerId(Integer playerId);

    int deleteCueById(int id);

    int addCue(PlayerCue playerCue);

    PlayerCue selectCueById(Integer id, int cueId);

    int upgradeCue(PlayerCue playerCue);

    int updateUseCueByPlayerId(Integer id);

    int updateUseCue(PlayerCue playerCue);

    int updateCueDefend(PlayerCue playerCue);

    int updateCue(PlayerCue playerCue);

}
