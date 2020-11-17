package com.wangpo.platform.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.wangpo.platform.dao.WxFollower;
import com.wangpo.platform.mapper.WxFollowerMapper;
import com.wangpo.platform.service.WxFollowerService;

@Service
public class WxFollowerServiceImpl implements WxFollowerService {
	
	@Resource
	private WxFollowerMapper wxFollowerMapper;

	@Override
	public WxFollower selectByID(String unionId) {
		return wxFollowerMapper.selectByID(unionId);
	}

}
