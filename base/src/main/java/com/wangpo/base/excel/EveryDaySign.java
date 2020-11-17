package com.wangpo.base.excel;


import com.alibaba.fastjson.JSONObject;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class EveryDaySign implements IConfig{
	
    private int id;
    private int dayNum;
    private int gold;
    private int diamond;
    private String itemNum;
    private int repeat;  

    private JSONObject itemNums = new JSONObject();

    @Override
    public void explain() {
        //将奖励放到jsonobject中
        if (!"0".equals(itemNum)) {
            String[] split = itemNum.split("_");
            Map<String,Object> rewardMap = new HashMap<>();
            for (String s : split) {
                String[] split1 = s.split(",");
                rewardMap.put(split1[0],split1[1]);
            }
            JSONObject jsonObject = new JSONObject(rewardMap);
            setItemNums(jsonObject);
        }
    }
}
