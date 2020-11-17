package com.wangpo.platform.dao;

import com.wangpo.base.bean.TaskCondition;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TaskEntity implements Serializable {
    /** 数据库唯一id */
    private int id;
    /** 玩家id */
    private int playerId;
    /** 任务配置id */
    private int taskId;
    /** 后置任务id */
    private int beforeTaskId;
    /** 游戏类型 */
    private int gameType;
    /** 任务类型 */
    private int taskType;
    /** 任务完成情况 */
    private int taskStatus;
    private Date createTime;
    private Date updateTime;
    /** 任务完成进度 */
    private List<TaskCondition> conditionIds = new ArrayList<>();

}