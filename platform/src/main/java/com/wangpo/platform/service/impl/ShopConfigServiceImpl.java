package com.wangpo.platform.service.impl;

import com.wangpo.base.excel.ShopConfig;
import com.wangpo.platform.mapper.ShopConfigMapper;
import com.wangpo.platform.service.ShopConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShopConfigServiceImpl implements ShopConfigService {
    @Resource
    ShopConfigMapper shopConfigMapper;

    @Override
    public List<ShopConfig> getShopConfig() {
        return shopConfigMapper.getShopConfig();
    }

	@Override
	public int insertShopConfig(ShopConfig shopConfig) {
		return shopConfigMapper.insertShopConfig(shopConfig);
	}

	@Override
	public int updateShopConfig(ShopConfig shopConfig) {
		return shopConfigMapper.updateShopConfig(shopConfig);
	}

	@Override
	public int deleteShopConfig(long id) {
		return shopConfigMapper.deleteShopConfig(id);
	}
}
