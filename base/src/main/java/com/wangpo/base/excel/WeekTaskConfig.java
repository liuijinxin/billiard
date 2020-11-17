package com.wangpo.base.excel;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WeekTaskConfig implements IConfig{
    private int taskId;
    private int gameType;
    private int taskType;
    private String conditionId;
    private int totalProgress;
    private String taskText;
    private int weekActive;
    private String reward;

//    private int id;
    private int currentProgress;
    private JSONObject rewards = new JSONObject();
    private List<Integer> conditionIds = new ArrayList<>();

	@Override
	public int getId() {
		return taskId;
	}

	@Override
    public void explain() {
        //条件id
//			String conditionId = weekTaskConfig.getConditionId();
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
//			String reward = weekTaskConfig.getReward();
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
