package com.wangpo.base.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import java.io.Serializable;

@Data
public class TaskData implements Serializable {
	//任务类型,每个游戏具体分配任务类型，不同游戏可以重复
	private int taskType;
	//条件ID，平台服制定
	private int conditionId;
	//条件ID完成次数
	private int times;
	//附加参数,具体逻辑自定义
	private JSONObject json;
}
