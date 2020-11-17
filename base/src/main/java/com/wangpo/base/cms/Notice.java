package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;

@Data
public class Notice implements Serializable {
    private int id;
    private String label;//标签
    private String uighur;//维语标签
    private String content;//内容
    private String uighurContent;//维语内容
    private int eject;//是否强制弹出
    private int sort;//排序

}
