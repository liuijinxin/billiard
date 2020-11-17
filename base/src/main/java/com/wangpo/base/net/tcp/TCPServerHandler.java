package com.wangpo.base.net.tcp;

import com.wangpo.base.net.IProtoHandler;
import com.wangpo.base.net.Proto;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@ChannelHandler.Sharable
@Service()
@Scope("prototype")
@Slf4j
public class TCPServerHandler extends SimpleChannelInboundHandler<Proto> {
    @Resource
    private IProtoHandler handler;

    public TCPServerHandler( ) {
    }
    /**
     * 客户端连接
     *
     * @param ctx 上下文
     * @throws Exception 异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 添加
//        log.info("客户端与服务端连接开启:{}",ctx.channel().remoteAddress());
    }

    /**
     * 客户端关闭
     *
     * @param ctx 上下文
     * @throws Exception 异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除
        ctx.close();
//        log.info("客户端与服务端连接关闭");
    }

    /**
     * 读取消息
     *
     * @param ctx   通道上下文
     * @param proto 协议
     * @throws Exception 异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Proto proto) throws Exception {
        this.handler.handle(ctx,proto);
    }

    /**
     * 异常消息
     *
     * @param ctx   通道上下文
     * @param cause 线程
     * @throws Exception 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常消息", cause);
        ctx.close();
    }
}
