package com.wangpo.platform.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.wangpo.platform.dao.BuyOrder;
import com.wangpo.platform.mapper.BuyOrderMapper;
import com.wangpo.platform.mapper.PlayerMapper;
import com.wangpo.platform.service.BuyOrderService;

@Service
public class BuyOrderServiceImpl implements BuyOrderService{
	
    @Resource
    BuyOrderMapper buyOrderMapper;

	@Override
	public int insertBuyOrder(BuyOrder buyOrder) {
		return buyOrderMapper.insertBuyOrder(buyOrder);
	}

	@Override
	public BuyOrder selectBuyOrder(String paymentSn) {
		return buyOrderMapper.selectBuyOrder(paymentSn);
	}

	@Override
	public int updateBuyOrder(BuyOrder buyOrder) {
		return buyOrderMapper.updateBuyOrder(buyOrder);
	}

}
