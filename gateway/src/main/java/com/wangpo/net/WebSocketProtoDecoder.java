package com.wangpo.net;

import com.wangpo.base.bean.C2S;
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
		C2S c2s = new C2S();
		int len = byteBuf.readInt();
		c2s.setSid(byteBuf.readInt());
		c2s.setCid(byteBuf.readInt());
		c2s.setSequence(byteBuf.readInt());
		if( len > C2S.HEADER_LENGTH) {
			byte[] bytes = new byte[len-C2S.HEADER_LENGTH];
			byteBuf.readBytes(bytes);
			c2s.setBody(bytes);
		}
		list.add(c2s);
//		log.debug("decode: {}", c2s);
	}
}
