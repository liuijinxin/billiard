package com.wangpo.billiard.logic.room;

import com.wangpo.base.net.tcp.TcpSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GoRoom {
//    private static final Logger log = LoggerFactory.getLogger(GoRoom.class);
//    private static ScheduledExecutorService tcpExecutor = Executors.newSingleThreadScheduledExecutor();
//
//    public static void main(String[] args) {
//        try {
//            tcpExecutor.execute(() -> {
//                try {
//                    new TcpSocketServer().start();
//                } catch (Exception e) {
//                    log.error("启动tcp server error:",e);
//                }
//            });
//
//            Runtime.getRuntime().addShutdownHook(new Thread(()->{
//                log.error("<<< 进入ShutdownHook，开始关闭服务器 >>>.");
//                closeTcp();
//                log.error("<<< 进入ShutdownHook，服务器关闭成功 >>>.");
//            }));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 关闭websocket服务器
//     */
//    public static void closeTcp() {
//        tcpExecutor.shutdown();
//        try {
//            if( !tcpExecutor.awaitTermination(3, TimeUnit.SECONDS) ) {
//                log.error("关闭超时 tcpExecutor，开始强制关闭");
//                tcpExecutor.shutdownNow();
//            } else {
//                log.error("tcpExecutor 关闭成功。");
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
