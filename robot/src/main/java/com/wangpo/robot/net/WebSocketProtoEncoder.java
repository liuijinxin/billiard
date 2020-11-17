package com.wangpo.robot.net;

import com.wangpo.base.bean.C2S;
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
public class WebSocketProtoEncoder extends MessageToMessageEncoder<C2S> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, C2S c2s, List<Object> list) throws Exception {
		ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
		if (c2s.getBody() != null) {
			byteBuf.writeInt(C2S.HEADER_LENGTH + c2s.getBody().length);
			byteBuf.writeInt(c2s.getSid());
			byteBuf.writeInt(c2s.getCid());
			byteBuf.writeInt(c2s.getSequence());
			byteBuf.writeBytes(c2s.getBody());
		} else {
			byteBuf.writeInt(C2S.HEADER_LENGTH);
			byteBuf.writeInt(c2s.getSid());
			byteBuf.writeInt(c2s.getCid());
			byteBuf.writeInt(c2s.getSequence());
		}

		list.add(new BinaryWebSocketFrame(byteBuf));
	}
}
