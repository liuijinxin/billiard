package com.wangpo.base.excel;

import lombok.Data;

@Data
public class ItemConfig implements IConfig{
    //道具唯一ID
    private int id;
    private String name;
    private String code;
    //所属游戏ID，3-台球
    private int game;
    private String icon;
    //道具描述
    private String describe;

    @Override
    public void explain() {

    }
}
