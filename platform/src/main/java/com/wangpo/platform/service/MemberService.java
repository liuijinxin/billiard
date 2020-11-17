package com.wangpo.platform.service;

import com.wangpo.platform.bean.PlayerVip;

public interface MemberService {

    PlayerVip selectMemberByPlayerId(int playerId);

    int insertMember(PlayerVip playerVip);

    int updateMember(PlayerVip playerVip);



}
