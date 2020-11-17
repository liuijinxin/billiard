package com.wangpo.base.service;

import com.wangpo.base.bean.S2C;

public interface BilliardPushService {
    /**
     * 台球游戏推送 RPC
     * @param s2c 推送消息包体
     */
    void push(S2C s2c) ;

    void close(int uid);
}
