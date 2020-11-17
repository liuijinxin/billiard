package com.wangpo.platform.service.impl;

import com.wangpo.base.cms.APPVersion;
import com.wangpo.platform.mapper.APPMapper;
import com.wangpo.platform.service.APPService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class APPServiceImpl implements APPService {
    @Resource
    APPMapper appMapper;

    @Override
    public List<APPVersion> selectAPPVersion() {
        return appMapper.selectAPPVersion();
    }

}
