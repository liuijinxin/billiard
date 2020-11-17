package com.wangpo.platform.service;

import com.wangpo.platform.dao.UserEntity;

public interface UserService {
	int insert(UserEntity userEntity);

	int update(UserEntity userEntity);

	int delete(int id);

	UserEntity selectByID(int id);

	UserEntity selectByToken(String token);
}
