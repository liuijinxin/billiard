package com.wangpo.net;

import com.wangpo.base.net.ISocketServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * tcp server
 * Created by bovy lau on 7/30/2020
 */
@Component("webSocketServer")
@Slf4j
public class WebSocketServer implements ISocketServer {
	@Value("${server.websocket}")
	private int port;

	@Resource
	private WebSocketServerInitializer serverInitializer;

	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	private final EventLoopGroup workGroup = new NioEventLoopGroup();

	private ChannelFuture channelFuture;

	@Override
	public void start() throws Exception {
		try {
			ServerBootstrap b = new ServerBootstrap()
					.group(bossGroup, workGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(serverInitializer);

			log.error("WS启动成功，端口: " + port);

			channelFuture = b.bind(port).sync();
		} finally {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
		}
	}

	@Override
	public void shutdown()  {
		channelFuture.channel().close();
	}
}
