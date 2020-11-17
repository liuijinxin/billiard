package com.wangpo.billiard.service.impl;

import com.wangpo.billiard.bean.LotteryResult;
import com.wangpo.billiard.mapper.LotteryResultMapper;
import com.wangpo.billiard.service.LotteryResultService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class LotteryResultServiceImpl implements LotteryResultService {
    @Resource
    LotteryResultMapper lotteryResultMapper;

    @Override
    public int insertLotteryResult(LotteryResult lotteryResult) {
        lotteryResult.setCreateTime(new Date());
        return lotteryResultMapper.insertLotteryResult(lotteryResult);
    }


}
