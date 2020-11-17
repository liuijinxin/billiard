package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;

@Data
public class CmsLotteryConfig implements Serializable {
    private int id;
    private int chang;
    private String name;
    private int num;
    private int type;
    private int weight;
    private int grade;

}
