package com.wangpo.platform.service.impl;

import com.wangpo.base.excel.SystemConfig;
import com.wangpo.platform.mapper.SystemConfigMapper;
import com.wangpo.platform.service.SystemConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    @Resource
    SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> selectSystemConfig() {
        return systemConfigMapper.selectSystemConfig();
    }

}
