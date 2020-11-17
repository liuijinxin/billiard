package com.wangpo.base.net.tcp;

import com.wangpo.base.net.Proto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * TCP协议加解密
 */
public class TcpProtoCodec extends MessageToMessageCodec<ByteBuf, Proto> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Proto proto, List<Object> list) throws Exception {
        list.add(proto);
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        int len = 2;
        if (proto.getBody() != null) {
            len += proto.getBody().length;
        }
//        System.out.println("encode len:"+len+",cmd:"+proto.getCmd());

//        if(proto.getCmd() == HostCmd.L2M_MATCH) {
//            System.out.println("encode data len:"+len);
//        }

        byteBuf.writeShort(len);
        if (proto.getBody() != null) {
            byteBuf.writeShort(proto.getCmd());
            byteBuf.writeBytes(proto.getBody());
        } else {
            byteBuf.writeShort(proto.getCmd());
        }
        list.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List<Object> list) throws Exception {
       /*
        if ( buffer.readableBytes()>=4) {
            // 记录包头开始的index
            int beginReader = buffer.readerIndex();
            // 消息的长度
            int length = buffer.readShort();
            // 判断请求数据包数据是否到齐
            if (buffer.readableBytes() < length+2) {
                // 还原读指针
                buffer.readerIndex(beginReader);
                return;
            }

            // 读取data数据
            byte[] data = new byte[length];
            buffer.readBytes(data);

            Proto proto = new Proto();
            proto.setBody(data);
            list.add(proto);
        }
    }*/

        if (buffer.readableBytes() >= 4) {
            buffer.markReaderIndex();
//            System.out.println();
            Proto proto = new Proto();
            short len = buffer.readShort();
            proto.setCmd(buffer.readShort());
//            if(proto.getCmd() == HostCmd.L2M_MATCH) {
//                System.out.println("decode data len:"+len+",readableBytes :"+buffer.readableBytes());
//            }
            if (len > 2) {
                if( buffer.readableBytes() < len-2 ) {
                    // 还原读指针
//                    System.out.println("decode len:"+len+",cmd:"+proto.getCmd()+",readable:"+buffer.readableBytes());
                    buffer.resetReaderIndex();
                    return;
                }
                byte[] bytes = new byte[len - 2];
                buffer.readBytes(bytes);
                proto.setBody(bytes);
            }
            list.add(proto);
        }
    }
}
