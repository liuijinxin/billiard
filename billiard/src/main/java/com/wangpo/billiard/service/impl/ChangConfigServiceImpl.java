package com.wangpo.billiard.service.impl;

import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.billiard.mapper.ChangConfigMapper;
import com.wangpo.billiard.service.ChangConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChangConfigServiceImpl implements ChangConfigService {
    @Resource
    ChangConfigMapper changConfigMapper;

    @Override
    public List<CmsChangConfig> getChangConfig() {
        return changConfigMapper.getChangConfig();
    }
}
