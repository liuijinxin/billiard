package com.wangpo.base.excel;

import lombok.Data;

@Data
public class BilliardChangConfig implements IConfig{
    private int id;
    //玩法类型
    private int playType;
    //场次等级
    private int star;
    //货币类型
    private int moneyType;
    //台费
    private int fee;
    //初始赌注
    private int bet;
    //翻倍上限
    private int doubling;
    //准入下限
    private int lowerLimit;
    //准入上限
    private int upperLimit;
    //抽成比例
    private double percentage;
    //出杆限制
    private int rod;
    //AI开关
    private int ai;
    //角色经验值
    private String exp;
    //抽奖开关
    private int lottery;
    //场次开关
    private int openFlag;
    //匹配超时时间
    private int matchTime;
    //匹配机器人时间
    private int aiTime;

    private int winExp;
    private int loseExp;

    @Override
    public void explain() {
        if (!"0".equals(exp)) {
            String[] split = exp.split(",");
            setWinExp(Integer.parseInt(split[0]));
            setLoseExp(Integer.parseInt(split[1]));
        }
    }
}
