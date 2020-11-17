package com.wangpo.robot.net;

import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ChannelHandler.Sharable
@Slf4j
public class WebSocketProtoDecoder extends MessageToMessageDecoder<WebSocketFrame> {


	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame, List<Object> list) {
		ByteBuf byteBuf = frame.content();
		S2C s2c = new S2C();
		int len = byteBuf.readInt();
		s2c.setSid(byteBuf.readInt());
		s2c.setCid(byteBuf.readInt());
		s2c.setSequence(byteBuf.readInt());
		s2c.setCode(byteBuf.readInt());
		if( len > S2C.HEADER_LENGTH) {
			byte[] bytes = new byte[len-S2C.HEADER_LENGTH];
			byteBuf.readBytes(bytes);
			s2c.setBody(bytes);
		}
		list.add(s2c);
	}
}
