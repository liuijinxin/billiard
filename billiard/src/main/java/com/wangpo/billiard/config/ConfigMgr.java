package com.wangpo.billiard.config;

import com.wangpo.base.cms.ChannelConfig;
import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.base.cms.MatchConfig;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.util.FightUtil;
import com.wangpo.billiard.service.ChangConfigService;
import com.wangpo.billiard.service.LotteryConfigSevice;
import com.wangpo.billiard.service.MatchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class ConfigMgr {
    @Resource
    ExcelMgr excelMgr;
    @Resource
    MatchService matchService;
    @Resource
    LotteryConfigSevice lotteryConfigSevice;
    @Resource
    ChangConfigService changConfigService;

    /**
     * 初始化配置
     */
    public void initConfig(){
        //读取系统配置
        List<MatchConfig> matchConfigList = matchService.selectAllMatchConfig();
        Map<Integer, MatchConfig> matchConfigMap = excelMgr.getMatchConfigMap();
        matchConfigList.forEach(matchConfig -> {
            matchConfigMap.put(matchConfig.getId(),matchConfig);
            FightUtil.winWeight = matchConfig.getWinWeight();
            FightUtil.cueWeight = matchConfig.getCueWeight();
            FightUtil.streakWeight = matchConfig.getStreakWeight();
            FightUtil.isOpen = matchConfig.getIsOpen();
        });

        //读取抽奖配置
        List<CmsLotteryConfig> cmsLotteryConfig = lotteryConfigSevice.getCmsLotteryConfig();
        Map<Integer, CmsLotteryConfig> lotteryConfigMap = excelMgr.getLotteryConfigMap();
        cmsLotteryConfig.forEach(config -> {
            lotteryConfigMap.put(config.getId(),config);
        });

        //读取场次配置
        List<CmsChangConfig> changConfig = changConfigService.getChangConfig();
        Map<Integer, CmsChangConfig> cmsChangConfigMap = excelMgr.getCmsChangConfigMap();
        changConfig.forEach(config -> {
            cmsChangConfigMap.put(config.getChang(),config);
        });


    }

    /**
     * 修改匹配配置
     * @param type 类型
     * @param matchConfig 匹配配置
     */
    public void modifyMatchConfig(int type, MatchConfig matchConfig) {
        Map<Integer, MatchConfig> matchConfigMap = excelMgr.getMatchConfigMap();
        if (type == 1 || type == 3) {
            matchConfigMap.put(matchConfig.getId(),matchConfig);
            FightUtil.winWeight = matchConfig.getWinWeight();
            FightUtil.cueWeight = matchConfig.getCueWeight();
            FightUtil.streakWeight = matchConfig.getStreakWeight();
            FightUtil.isOpen = matchConfig.getIsOpen();
        } else if (type == 2) {
            matchConfigMap.remove(matchConfig.getId());
        }
    }

    /**
     * 修改抽奖配置
     * @param type 类型
     * @param cmsLotteryConfig 抽奖配置
     */
    public void modifyLotteryConfig(int type, CmsLotteryConfig cmsLotteryConfig) {
        Map<Integer, CmsLotteryConfig> lotteryConfigMap = excelMgr.getLotteryConfigMap();
        if (type == 1 || type == 3) {
            lotteryConfigMap.put(cmsLotteryConfig.getId(),cmsLotteryConfig);
        } else if (type == 2) {
            lotteryConfigMap.remove(cmsLotteryConfig.getId());
        }
    }

    /**
     * 修改场次配置
     * @param type 类型
     * @param cmsChangConfig 场次配置
     */
    public void modifyChangConfig(int type, CmsChangConfig cmsChangConfig) {
        Map<Integer, CmsChangConfig> cmsChangConfigMap = excelMgr.getCmsChangConfigMap();
        if (type == 1 || type == 3) {
            cmsChangConfigMap.put(cmsChangConfig.getChang(),cmsChangConfig);
        } else if (type == 2) {
            cmsChangConfigMap.remove(cmsChangConfig.getChang());
        }
    }
}
