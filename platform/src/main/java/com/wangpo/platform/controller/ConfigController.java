package com.wangpo.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.cms.APPVersion;
import com.wangpo.base.cms.ChannelConfig;
import com.wangpo.base.cms.ResourceConfig;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.excel.SystemConfig;
import com.wangpo.platform.config.ConfigMgr;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.service.ShopConfigService;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class ConfigController {
    @Resource
    BaseExcelMgr excelMgr;
	@Resource
	ConfigMgr configMgr;
    @Resource
    BaseExcelMgr baseExcelMgr;
    @Resource
    ShopConfigService shopConfigService;

    @RequestMapping(value = "/getSystemConfig")
    public JSONObject getSystemConfig() {
        Map<Integer, SystemConfig> systemConfigMap = excelMgr.getSystemConfigMap();
        JSONObject jsonObject = new JSONObject();
        systemConfigMap.forEach((id, systemConfig) -> {
            jsonObject.put(systemConfig.getSystemKey(), systemConfig.getSystemValue());
        });
        return jsonObject;
    }

    @RequestMapping("/check_version")
    public JSONObject checkVersion(@RequestParam(value = "channel", required = false) String channel) {
        if (channel == null ||  "".equals(channel)) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        //判断是否有渠道
        if (excelMgr.getChannelConfigMap().size() > 0) {
            ChannelConfig channelConfig = null;
            //根据渠道名找到对应的渠道
            Iterator<ChannelConfig> iterator = excelMgr.getChannelConfigMap().values().iterator();
            while (iterator.hasNext()) {
                ChannelConfig next = iterator.next();
                if (next.getName().equals(channel)) {
                    channelConfig = next;
                }
            }
            if (channelConfig != null) {
                //从渠道获取apk版本
                String apkVersion = channelConfig.getApkVersion();
                Map<Integer, APPVersion> appVersionMap = excelMgr.getAppVersionMap();
                APPVersion version = new APPVersion();
                //通过apk版本找到对应的版本配置信息
                for (APPVersion appVersion : appVersionMap.values()) {
                    if (apkVersion.equals(appVersion.getVersion())) {
                        version = appVersion;
                        break;
                    }
                }
//                jsonObject.put("apk_url", version.getDownload());
                jsonObject.put("apk_url", channelConfig.getDownload());
                jsonObject.put("apk_ver", version.getVersion());
                jsonObject.put("project_url", version.getManifest());
                //从渠道获取资源版本号
                String resourceVersion = channelConfig.getVersion();
                Map<Integer, ResourceConfig> resourceConfigMap = excelMgr.getResourceConfigMap();
                ResourceConfig resourceConfig = new ResourceConfig();
                //通过资源版本号找到对应的资源配置信息
                for (ResourceConfig config : resourceConfigMap.values()) {
                    if (resourceVersion.equals(config.getVersion())) {
                        resourceConfig = config;
                        break;
                    }
                }
                jsonObject.put("res_url", resourceConfig.getUrl());
                jsonObject.put("res_ver", resourceConfig.getVersion());
            }
        }
        return jsonObject;
    }
    
    
    @RequestMapping(value = "/add")
    @ResponseBody
	public void modifyShopConfig(ShopConfig shopConfig) {
		configMgr.modifyShopConfig(1, shopConfig);
		shopConfigService.insertShopConfig(shopConfig);
	}
    
    @RequestMapping(value = "/delete")
    @ResponseBody
	public void delete(@RequestParam("id") int id ){
    	Map<Integer, ShopConfig> shopConfigMap = baseExcelMgr.getShopConfigMap();
    	ShopConfig shopConfig = shopConfigMap.get(id);
		configMgr.modifyShopConfig(2, shopConfig);
		shopConfigService.deleteShopConfig(id);
	}
    
    /**
     * 下架
     */
    @RequestMapping(value = "/freeze")
    @ResponseBody
	public void freeze(@RequestParam(value = "goodsId", required = false)String goodsId) {
    	Map<Integer, ShopConfig> shopConfigMap = baseExcelMgr.getShopConfigMap();
    	ShopConfig shopConfig = shopConfigMap.get(Integer.valueOf(goodsId));
    	shopConfig.setStatus(0);
		configMgr.modifyShopConfig(3, shopConfig);
		shopConfigService.updateShopConfig(shopConfig);
	}
    
    /**
     * 上架
     */
    @RequestMapping(value = "/unfreeze")
    @ResponseBody
	public void unfreeze(@RequestParam(value = "goodsId", required = false) String goodsId) {
    	Map<Integer, ShopConfig> shopConfigMap = baseExcelMgr.getShopConfigMap();
    	ShopConfig shopConfig = shopConfigMap.get(Integer.valueOf(goodsId));
    	shopConfig.setStatus(1);
		configMgr.modifyShopConfig(3, shopConfig);
		shopConfigService.updateShopConfig(shopConfig);
	}
    
    @RequestMapping(value = "/update")
    @ResponseBody
	public void update(ShopConfig shopConfig) {
		configMgr.modifyShopConfig(3, shopConfig);
		shopConfigService.updateShopConfig(shopConfig);
	}

}
