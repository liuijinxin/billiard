package com.wangpo.platform.service.impl;

import com.wangpo.platform.dao.UserEntity;
import com.wangpo.platform.mapper.UserMapper;
import com.wangpo.platform.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
	@Resource
	private UserMapper userMapper;

	@Override
	public int insert(UserEntity userEntity) {
		return userMapper.insert(userEntity);
	}

	@Override
	public int update(UserEntity userEntity) {
		userEntity.setUpdateTime(new Date());
		return userMapper.update(userEntity);
	}

	@Override
	public int delete(int id) {
		return userMapper.delete(id);
	}

	@Override
	public UserEntity selectByID(int id) {
		return userMapper.selectByID(id);
	}

	@Override
	public UserEntity selectByToken(String token) {
		return userMapper.selectByToken(token);
	}
}
