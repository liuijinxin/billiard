//package com.wangpo.billiard.logic;
//
//import com.wangpo.base.net.Proto;
//import com.wangpo.base.net.tcp.TcpClient;
//import com.wangpo.base.net.websocket.HostUrl;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * Socket Client 客户端管理类
// */
//@Component
//@Slf4j
//public class ClientMgr {
//
//    private static final ScheduledExecutorService roomClientExecutor = Executors.newSingleThreadScheduledExecutor();
//
//    private ClientMgr() {
//    }
//
//
////    @Resource
////    public TcpClient roomClient;
//
//    public void close() {
////        roomClient.close();
//        roomClientExecutor.shutdown();
//        try {
//            if (!roomClientExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
//                log.error("roomClientExecutor 关闭失败，强制关闭.");
//                roomClientExecutor.shutdownNow();
//            } else {
//                log.error("roomClientExecutor 关闭成功.");
//            }
//
//        } catch (Exception e) {
//
//        }
//    }
//
//    public void init() {
//        System.out.println("ThreadName:" + Thread.currentThread().getName());
//        CountDownLatch cdl = new CountDownLatch(1);
////        roomClient = new TcpClient(new RoomClientHandler(), "127.0.0.1", 10001, HostUrl.LOGIN_PREFIX);
////        roomClient.setCdl(cdl);
//        roomClientExecutor.execute(() -> roomClient.start());
//
//        try {
//            cdl.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (roomClient.getStatus().get() == 1) {
//            log.info("roomClient connect ok,port:{}", 10001);
//        } else {
//            log.error("roomClient connect failed,port:{}", 9001);
//            System.exit(-1);
//        }
//
//    }
//
//    public void sendRoom(Proto proto) {
//        this.roomClient.send(proto);
//    }
//}
//
