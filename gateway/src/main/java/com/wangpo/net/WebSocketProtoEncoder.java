package com.wangpo.net;

import com.wangpo.base.bean.S2C;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ChannelHandler.Sharable
@Slf4j
public class WebSocketProtoEncoder extends MessageToMessageEncoder<S2C> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, S2C s2c, List<Object> list) throws Exception {
		ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
		if (s2c.getBody() != null) {
			byteBuf.writeInt(S2C.HEADER_LENGTH + s2c.getBody().length);
			byteBuf.writeInt(s2c.getSid());
			byteBuf.writeInt(s2c.getCid());
			byteBuf.writeInt(s2c.getSequence());
			byteBuf.writeInt(s2c.getCode());
			byteBuf.writeBytes(s2c.getBody());
		} else {
			byteBuf.writeInt(S2C.HEADER_LENGTH);
			byteBuf.writeInt(s2c.getSid());
			byteBuf.writeInt(s2c.getCid());
			byteBuf.writeInt(s2c.getSequence());
			byteBuf.writeInt(s2c.getCode());
		}

		list.add(new BinaryWebSocketFrame(byteBuf));
//		log.debug("encode: {}", s2c);
	}
}
