package com.wangpo.platform.service;


import com.wangpo.base.bean.Mail;

import java.util.List;

public interface MailService {

    List<Mail> selectMailByPlayerId(int playerId);

    int updateMail(Mail mail);

    int insertMail(Mail mail);

    List<Mail> selectSystemMail();

    int deleteMailById(long id);



}
