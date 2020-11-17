package com.wangpo.net;

import com.wangpo.base.net.IProtoHandler;
import com.wangpo.base.net.Proto;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class TestHandler implements IProtoHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Proto proto) {

	}
}
