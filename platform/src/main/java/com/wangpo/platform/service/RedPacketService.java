package com.wangpo.platform.service;


import com.wangpo.platform.dao.RedPacketEntity;

import java.util.List;

public interface RedPacketService {

    int insertRedPacket(RedPacketEntity redPacketEntity);

    List<RedPacketEntity> selectAllRedPacket();

}
