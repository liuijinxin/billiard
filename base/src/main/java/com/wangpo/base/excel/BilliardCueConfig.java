package com.wangpo.base.excel;

import lombok.Data;

@Data
public class BilliardCueConfig implements IConfig{
    private int id;
    private String name;
    //品级
    private String quality;
    //星级
    private int star;
    //力度
    private int power;
    //加塞
    private int gase;
    //瞄准
    private int aim;
    //战力
    private int combat;
    //升级消耗
    private String upgrade;
    //出售价格
    private String sellPrice;

    private int upgradePayType;
    private int upgradePayPrice;

    private int sellPayType;
    private int sellPayPrice;

    @Override
    public void explain() {
//        String upgrade = cueConfig.getUpgrade();

            if (!"0".equals(upgrade)) {
                String[] split = upgrade.split(",");
                setUpgradePayType(Integer.parseInt(split[0]));
                setUpgradePayPrice(Integer.parseInt(split[1]));
            }
            if (!"0".equals(sellPrice)) {
                String[] split = sellPrice.split(",");
                setSellPayType(Integer.parseInt(split[0]));
                setSellPayPrice(Integer.parseInt(split[1]));
            }
//        String sellPrice = cueConfig.getSellPrice();

    }
}
