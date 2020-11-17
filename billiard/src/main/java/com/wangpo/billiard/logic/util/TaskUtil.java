package com.wangpo.billiard.logic.util;

import com.wangpo.base.bean.TaskData;

public class TaskUtil {
	/**
	 * 构造TaskData
	 * @param taskType  任务类型：对应任务配置文件的具体任务类型
	 * @param conditionId   条件ID
	 * @param times         条件次数
	 * @return  TaskData
	 */
	public static TaskData createTaskData(int taskType,int times,int conditionId){
		TaskData taskData = new TaskData();
		taskData.setTimes(times);
		taskData.setTaskType(taskType);
		taskData.setConditionId(conditionId);
		return taskData;
	}
}
