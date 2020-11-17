package com.wangpo.robot.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;

import static io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory.newHandshaker;

/**
 * WebSocket服务初始化类
 */
//@Component
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

//    @Resource
    private WebSocketProtoEncoder protoEncoder;
//    @Resource
    private WebSocketProtoDecoder protoDecoder;
//    @Resource
    private ServerHandler serverHandler;
    private int id;
    private String url;

    public WebSocketServerInitializer(int id,String url){
        this.id = id;
        this.url = url;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 编解码 http 请求
        pipeline.addLast(new HttpClientCodec());
        // 写文件内容
        pipeline.addLast(new ChunkedWriteHandler());
        // 聚合解码 HttpRequest/HttpContent/LastHttpContent 到 FullHttpRequest
        // 保证接收的 Http 请求的完整性
        pipeline.addLast(new HttpObjectAggregator(8192));
        // 处理其他的 WebSocketFrame
//        pipeline.addLast(new WebSocketServerProtocolHandler("/"));
        // 处理 TextWebSocketFrame
//        pipeline.addLast(protoCodec);
        pipeline.addLast(new WebSocketProtoDecoder());
        pipeline.addLast(new WebSocketProtoEncoder());

        pipeline.addLast(new ServerHandler(id,WebSocketClientHandshakerFactory.newHandshaker(new URI(url), WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
    }

}
