package com.wangpo.base.excel;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.item.Item;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GoodsConfig implements IConfig{
    private int id;
    /** 游戏类型 */
    private int gameType;
    /** 购买所需货币类型及数值 */
    private String buyType;
    /** 购买道具ID及数量 */
    private String item;

    private List<Item> itemList = new ArrayList();

    @Override
    public void explain() {

    }
}
