package com.wangpo.platform.service.impl;

import com.wangpo.base.cms.Notice;
import com.wangpo.platform.mapper.NoticeMapper;
import com.wangpo.platform.service.NoticeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Resource
    NoticeMapper noticeMapper;

    @Override
    public List<Notice> getAllNotice() {
        return noticeMapper.getAllNotice();
    }
}
