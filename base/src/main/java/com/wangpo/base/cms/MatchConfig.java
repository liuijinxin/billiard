package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;

@Data
public class MatchConfig implements Serializable {
    /** 数据库唯一id */
    private int id;
    /** 杆数权重 */
    private double cueWeight;
    /** 胜率权重 */
    private double winWeight;
    /** 连胜权重 */
    private double streakWeight;
    /** 是否启用匹配机制 */
    private int isOpen;

}
