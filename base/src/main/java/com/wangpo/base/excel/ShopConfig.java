package com.wangpo.base.excel;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

@Data
public class ShopConfig implements Serializable {
    /**
     * 商品id
     */
    private int goodsId;

    /**
     * 商品状态，0下架，1上架
     */
    private int status;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 维语展示
     */
    private String showName;

    /**
     * 付费类型，1人民币，2红包
     */
    private int payType;

    /**
     * 价格，单位分
     */
    private int price;

    /**
     * 货币类型：1 金币、2钻石、 3 微信红包、 4 话费、5复活新手礼包（限一次）、6月卡、7复活月卡、11支付宝红包、12京东卡
     */
    private int goodsType;

    /**
     * 增加货币数量
     */
    private int count;

    /**
     * 赠送的货币数量
     */
    private int addCount;

    /**
     * 永久限购数量，0为未限制
     */
    private int evenLimit;

    /**
     * 每日限购数量，0为无限制
     */
    private int dayLimit;

    /**
     * 游戏角标展示，1永久限购，2每日限购，3买赠，4热销，5打折
     */
    private int promotionType;

    /**
     * 角标展示数值，主要展示买多少送多少，或者打折多少，限购的具体次数等
     */
    private int promotionValue;

    /**
     * 生效效果，持续小时，主要用于月卡，记牌器等
     */
    private int effectTimes;

    /**
     * 循环周期
     */
    private int cycleTimes;

    /**
     * 使用房间等级，0为无限制，主要用于复活卡
     */
    private int roomLimit;

    /**
     * 限制支付类型，1支付宝，2微信，3银联，0无限制
     */
    private int channalLimit;
    /**
     * 购买商品获得的道具Id
     */
    private int itemId;
    /**
     * 额外的商品
     */
    private JSONObject bonusItem = new JSONObject();


}
