package com.wangpo.platform.service.impl;

import com.wangpo.platform.dao.TaskEntity;
import com.wangpo.platform.mapper.TaskMapper;
import com.wangpo.platform.service.TaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Resource
    TaskMapper taskMapper;

    @Override
    public List<TaskEntity> selectAllTask(int playerId) {
        return taskMapper.selectAllTask(playerId);
    }

    @Override
    public int updateTask(TaskEntity taskEntity) {
        taskEntity.setUpdateTime(new Date());
        return taskMapper.updateTask(taskEntity);
    }

    @Override
    public List<TaskEntity> selectTask( int playerId,int taskType ) {
        return taskMapper.selectTask(playerId ,taskType);
    }

    @Override
    public int insertTask(TaskEntity taskEntity) {
        taskEntity.setCreateTime(new Date());
        taskEntity.setUpdateTime(new Date());
        return taskMapper.insertTask(taskEntity);
    }



}
