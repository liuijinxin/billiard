package com.wangpo.base.net;

import io.netty.channel.ChannelHandlerContext;

public interface IProtoHandler {
    void handle(ChannelHandlerContext ctx, Proto proto);
}
