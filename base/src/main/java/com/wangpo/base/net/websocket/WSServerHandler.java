package com.wangpo.base.net.websocket;

import com.wangpo.base.net.IJSONHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class WSServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger log = LoggerFactory.getLogger(WSServerHandler.class);

    private IJSONHandler handler;

    public WSServerHandler(IJSONHandler handler){
        this.handler = handler;
    }
    /**
     * 客户端连接
     *
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.handler.channelActive(ctx);
    }

    /**
     * 客户端关闭
     *
     * @param ctx 上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 移除
//        ctx.close();
        this.handler.channelInactive(ctx);
//        log.info("客户端与服务端连接关闭");
    }

    /**
     * 读取消息
     *
     * @param ctx   通道上下文
     * @param proto 协议
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String proto) {
        this.handler.handle(ctx,proto);
    }

    /**
     * 异常消息
     *
     * @param ctx   通道上下文
     * @param cause 线程
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        ctx.close();
        this.handler.exceptionCaught(ctx,cause);
    }
}
