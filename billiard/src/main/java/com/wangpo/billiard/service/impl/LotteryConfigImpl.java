package com.wangpo.billiard.service.impl;

import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.billiard.mapper.LotteryConfigMapper;
import com.wangpo.billiard.service.LotteryConfigSevice;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LotteryConfigImpl implements LotteryConfigSevice {
    @Resource
    LotteryConfigMapper lotteryConfigMapper;

    @Override
    public List<CmsLotteryConfig> getCmsLotteryConfig() {
        return lotteryConfigMapper.getCmsLotteryConfig();
    }
}
