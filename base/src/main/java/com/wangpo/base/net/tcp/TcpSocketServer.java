package com.wangpo.base.net.tcp;

import com.wangpo.base.net.ISocketServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * tcp server
 * Created by bovy on 7/30/2020
 */
@Component("tcpSocketServer")
@Slf4j
public class TcpSocketServer implements ISocketServer {
    @Value("${server.tcpSocket.port:9092}")
    private int port;
    @Resource
    private TcpServerInitializer serverInitializer;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    private ChannelFuture channelFuture;


    public TcpSocketServer() {
    }

    @Override
    public void start() throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(serverInitializer);

            log.info("Starting TcpChatServer at Port:{}", port);

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

    @Override
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
