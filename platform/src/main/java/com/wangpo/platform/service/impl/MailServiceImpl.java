package com.wangpo.platform.service.impl;

import com.wangpo.base.bean.Mail;
import com.wangpo.platform.mapper.MailMapper;
import com.wangpo.platform.service.MailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class MailServiceImpl implements MailService {
    @Resource
    MailMapper mailMapper;

    @Override
    public List<Mail> selectMailByPlayerId(int playerId) {
        return mailMapper.selectMailByPlayerId(playerId);
    }

    @Override
    public int updateMail(Mail mail) {
        return mailMapper.updateMail(mail);
    }

    @Override
    public int insertMail(Mail mail) {
        mail.setCreateTime(new Date());
        return mailMapper.insertMail(mail);
    }

    @Override
    public List<Mail> selectSystemMail() {
        return mailMapper.selectSystemMail();
    }

    @Override
    public int deleteMailById(long id) {
        return mailMapper.deleteMailById(id);
    }


}
