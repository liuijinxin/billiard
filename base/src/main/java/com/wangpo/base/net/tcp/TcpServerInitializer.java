package com.wangpo.base.net.tcp;

import com.wangpo.base.net.IProtoHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * TCP服务初始化类
 */
@Component
public class TcpServerInitializer extends ChannelInitializer<NioSocketChannel> {
	@Resource
	TCPServerHandler handler;
	@Resource
	MessageDecoder decoder;
	@Resource
	MessageEncoder encoder;

	public TcpServerInitializer() {

	}

	@Override
	protected void initChannel(NioSocketChannel ch) {
		ch.pipeline().addLast(decoder);
		ch.pipeline().addLast(encoder);
		ch.pipeline().addLast(handler);
	}

}
