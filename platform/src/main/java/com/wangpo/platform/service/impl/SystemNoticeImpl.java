package com.wangpo.platform.service.impl;

import com.wangpo.base.cms.CmsSystemNotice;
import com.wangpo.platform.mapper.SystemNoticeMapper;
import com.wangpo.platform.service.SystemNoticeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemNoticeImpl implements SystemNoticeService {
    @Resource
    SystemNoticeMapper systemNoticeMapper;

    @Override
    public List<CmsSystemNotice> getSystemNotice() {
        return systemNoticeMapper.getSystemNotice();
    }
}
