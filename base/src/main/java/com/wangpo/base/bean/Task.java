package com.wangpo.base.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Task implements Serializable {
    private int taskId;
    private int afterTaskId;
    private int gameType;
    private int taskType;
    private String taskText;
    private String reward;

    private int id;
    //玩家id
    private int playerId;
    /** 任务状态 0-未完成，1已完成**/
    private int taskStatus;
    private JSONObject rewards = new JSONObject();
    private List<TaskCondition> conditionIds = new ArrayList<>();

    private Date createTime;
    private Date updateTime;

}