package com.wangpo.platform.service.impl;

import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.platform.dao.RedPacketEntity;
import com.wangpo.platform.mapper.RedPacketMapper;
import com.wangpo.platform.service.RedPacketService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class RedPacketServiceImpl implements RedPacketService {
    @Resource
    RedPacketMapper redPacketMapper;

    @Override
    public int insertRedPacket(RedPacketEntity redPacketEntity) {
        redPacketEntity.setUpdateTime(new Date());
        redPacketEntity.setCreateTime(new Date());
        return redPacketMapper.insertRedPacket(redPacketEntity);
    }

    @Override
    public List<RedPacketEntity> selectAllRedPacket() {
        return redPacketMapper.selectAllRedPacket();
    }

}
