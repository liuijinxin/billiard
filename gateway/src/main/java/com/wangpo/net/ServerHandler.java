package com.wangpo.net;

import com.wangpo.base.bean.*;
import com.wangpo.base.bean.PlatFormProto.S2C_PushAuthentication;
import com.wangpo.service.HandlerService;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

import javax.annotation.Resource;

/**
 * 消息处理类
 */
@Sharable
@Service()
@Scope("prototype")
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<C2S> {
    AttributeKey<Integer> idAttr = AttributeKey.newInstance("idAttr");

    /**
     * 客户端连接
     *
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 添加
        log.info("客户端与服务端连接开启：{}",ctx.channel().remoteAddress());
    }

    /**
     * 客户端关闭
     *
     * @param ctx 上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 移除
        ctx.close();
//        log.info("客户端与服务端连接关闭：{}",ctx.channel().remoteAddress());
        Attribute<Integer> attr = ctx.channel().attr(idAttr);

        if( attr!= null   ) {
            Integer uid = attr.get();
            if(uid!=null && uid>0) {
//                用户断线，通知平台服和各个游戏服。
                handlerService.logout(uid);
            }
        }
    }

    /**
     * 读取消息
     *
     * @param ctx   通道上下文
     * @param c2s 协议
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, C2S c2s) {
        try {
            if( c2s.getCid() == 100 ){
                handleLogin(ctx,c2s);
            } else {
                switch (c2s.getSid()) {
                    case 4:
//                        log.info("捕鱼服");
                        break;
                    case 5:
                        //台球游戏
                        handleBilliard(ctx, c2s);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error("处理消息异常,消息id:{},：{}", c2s.getCid(),e);
        }
    }

    public void handleLogin(ChannelHandlerContext ctx,C2S c2s) throws Exception {
        //登录

        CommonUser user = handlerService.queryUser(c2s);
        if( user == null ) {
            log.error("登录异常，平台返回为空");
            S2C s2c = new S2C();
            s2c.setCid(100);
            s2c.setCode(1);
            ctx.channel().writeAndFlush(s2c);
            return;
        }
        if (user.getStatus() == 1) {
            log.error("玩家已被冻结");
            S2C s2c = new S2C();
            s2c.setCid(100);
            s2c.setCode(2);
            ctx.channel().writeAndFlush(s2c);
            return;
        } else if( user.getStatus() == 2) {
            log.error("账号可能被删除，需要重新拉取微信登录");
            S2C s2c = new S2C();
            s2c.setCid(100);
            s2c.setCode(3);
            ctx.channel().writeAndFlush(s2c);
            return;
        }
        log.info("从平台获取用户信息：{}",user);
        ctx.channel().attr(idAttr).set(user.getId());
        S2C s2c = new S2C();
        s2c.setCid(100);
        s2c.setUid(user.getId());
        s2c.setBody(PlatFormProto.S2C_Login.newBuilder()
                .setId(user.getId())
                .setToken(user.getToken())
                .setDiamond(user.getDiamond())
                .setRedPacket(user.getRedPacket())
                .setGold(user.getGold())
                .setHead(user.getHead())
                .setNick(user.getNick())
                .setNociveGuideNum(user.getNociveGuideNum().toJSONString())
                .build().toByteArray());
        ctx.channel().writeAndFlush(s2c);
        //添加channel到channelService
        channelService.addChannel(user.getId(),ctx);
        c2s.setUid(user.getId());
        //1，通知台球游戏服
        handlerService.request(c2s);
        //登录后推送消息
        handlerService.afterLogin(user.getId());
    }


    @Resource
    private HandlerService handlerService;

    @Resource
    private ChannelService channelService;

    /**
     * 处理台球游戏的协议
     * @param ctx   websocket链接
     * @param c2s 包体内容
     */
    private void handleBilliard(ChannelHandlerContext ctx, C2S c2s) throws Exception{
        log.info("收到台球服消息，指令：{}，是否登录:{}", c2s.getCid(),ctx.channel().hasAttr(idAttr));
        if( c2s.getCid() < 1000 ) {
            //非登录请求直接转发游戏服
            Integer id = ctx.channel().attr(idAttr).get();
            c2s.setUid(id);
            S2C s2c = handlerService.requestPlatform(c2s);
            if( s2c != null ) {
	            ctx.channel().writeAndFlush( s2c );
            }
        }  else if( ctx.channel().hasAttr(idAttr) ){
            //非登录请求直接转发游戏服
            Integer id = ctx.channel().attr(idAttr).get();
            c2s.setUid(id);
            //非登录请求，发送给台球服,将结果返回给客户端
            S2C s2c = handlerService.request(c2s);
            if( s2c != null ) {
                s2c.setSid(c2s.getSid());
                ctx.channel().writeAndFlush(s2c);
            }
        }
    }

    /**
     * 异常消息
     *
     * @param ctx   通道上下文
     * @param cause 线程
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常消息", cause);
        ctx.close();
    }

}
