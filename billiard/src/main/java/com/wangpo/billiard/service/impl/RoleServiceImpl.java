package com.wangpo.billiard.service.impl;

import com.wangpo.billiard.bean.Role;
import com.wangpo.billiard.mapper.RoleMapper;
import com.wangpo.billiard.service.RoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    RoleMapper roleMapper;

    @Override
    public int insertRole(Role role) {
        role.setUpdateTime(new Date());
        role.setCreateTime(new Date());
        return roleMapper.insertRole(role);
    }

    @Override
    public int updateRole(Role role) {
        role.setUpdateTime(new Date());
        return roleMapper.updateRole(role);
    }

    @Override
    public List<Role> selectRoleById(int playerId) {
        return roleMapper.selectRoleById(playerId);
    }



}
