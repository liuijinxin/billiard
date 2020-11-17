package com.wangpo.base.net.websocket;

import com.wangpo.base.net.IJSONHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServer {

    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private int port;
    private WebSocketServerInitializer serverInitializer ;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    private ChannelFuture channelFuture;

    public WebSocketServer(IJSONHandler handler, int port) {
        this.port = port;
        this.serverInitializer =  new WebSocketServerInitializer(handler);
    }

    public void start() throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(serverInitializer);
            logger.info("WebSocket启动成功，端口:{} ", port);
            channelFuture = b.bind(port).sync();
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    shutdown();
                }
            });
        }
    }

    public void restart() throws Exception {
        shutdown();
        start();
    }

    public void shutdown() {
        if (channelFuture != null) {
            channelFuture.channel().close().syncUninterruptibly();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
    }
}
