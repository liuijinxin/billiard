package com.wangpo.robot;

import com.wangpo.robot.logic.ClientMgr;
import com.wangpo.robot.net.SocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@SpringBootApplication
@Slf4j
public class RobotApplication {
	@Resource
	ClientMgr clientMgr;
	public static void main(String[] args) {
		SpringApplication.run(RobotApplication.class,args) ;
	}

	@Component
	public class WebSocketRunner implements CommandLineRunner {
		@Override
		public void run(String... strings) {
			try {
				clientMgr.start();
			} catch (Exception e) {
				log.error("startup error!", e);
			}
		}
	}
}
