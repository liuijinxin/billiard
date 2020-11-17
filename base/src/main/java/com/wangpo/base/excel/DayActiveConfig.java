package com.wangpo.base.excel;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DayActiveConfig implements IConfig{
    private int id;
    //里程碑
    private int milepost;
    //奖励
    private String reward;
    private int icon;

    private JSONObject rewards = new JSONObject();

    @Override
    public void explain() {
//        String reward = dayActiveConfig.getReward();
			if (!"0".equals(reward)) {
				String[] split = reward.split(";");
				Map<String,Object> dayRewards = new HashMap<>();
				for (String s : split) {
					String[] split1 = s.split(",");
					dayRewards.put(split1[0],split1[1]);
				}
				JSONObject jsonObject = new JSONObject(dayRewards);
				setRewards(jsonObject);
			}
    }
}
