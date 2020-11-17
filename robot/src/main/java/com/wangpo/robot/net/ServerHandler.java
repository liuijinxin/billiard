package com.wangpo.robot.net;

import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.CommonUser;
import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.robot.logic.ClientMgr;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 消息处理类
 */
//@Sharable
//@Service()
//@Scope("prototype")
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final AttributeKey<Integer> idAttr = AttributeKey.newInstance("idAttr");

    private Channel outboundChannel;
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final WebSocketClientHandshaker handshaker;
//    private ChannelHandlerContext channel;
    private ChannelPromise handshakeFuture;
    private int id;

    public ServerHandler(int id,  WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
        this.id = id;
//        this.channel = channel;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
//        System.out.println("handlerAdded");
        handshakeFuture = ctx.newPromise();
    }

    /**
     * 客户端连接
     *
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        System.out.println("channelActive");
        handshaker.handshake(ctx.channel());
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
    }

    /**
     * 读取消息
     *
     * @param ctx   通道上下文
     * @param msg 协议
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
//                System.out.println("WebSocket 握手成功!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);

            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.getStatus()
                    + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        if(msg instanceof S2C) {
            S2C s2c = (S2C)msg;
            try {
//                log.info("收到消息号：{}",s2c.getCid());
                ClientMgr.map.get(id).gotMsg(s2c);
            } catch (Exception e) {
                log.error("处理消息异常：",  e);
            }
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            // resposnse(ctx, frame);

            ctx.writeAndFlush(textFrame.text());
//            System.out.println("WebSocket Client received message: " + textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
//            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
//            System.out.println("WebSocket Client received closing");
            ch.close();
        } else if ( frame instanceof BinaryWebSocketFrame ) {

        }


    }

    public void handleLogin(ChannelHandlerContext ctx,C2S c2s) throws Exception {
//        log.info("处理登录");
    }


    /**
     * 处理台球游戏的协议
     * @param ctx   websocket链接
     * @param c2s 包体内容
     */
    private void handleBilliard(ChannelHandlerContext ctx, C2S c2s) throws Exception{
//    	log.info("收到消息号：{}",c2s.getCid());
        if( c2s.getCid() < 1000 ) {
            //非登录请求直接转发游戏服
            Integer id = ctx.channel().attr(idAttr).get();
        }  else if( ctx.channel().hasAttr(idAttr) ){
            //非登录请求直接转发游戏服
            Integer id = ctx.channel().attr(idAttr).get();
            c2s.setUid(id);
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
//        log.error("异常消息", cause);
        ctx.close();
    }

}
