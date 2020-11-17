package com.wangpo.platform.controller;

import com.wangpo.base.cms.*;
import com.wangpo.base.excel.SystemConfig;
import com.wangpo.base.service.BilliardService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.bean.TaskData;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.task.BilliardTaskType;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.charge.ChargeHandler;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.logic.task.TaskHandler;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class TestController {
    @Resource
    TaskHandler taskHandler;
    @Resource
    PlatformService platformService;
    @Resource
    ChargeHandler chargeHandler;
    @Resource
    PlayerMgr playerMgr;
    @Resource
    private HttpServletRequest request;
    @DubboReference
    private BilliardService billiardService;
    @Resource
    BaseExcelMgr baseExcelMgr;
    @Resource
    ItemMgr itemMgr;

    @RequestMapping("/hello")
    public String index(@RequestParam(name="token", required=false ) String token, Model model){
        if( token == null) {
            token = (String)request.getSession().getAttribute("token");
        } else {
            request.getSession().setAttribute("token",token);
        }

        Player player = playerMgr.getPlayerByToken(token);
        if( player !=null ) {
            model.addAttribute("user", player);
            return "hello";
        }
        return "error";
    }

    @RequestMapping("/loading")
    public String loading(@RequestParam(name="uid", required=false ) String uid,
                          @RequestParam(name="origin", required=false ) String origin,Model model){
        model.addAttribute("uid", uid);
        model.addAttribute("origin", origin);
        return "loading";
    }


    @PostMapping("/addGold")
    public String modifyGold(int id, int gold) {
//        System.out.println(id+","+gold);
        platformService.modifyGold(id, gold, GameEventEnum.TEST.reason);
        return "redirect:/hello";
    }


    @PostMapping("/addItem")
    public String addItem(int id, int itemId,int itemNum) {
//        System.out.println(id+","+gold);
        Player player = playerMgr.getPlayerByID(id);
        if( player !=null ) {
            itemMgr.addItem(player,itemId,itemNum,GameEventEnum.TEST);
        }

        return "redirect:/hello";
    }

    @RequestMapping("/addDiamond")
    public String modifyDiamond(int id, int diamond) {
        chargeHandler.afterCharge(id,diamond);
        TaskData taskData = new TaskData();
        taskData.setTimes(diamond);
        taskData.setTaskType(BilliardTaskType.RECHARGE.code);
        taskHandler.finishTask(id, 3, taskData);
//        platformService.modifyDiamond(id, diamond, GameEventEnum.TASK_REWARD.reason);
        return "redirect:/hello";
    }

    @RequestMapping("/share")
    public String share(int id){
        TaskData taskData = new TaskData();
        taskData.setConditionId(1000);
        taskData.setTimes(1);
        taskData.setTaskType(BilliardTaskType.SHARE.code);
        taskHandler.finishTask(id, 3, taskData);
        return "redirect:/hello";
    }

    @RequestMapping("/sign")
    public String sign(int id){
        TaskData taskData = new TaskData();
        taskData.setTimes(1);
        taskData.setTaskType(BilliardTaskType.SIGN.code);
        taskHandler.finishTask(id, 3, taskData);
        return "redirect:/hello";
    }

    @RequestMapping("/play")
    public String play(int id,int money,int game,int chang,int win,int clearance){
//        System.out.println("id:"+id+",money:"+money+",game:"+game+",chang:"+chang+",win:"+win+",clearance:"+clearance);
        int conditionId = money*1000+game*10+chang;
        int times = 1;
        int taskType = 3;
        //完成赢一局任务
        if( win == 1) {
            taskType  = 2;
        }
        TaskData taskData = new TaskData();
        taskData.setConditionId(conditionId);
        taskData.setTimes(times);
        taskData.setTaskType(taskType);
        taskHandler.finishTask(id, 3, taskData);
        //完成一杆清台任务
        if (clearance == 1) {
            taskData.setTaskType(BilliardTaskType.ONE_GAN.code);
            taskHandler.finishTask(id, 3, taskData);
        }
        billiardService.updateRole(id,100);
        billiardService.addGameTimes(id,conditionId);
        return  "redirect:/hello";
    }

    @RequestMapping("/getMap")
    @ResponseBody
    public Map<Integer, CmsSystemNotice> map() {
        return baseExcelMgr.getSystemNoticeMap();
    }


}
