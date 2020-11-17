package com.wangpo.base.excel;

import lombok.Data;

@Data
public class BilliardRoleConfig implements IConfig{
    private int id;
    private String name;
    //品级
    private String quality;
    //等级
    private int star;
    //购买价格
    private String price;
    //力量
    private int power;
    //加塞
    private int gase;
    //瞄准器
    private int aim;
    //战力
    private int combat;
    //升级经验
    private int roleExp;

    private int buyType;
    private int buyPrice;


    @Override
    public void explain() {
        if (!"0".equals(price)) {
            String[] split = price.split(",");
            setBuyType(Integer.parseInt(split[0]));
            setBuyPrice(Integer.parseInt(split[1]));
        }
    }
}
