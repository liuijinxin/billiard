package com.wangpo.platform.service.impl;

import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.bean.LoginLog;
import com.wangpo.platform.mapper.GameLogMapper;
import com.wangpo.platform.mapper.LoginLogMapper;
import com.wangpo.platform.service.GameLogService;
import com.wangpo.platform.service.LoginLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LoginLogServiceImpl implements LoginLogService {
    @Resource
    LoginLogMapper mapper;


    @Override
    public LoginLog selectLoginLogByPlayerId(int playerId,String today) {
        return mapper.selectLoginLogByPlayerId(playerId,today);
    }

    @Override
    public int insertLoginLog(LoginLog loginLog) {
        return mapper.insertLoginLog(loginLog);
    }

    @Override
    public int updateLoginLog(LoginLog loginLog) {
        return mapper.updateLoginLog(loginLog);
    }
}
