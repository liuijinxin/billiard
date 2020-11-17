package com.wangpo.billiard.logic.match;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private static AtomicInteger id = new AtomicInteger(100001);

    public static synchronized int generateID() {
        int roomNo = id.getAndIncrement();
        if( roomNo > 999999) {
            id.set(100001);
            roomNo = id.getAndIncrement();
        }
        return roomNo;
    }
}
