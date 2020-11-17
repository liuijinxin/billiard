package com.wangpo.platform.service.impl;

import com.wangpo.base.cms.ResourceConfig;
import com.wangpo.platform.mapper.ResourceMapper;
import com.wangpo.platform.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Resource
    ResourceMapper resourceMapper;

    @Override
    public List<ResourceConfig> getResourceConfig() {
        return resourceMapper.getResourceConfig();
    }
}
