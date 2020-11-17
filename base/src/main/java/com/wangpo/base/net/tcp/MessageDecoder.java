package com.wangpo.base.net.tcp;

import com.wangpo.base.net.Proto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author bovy
 */
@Component
public class MessageDecoder extends LengthFieldBasedFrameDecoder {
    private Logger logger = LoggerFactory.getLogger(getClass());

    //头部信息的大小应该是 2
    private static final int HEADER_SIZE = 2;

    public MessageDecoder() {
        super(1<<10,0,2);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null) {
            return null;
        }

        if (in.readableBytes() <= HEADER_SIZE) {
            return null;
        }

        in.markReaderIndex();

        int dataLength = in.readShort();

        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return null;
        }

        short cmd = in.readShort();
        byte[] data = new byte[dataLength-2];
        in.readBytes(data);
        Proto msg = new Proto( );
        msg.setCmd(cmd);
        msg.setBody(data);
        return msg;
    }
}
