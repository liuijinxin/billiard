package com.wangpo.base.excel;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MemberConfig implements IConfig {
    private int id;
    private int point;
    private String dayReward;
    private String upgradeReward;
    private int decline;
    private int power;
    private int gase;
	private int aim;
	private int combat;
	//红包兑换次数
	private int exchangeTimes;

    private JSONObject dayRewards = new JSONObject();
    private JSONObject upgradeRewards = new JSONObject();

    @Override
    public void explain() {
			if (!"0".equals(dayReward)) {
				String[] split = dayReward.split(";");
				Map<String,Object> dayRewards = new HashMap<>();
				for (String s : split) {
					String[] split1 = s.split(",");
					dayRewards.put(split1[0],split1[1]);
				}
				JSONObject jsonObject = new JSONObject(dayRewards);
				setDayRewards(jsonObject);
			}
//			String upgradeReward = memberConfig.getUpgradeReward();
			if (!"0".equals(upgradeReward)) {
				String[] split2 = upgradeReward.split(";");
				Map<String,Object> upgradeRewards = new HashMap<>();
				for (String s1 : split2) {
					String[] split3 = s1.split(",");
					upgradeRewards.put(split3[0],split3[1]);
				}
				JSONObject jsonObject = new JSONObject(upgradeRewards);
				setUpgradeRewards(jsonObject);
			}
    }
}
