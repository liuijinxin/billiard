package com.wangpo.platform.dao;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class BuyOrder implements Serializable {
	private int id;
	/** 订单号*/
	private String paymentSn;
	/** 订单创建时间*/
	private Date addTime;
	/** 玩家id*/
	private int userId;
	/** 商品id*/
	private int  goodsId;
	/** 商品描述*/
	private String goodsTipName;
	/** 位置*/
	private int paymentCode;
	/** 订单状态 0未付款1已付款*/
	private int orderStatus;
	/** 订单来源*/
	private int orderSource;
	/** 订单数量*/
	private int orderAmount;
	/** 订单配置Id*/
	private int payConfigId;

}
