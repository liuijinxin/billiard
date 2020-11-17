package com.wangpo.controller;

import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.service.HandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
/*

http://localhost:7008/getParam
响应参数：[Hello,Cicada]

http://localhost:7008/getUserInfo
响应参数：{"id":1,"userName":"知了"}

 */
@RestController
@Slf4j
public class DubboController {

    @Resource
    private HandlerService service ;

    @RequestMapping("/login")
    public String c2s (){
        C2S c2s = new C2S();
        c2s.setCid(1001);//登录测试
        S2C s2c = service.request(c2s);
        if(s2c!=null) {
//	        log.info("登录返回cid:"+s2c.getCid());
        }
        return "登录成功";
    }

    /**
     * 抛出超时异常
     * com.alibaba.dubbo.remoting.TimeoutException
     */
    /*@RequestMapping("/timeOut")
    public String timeOut (){
        return consumeService.timeOut(2000) ;
    }*/

    /**
     * 测试接口版本
     * 启动日志
     * <dubbo:reference object="com.alibaba.dubbo.common.bytecode.proxy1@3ad65"
     * singleton="true"
     * interface="com.boot.common.VersionService"
     * uniqueServiceName="com.boot.common.VersionService:1.0.0"
     * generic="false" version="1.0.0"
     * id="com.boot.common.VersionService" /> has been built.
     */
    /*@RequestMapping("/getVersion1")
    public String getVersion1 (){
        return versionService1.getVersion() ;
    }

    @RequestMapping("/getVersion2")
    public String getVersion2 (){
        return versionService2.version2() ;
    }*/
}
