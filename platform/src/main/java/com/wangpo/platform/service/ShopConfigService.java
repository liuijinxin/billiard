package com.wangpo.platform.service;


import com.wangpo.base.excel.ShopConfig;

import java.util.List;

public interface ShopConfigService {

    List<ShopConfig> getShopConfig();
    
    int insertShopConfig(ShopConfig shopConfig);
    
    int updateShopConfig(ShopConfig shopConfig);
    
    int deleteShopConfig(long id);

}
