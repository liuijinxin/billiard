package com.wangpo.platform.service;

import com.wangpo.platform.dao.WxFollower;

public interface WxFollowerService {
	
	WxFollower selectByID(String unionId);

}
