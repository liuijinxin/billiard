package com.wangpo.base.net;

import io.netty.channel.ChannelHandlerContext;

public interface IJSONHandler {
    void channelActive(ChannelHandlerContext ctx);
    void handle(ChannelHandlerContext ctx, String json);
    void channelInactive(ChannelHandlerContext ctx);
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause);
}
