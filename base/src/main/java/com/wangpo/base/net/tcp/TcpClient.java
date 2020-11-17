package com.wangpo.base.net.tcp;

import com.wangpo.base.net.HostCmd;
import com.wangpo.base.net.IProtoHandler;
import com.wangpo.base.net.Proto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@Data
public class TcpClient {
//    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);
    private String ip;
    private int port;
    private final AtomicInteger status = new AtomicInteger(0);
    private ChannelFuture future;
    @Resource
    private IProtoHandler handler;
    //启动成功标志
    private CountDownLatch cdl;
    //客户端链接地址，用于区分来自哪台服务器
    private String link;

    public TcpClient( ) {
//        this.ip = ip;
//        this.port = port;
//        this.link = link;
    }

    public void start( ) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        Bootstrap bs = new Bootstrap();
        bs.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
//                        socketChannel.pipeline().addLast(new TcpProtoCodec());
                        socketChannel.pipeline().addLast(new MessageDecoder());
                        socketChannel.pipeline().addLast(new MessageEncoder());
                        socketChannel.pipeline().addLast(new TCPServerHandler());
                    }
                });

            try{
                // 客户端开启
                this.future = bs.connect(ip, port).sync();
                // 等待直到连接中断
                this.status.set(1);
                this.afterConnected();
                cdl.countDown();
                this.future.channel().closeFuture().sync();
            } catch (Exception e) {
                this.status.set(-1);
                cdl.countDown();
            } finally {
                bossGroup.shutdownGracefully();

                try {
                    log.info("tcpClient 断线，每隔3秒重连一次，直到连接成功");
                    TimeUnit.SECONDS.sleep(3);
                    start(); // 断线重连
                } catch (InterruptedException e) {
                }
            }
    }

    /**
     * 链接建立后，发送link到对应的服务器
     */
    public void afterConnected() {
//        Proto proto = new Proto();
//        proto.setCmd(HostCmd.LINK);
//        proto.setBody(GameProto.HostUrl.newBuilder().setHostUrl(this.link).build().toByteArray());
//        send(proto);
    }

    /**
     * 关闭客户端链接，一般在停服的时候调用
     */
    public void close(){
        this.future.channel().close();
    }

    public void setCdl(CountDownLatch cdl) {
        this.cdl = cdl;
    }
    public AtomicInteger getStatus(){
        return this.status;
    }

    public void send(Proto proto) {
        this.future.channel().writeAndFlush(proto);
    }

}
