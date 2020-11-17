package com.wangpo.base.excel;

import lombok.Data;

@Data
public class BilliardLotteryConfig implements IConfig {
    private int id;
    private String name;
    private int num;
    private int type;
    private int weight;
    private int grade;

    @Override
    public void explain() {

    }
}
