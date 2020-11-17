//package com.wangpo.platform.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import com.wangpo.base.excel.SystemConfig;
//
//@Controller
//@RequestMapping("/systemconfig")
//public class SystemConfigController {
//
//    /**
//     * 新增系统配置
//     */
//    @RequestMapping(value = "/add")
//    @ResponseBody
//    public String add(SystemConfig config) {
//        this.systemConfigService.addConfig(config);
//        com.wangpo.base.excel.SystemConfig systemConfig = systemConfigService.getSystemConfig(config);
//        platformService.modifySystemConfig(1,systemConfig);
//        return SUCCESS_TIP;
//    }
//
//    /**
//     * 修改系统配置
//     */
//    @RequestMapping(value = "/update")
//    @Permission
//    @ResponseBody
//    public ResponseData update(@Valid SystemConfig config) {
//        systemConfigService.editConfig(config);
//        com.wangpo.base.excel.SystemConfig systemConfig = systemConfigService.getSystemConfig(config);
//        platformService.modifySystemConfig(3,systemConfig);
//        return SUCCESS_TIP;
//    }
//
//    /**
//     * 删除系统配置
//     */
//    @RequestMapping(value = "/delete")
//    @Permission
//    @ResponseBody
//    public ResponseData delete(@RequestParam("id") int id) {
//        systemConfigService.deleteConfig(id);
//        com.wangpo.base.excel.SystemConfig systemConfig = new com.wangpo.base.excel.SystemConfig();
//        systemConfig.setId(id);
//        platformService.modifySystemConfig(2,systemConfig);
//        return SUCCESS_TIP;
//    }
//
//
//}
