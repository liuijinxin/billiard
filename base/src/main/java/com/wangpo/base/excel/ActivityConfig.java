package com.wangpo.base.excel;

import lombok.Data;

@Data
public class ActivityConfig implements IConfig {
	
	/**
     * 活动Id
     */
    private int id;

    /**
     * 活动描述
     */
    private String activityInfo;

    /**
     * 活动开关
     */
    private String activityStatus;
    
    /**
     * 活动商品
     */
    private String activityShop;

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void explain() {
		// TODO Auto-generated method stub
		
	}


}
