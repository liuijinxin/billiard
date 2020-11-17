package com.wangpo.base.bean;

import lombok.Data;

@Data
public class TaskCondition {
    private int conditionId;
    private int currentProgress;
    private int totalProgress;

}
