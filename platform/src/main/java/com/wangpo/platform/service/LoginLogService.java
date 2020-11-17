package com.wangpo.platform.service;


import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.bean.LoginLog;

import java.util.List;

public interface LoginLogService {

    LoginLog selectLoginLogByPlayerId(int playerId,String today);

    int insertLoginLog(LoginLog gameLog);

    int updateLoginLog(LoginLog gameLog);

}
