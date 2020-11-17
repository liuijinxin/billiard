package com.wangpo.base.excel;

import lombok.Data;

@Data
public class BilliardCueTypeConfig implements IConfig{

    private int id;
    private String name;
    private String quality;
    private int star;
    private String price;
    private String fragmen;//碎片
    private String defend_30_times;
    private String defend_3_days;
    private String defend_7_days;
    private String defend_30_days;
    private String defend_365_days;
    private String damage;
    private int cueRes;

    private int buyType;
    private int buyPrice;
    
    private int fragmenType;
    private int fragmenNum;

    private int defend_30_times_type;
    private int defend_30_times_price;

    private int defend_3_days_type;
    private int defend_3_days_price;

    private int defend_7_days_type;
    private int defend_7_days_price;

    private int defend_30_days_type;
    private int defend_30_days_price;

    private int defend_365_days_type;
    private int defend_365_days_price;


    @Override
    public void explain() {
        if (!"0".equals(price)) {
            String[] split = price.split(",");
            setBuyType(Integer.parseInt(split[0]));
            setBuyPrice(Integer.parseInt(split[1]));
        }
        if (!"0".equals(fragmen)) {
            String[] split = fragmen.split(",");
            setFragmenType(Integer.parseInt(split[0]));
            setFragmenNum(Integer.parseInt(split[1]));
        }
        if (!"0".equals(defend_30_times)) {
            String[] split = defend_30_times.split(",");
            setDefend_30_times_type(Integer.parseInt(split[0]));
            setDefend_30_times_price(Integer.parseInt(split[1]));
        }
        if (!"0".equals(defend_3_days)) {
            String[] split = defend_3_days.split(",");
            setDefend_3_days_type(Integer.parseInt(split[0]));
            setDefend_3_days_price(Integer.parseInt(split[1]));
        }
        if (!"0".equals(defend_7_days)) {
            String[] split = defend_7_days.split(",");
            setDefend_7_days_type(Integer.parseInt(split[0]));
            setDefend_7_days_price(Integer.parseInt(split[1]));
        }
        if (!"0".equals(defend_30_days)) {
            String[] split = defend_30_days.split(",");
            setDefend_30_days_type(Integer.parseInt(split[0]));
            setDefend_30_days_price(Integer.parseInt(split[1]));
        }
        if (!"0".equals(defend_365_days)) {
            String[] split = defend_365_days.split(",");
            setDefend_365_days_type(Integer.parseInt(split[0]));
            setDefend_365_days_price(Integer.parseInt(split[1]));
        }
    }
}
