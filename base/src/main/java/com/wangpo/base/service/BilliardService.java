package com.wangpo.base.service;


import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.bean.CommonUser;
import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.base.cms.MatchConfig;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.excel.SystemConfig;

import java.util.Map;

public interface BilliardService {
    void logout(int uid);
    /**
     * 台球服游戏请求rpc
     * @param message   请求包体
     * @return  应答包体
     */
    S2C request (C2S message) ;

    /**
     * 同步玩家信息
     * 当平台服玩家数据有改变时，需要同步到各个游戏服。
     */
    void syncUser(CommonUser user);

    /**
     * 平台服通过任务，活动等系统奖励道具到各个游戏服。
     * @param uid   玩家id
     * @param modelId   道具配置id
     * @param num   道具数量
     */
    void syncItem(int uid, int modelId, int num, GameEventEnum gameEventEnum);

    /**
     * 同步系统配置到台球游戏服
     * @param map
     */
    void syncSystemConfig(Map<Integer, SystemConfig> map);

    void updateRole(int uid,int exp);

    /**
     * GM测试直接添加场次次数
     * @param id 玩家id
     * @param chang 场次
     */
    void addGameTimes(int id, int chang);

    Map<String, Integer> getGameData();

    /**
     * 修改匹配配置
     * @param type 类型
     * @param matchConfig 匹配配置
     */
    void modifyMatchConfig(int type, MatchConfig matchConfig);

    /**
     * 修改抽奖配置
     * @param type 类型
     * @param cmsLotteryConfig 抽奖配置
     */
    void modifyLotteryConfig(int type, CmsLotteryConfig cmsLotteryConfig);

    /**
     * 修改场次配置
     * @param type 类型
     * @param cmsChangConfig 场次配置
     */
    void modifyChangConfig(int type, CmsChangConfig cmsChangConfig);

}
