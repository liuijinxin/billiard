package com.wangpo.base.excel;

import lombok.Data;

@Data
public class BilliardFileCodeConfig implements IConfig {
    private int id;
    //金币类型
    private String moneyType;
    //玩法
    private String playType;
    //等级
    private String grade;


    @Override
    public void explain() {

    }

}
