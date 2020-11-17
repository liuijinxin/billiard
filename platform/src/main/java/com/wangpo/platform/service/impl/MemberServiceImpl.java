package com.wangpo.platform.service.impl;

import com.wangpo.platform.bean.PlayerVip;
import com.wangpo.platform.mapper.MemberMapper;
import com.wangpo.platform.service.MemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class MemberServiceImpl implements MemberService {
    @Resource
    MemberMapper memberMapper;

    @Override
    public PlayerVip selectMemberByPlayerId(int playerId) {
        return memberMapper.selectMemberByPlayerId(playerId);
    }

    @Override
    public int insertMember(PlayerVip playerVip) {
        playerVip.setUpdateTime(new Date());
        playerVip.setCreateTime(new Date());
        return memberMapper.insertMember(playerVip);
    }

    @Override
    public int updateMember(PlayerVip playerVip) {
        playerVip.setUpdateTime(new Date());
        return memberMapper.updateMember(playerVip);
    }


}
