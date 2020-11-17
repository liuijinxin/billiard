package com.wangpo.platform.dao;

import lombok.Data;

@Data
public class WxFollower {
	private int id;

	private String openId;//微信公众号ID

	private String unionId;//

	private int subscribe;//是否关注

	private long subscribeTime;//最后一次关注时间

	private long createTime;//创建时间

}
