package com.wangpo.base.net.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WebSocketProtoCodec extends MessageToMessageCodec<WebSocketFrame, String> {

    private Logger logger = LoggerFactory.getLogger(WebSocketProtoCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, String proto, List<Object> list) throws Exception {
        /*ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        if (proto.getBody() != null) {
            byteBuf.writeInt(Proto.HEADER_LENGTH + proto.getBody().length);
            byteBuf.writeShort(Proto.HEADER_LENGTH);
            byteBuf.writeShort(Proto.VERSION);
            byteBuf.writeInt(proto.getOperation());
            byteBuf.writeInt(proto.getSeqId());
            byteBuf.writeBytes(proto.getBody());
        } else {
            byteBuf.writeInt(Proto.HEADER_LENGTH);
            byteBuf.writeShort(Proto.HEADER_LENGTH);
            byteBuf.writeShort(Proto.VERSION);
            byteBuf.writeInt(proto.getOperation());
            byteBuf.writeInt(proto.getSeqId());
        }

        list.add(new BinaryWebSocketFrame(byteBuf));
        */
        list.add(new TextWebSocketFrame(proto));

        logger.debug("encode: {}", proto);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> list) throws Exception {
        if( webSocketFrame instanceof TextWebSocketFrame) {
            TextWebSocketFrame txt =  (TextWebSocketFrame)webSocketFrame;
            list.add(txt.text());
        }

    }
}
