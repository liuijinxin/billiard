package com.wangpo.base.excel;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DayTaskConfig implements IConfig{
    //条件配置id
    private int taskId;
    //游戏类型
    private int gameType;
    //任务类型
    private int taskType;
    //条件id
    private String conditionId;
    //总进度
    private int totalProgress;
    //任务文案
    private String taskText;
    //日活跃度
    private int dayActive;
    //奖励
    private String reward;

    //任务id（唯一）
//    private int id;
    //当前进度
    private int currentProgress;
    //奖励
    private JSONObject rewards = new JSONObject();
    //条件id
    private List<Integer> conditionIds = new ArrayList<>();

	@Override
	public int getId() {
		return taskId;
	}

	@Override
    public void explain() {
//			String conditionId = dayTaskConfig.getConditionId();
			List<Integer> conditionIds = new ArrayList<>();
			if (conditionId.contains(",")) {
				String[] split = conditionId.split(",");
				for (String s : split) {
					conditionIds.add(Integer.valueOf(s));
				}
			} else {
				conditionIds.add(Integer.valueOf(conditionId));
			}
			setConditionIds(conditionIds);
			//将奖励放到jsonobject中
//			String reward = dayTaskConfig.getReward();
			if (!"0".equals(reward)) {
				String[] split = reward.split(";");
				Map<String,Object> rewardMap = new HashMap<>();
				for (String s : split) {
					String[] split1 = s.split(",");
					rewardMap.put(split1[0],split1[1]);
				}
				JSONObject jsonObject = new JSONObject(rewardMap);
				setRewards(jsonObject);
			}
    }
}
