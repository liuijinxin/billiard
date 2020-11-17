package com.wangpo.billiard.service;

import com.wangpo.billiard.bean.Role;

import java.util.List;

public interface RoleService {

    int insertRole(Role role);

    int updateRole(Role role);

    List<Role> selectRoleById(int playerId);


}
