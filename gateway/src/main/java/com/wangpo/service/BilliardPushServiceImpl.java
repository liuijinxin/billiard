package com.wangpo.service;

import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.S2C;
import com.wangpo.net.ChannelService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/*
 * 注意这里的注解
 * com.alibaba.dubbo.config.annotation.Service
 */
@DubboService(timeout = 2000)
@Component
@Slf4j
public class BilliardPushServiceImpl implements BilliardPushService {
    @Resource
    private ChannelService channelService;
    @Resource
    private HandlerService handlerService;

    @Override
    public void push(S2C s2c) {

        ChannelHandlerContext ctx = channelService.getChannel(s2c.getUid());
        if( ctx != null ) {
//            log.info("push:{},{}",s2c.getCid(),s2c.getUid());
//            if( s2c.getCid()==101) {
////                log.info("发送更新。。");
//            }
            ctx.writeAndFlush(s2c);
        } else if( s2c.getUid() > 0 ) {
            log.error("找不到 ctx,cid:{}",s2c.getCid());
        }
    }

    @Override
    public void close(int uid) {
        ChannelHandlerContext ctx = channelService.getChannel( uid );
        if( ctx != null ) {
            ctx.close();
            handlerService.logout(uid);
        }
    }
}
