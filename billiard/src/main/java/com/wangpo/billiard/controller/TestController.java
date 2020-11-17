package com.wangpo.billiard.controller;

import com.alibaba.fastjson.JSONObject;
import com.sun.xml.bind.v2.model.core.ID;
import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.base.cms.MatchConfig;
import com.wangpo.base.excel.BilliardFileCodeConfig;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.service.PlayerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RequestMapping("/")
@RestController
public class TestController {
    @Resource
    ExcelMgr excelMgr;
    @Resource
    PlayerService playerService;

    @RequestMapping("/getMap")
    public Map getMap() {
        Map<Integer, CmsChangConfig> changConfigMap = excelMgr.getCmsChangConfigMap();
        return changConfigMap;
    }

    @RequestMapping("/getMap1")
    public Map getMap1() {
        Map<Integer, CmsLotteryConfig> lotteryConfigMap = excelMgr.getLotteryConfigMap();
        return lotteryConfigMap;
    }

}
