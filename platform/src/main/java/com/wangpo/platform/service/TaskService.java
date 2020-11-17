package com.wangpo.platform.service;


import com.wangpo.platform.dao.TaskEntity;

import java.util.List;

public interface TaskService {

    List<TaskEntity> selectAllTask(int playerId);

    int updateTask(TaskEntity taskEntity);

    List<TaskEntity> selectTask( int playerId,int taskType );

    int insertTask(TaskEntity taskEntity);

}
