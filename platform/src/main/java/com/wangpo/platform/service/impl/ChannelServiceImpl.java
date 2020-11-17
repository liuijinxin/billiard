package com.wangpo.platform.service.impl;

import com.wangpo.base.cms.ChannelConfig;
import com.wangpo.platform.mapper.ChannelMapper;
import com.wangpo.platform.service.ChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {
    @Resource
    ChannelMapper channelMapper;

    @Override
    public List<ChannelConfig> getChannelConfig() {
        return channelMapper.getChannelConfig();
    }
}
