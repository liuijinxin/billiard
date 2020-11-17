package com.wangpo.billiard.service.impl;

import com.wangpo.base.cms.MatchConfig;
import com.wangpo.billiard.mapper.MatchMapper;
import com.wangpo.billiard.service.MatchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MatchServiceImpl implements MatchService {
    @Resource
    MatchMapper matchMapper;

    @Override
    public List<MatchConfig> selectAllMatchConfig() {
        return matchMapper.selectAllMatchConfig();
    }
}
