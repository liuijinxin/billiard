package com.wangpo;

import com.wangpo.net.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@EnableDubbo
@SpringBootApplication
@Slf4j
public class GatewayApplication {
    @Resource(name = "webSocketServer")
    private WebSocketServer webSocketServer;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class,args) ;
    }

    @Component
    public class WebSocketRunner implements CommandLineRunner {
        @Override
        public void run(String... strings) {
            try {
                webSocketServer.start();
                Thread.currentThread().join();
            } catch (Exception e) {
                log.error("startup error!", e);
            }
        }
    }

}
