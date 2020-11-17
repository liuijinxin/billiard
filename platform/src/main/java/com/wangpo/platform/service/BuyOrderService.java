package com.wangpo.platform.service;

import com.wangpo.platform.dao.BuyOrder;

public interface BuyOrderService {
	
	int insertBuyOrder(BuyOrder buyOrder);
	
	BuyOrder selectBuyOrder(String paymentSn);
	
	int updateBuyOrder(BuyOrder buyOrder);

}
