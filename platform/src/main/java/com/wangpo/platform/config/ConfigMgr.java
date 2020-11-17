package com.wangpo.platform.config;

import com.wangpo.base.cms.*;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.excel.SystemConfig;
import com.wangpo.base.service.BilliardService;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.service.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class ConfigMgr {
    @Resource
    SystemConfigService systemConfigService;
    @Resource
    BaseExcelMgr baseExcelMgr;
    @Resource
    ShopConfigService shopConfigService;
    @Resource
    APPService appService;
    @Resource
    ChannelService channelService;
    @Resource
    ResourceService resourceService;
    @Resource
    NoticeService noticeService;
    @Resource
    SystemNoticeService systemNoticeService;
    @Resource
    BilliardService billiardService;

    /**
     * 初始化配置
     */
    public void initConfig(){
        //读取系统配置
        List<SystemConfig> systemConfigs = systemConfigService.selectSystemConfig();
        Map<Integer, SystemConfig> systemConfigMap = baseExcelMgr.getSystemConfigMap();
        systemConfigs.forEach(systemConfig -> {
            systemConfigMap.put(systemConfig.getId(),systemConfig);
        });
        billiardService.syncSystemConfig(systemConfigMap);

        //读取商城配置
        List<ShopConfig> shopConfigs = shopConfigService.getShopConfig();
        Map<Integer, ShopConfig> shopConfigMap = baseExcelMgr.getShopConfigMap();
        shopConfigs.forEach(shopConfig -> {
            shopConfigMap.put(shopConfig.getGoodsId(),shopConfig);
        });

        //读取APP版本管理
        List<APPVersion> appVersions = appService.selectAPPVersion();
        Map<Integer, APPVersion> appVersionMap = baseExcelMgr.getAppVersionMap();
        appVersions.forEach(appVersion -> {
            appVersionMap.put(appVersion.getId(),appVersion);
        });

        //读取资源渠道配置
        List<ChannelConfig> channelConfigs = channelService.getChannelConfig();
        Map<Integer, ChannelConfig> channelConfigMap = baseExcelMgr.getChannelConfigMap();
        channelConfigs.forEach(channelConfig -> {
            channelConfigMap.put(channelConfig.getId(),channelConfig);
        });

        //读取资源管理
        List<ResourceConfig> resourceConfigs = resourceService.getResourceConfig();
        Map<Integer, ResourceConfig> resourceConfigMap = baseExcelMgr.getResourceConfigMap();
        resourceConfigs.forEach(resourceConfig -> {
            resourceConfigMap.put(resourceConfig.getId(),resourceConfig);
        });

        //读取公告配置
        List<Notice> allNotice = noticeService.getAllNotice();
        Map<Integer, Notice> noticeMap = baseExcelMgr.getNoticeMap();
        allNotice.forEach(notice -> {
            noticeMap.put(notice.getId(),notice);
        });

        //读取登录公告
        List<CmsSystemNotice> noticeList = systemNoticeService.getSystemNotice();
        Map<Integer, CmsSystemNotice> systemNoticeMap = baseExcelMgr.getSystemNoticeMap();
        noticeList.forEach(notice -> {
            systemNoticeMap.put(notice.getId(),notice);
        });


    }

    /**
     * 修改商品配置
     * @param type 类型
     * @param shopConfig 商品配置
     */
    public void modifyShopConfig(int type, ShopConfig shopConfig) {
        Map<Integer, ShopConfig> shopConfigMap = baseExcelMgr.getShopConfigMap();
        if (type == 1 || type == 3) {
            shopConfigMap.put(shopConfig.getGoodsId(),shopConfig);
        } else if (type == 2) {
            shopConfigMap.remove(shopConfig.getGoodsId());
        }
    }

    /**
     * 修改系统配置
     * @param type 1增加，2删除，3修改
     * @param systemConfig 系统配置
     */
    public void modifySystemConfig(int type, SystemConfig systemConfig) {
        Map<Integer, SystemConfig> systemConfigMap = baseExcelMgr.getSystemConfigMap();
        if (type == 1 || type == 3) {
            systemConfigMap.put(systemConfig.getId(),systemConfig);
        } else if (type == 2) {
            systemConfigMap.remove(systemConfig.getId());
        }

        billiardService.syncSystemConfig(systemConfigMap);
    }

    /**
     * 修改版本配置
     * @param type 1增加，2删除，3修改
     * @param appVersion 版本配置
     */
    public void modifyAppVersion(int type, APPVersion appVersion) {
        Map<Integer, APPVersion> appVersionMap = baseExcelMgr.getAppVersionMap();
        if (type == 1 || type == 3) {
            appVersionMap.put(appVersion.getId(),appVersion);
        } else if (type == 2) {
            appVersionMap.remove(appVersion.getId());
        }
    }

    /**
     * 修改渠道配置
     * @param type 1增加，2删除，3修改
     * @param channelConfig 渠道配置
     */
    public void modifyChannelConfig(int type, ChannelConfig channelConfig) {
        Map<Integer, ChannelConfig> channelConfigMap = baseExcelMgr.getChannelConfigMap();
        if (type == 1 || type == 3) {
            channelConfigMap.put(channelConfig.getId(),channelConfig);
        } else if (type == 2) {
            channelConfigMap.remove(channelConfig.getId());
        }
    }

    /**
     * 修改资源配置
     * @param type 1增加，2删除，3修改
     * @param resourceConfig 资源配置
     */
    public void modifyResourceConfig(int type, ResourceConfig resourceConfig) {
        Map<Integer, ResourceConfig> resourceConfigMap = baseExcelMgr.getResourceConfigMap();
        if (type == 1 || type == 3) {
            resourceConfigMap.put(resourceConfig.getId(),resourceConfig);
        } else if (type == 2) {
            resourceConfigMap.remove(resourceConfig.getId());
        }
    }

    /**
     * 修改公告信息
     * @param type 1增加，2删除，3修改
     * @param notice 公告信息
     */
    public void modifyNotice(int type, Notice notice) {
        Map<Integer, Notice> noticeMap = baseExcelMgr.getNoticeMap();
        if (type == 1 || type == 3) {
            noticeMap.put(notice.getId(),notice);
        } else if (type == 2) {
            noticeMap.remove(notice.getId());
        }
    }

    /**
     * 修改公告信息
     * @param type 1增加，2删除，3修改
     * @param notice 公告信息
     */
    public void modifyCmsSystemNotice(int type, CmsSystemNotice notice) {
        Map<Integer, CmsSystemNotice> noticeMap = baseExcelMgr.getSystemNoticeMap();
        if (type == 1 || type == 3) {
            noticeMap.put(notice.getId(),notice);
        } else if (type == 2) {
            noticeMap.remove(notice.getId());
        }
    }

}
