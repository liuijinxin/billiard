package com.wangpo.billiard.logic.room.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
public class AIMgr {

    //AI执行线程
//    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

//    private static final Random random = new Random();

//    public void submitAI(IGameRoom gameRoom) {
////        log.info("添加ai，房间id:{}",gameRoom.getRoomNo());
//        int t = random.nextInt(3) +1;
//        executor.schedule(gameRoom::ai,t,TimeUnit.SECONDS);
//    }

//    public static void main(String[] args) {
//    }
}
