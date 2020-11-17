package com.wangpo.base.net.websocket;

import com.wangpo.base.net.IJSONHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServerInitializer extends ChannelInitializer<NioSocketChannel> {
    IJSONHandler handler;
    WSServerHandler wsServerHandler;

    public WebSocketServerInitializer(IJSONHandler handler){
        this.handler = handler;
        //防止重复创建
        this.wsServerHandler = new WSServerHandler(handler);
    }
    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 编解码 http 请求
        pipeline.addLast(new HttpServerCodec());
        // 写文件内容
        pipeline.addLast(new ChunkedWriteHandler());
        // 聚合解码 HttpRequest/HttpContent/LastHttpContent 到 FullHttpRequest
        // 保证接收的 Http 请求的完整性
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        // 处理其他的 WebSocketFrame
        pipeline.addLast(new WebSocketServerProtocolHandler("/"));
        // 处理 TextWebSocketFrame
        pipeline.addLast(new WebSocketProtoCodec());
        pipeline.addLast(this.wsServerHandler);
    }

}
