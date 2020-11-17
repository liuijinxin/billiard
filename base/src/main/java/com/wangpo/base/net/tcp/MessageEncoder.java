package com.wangpo.base.net.tcp;

import com.wangpo.base.net.Proto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.stereotype.Component;

@Component
public class MessageEncoder extends MessageToByteEncoder<Proto> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Proto msg, ByteBuf out) throws Exception {
        //
        out.writeShort(msg.getBody().length+2);
        out.writeShort(msg.getCmd());
        out.writeBytes(msg.getBody());
    }
}
