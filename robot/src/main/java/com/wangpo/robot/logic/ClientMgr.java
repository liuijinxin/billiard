package com.wangpo.robot.logic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wangpo.base.bean.S2C;
import com.wangpo.robot.net.SocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class ClientMgr {

	public static final Map<Integer, SocketClient> map = new ConcurrentHashMap();

	public static ExecutorService create(String threadName, int corePoolSize, int maximumPoolSize){
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat(threadName).build();

		//Common Thread Pool
		ExecutorService pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

		return pool;
	}

	public void start() throws InterruptedException {
		ExecutorService executorService = create("test",20,100);
		for(int k=1;k<150;k++) {
			final int t = k;
			executorService.execute(()->{
				for(int i=1;i<=20;i++) {
					log.info("玩家登录："+t+"_"+i);
					startNew(t*20+i);
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});

		}

		log.info("所有玩家创建结束。");
	}

	private void startNew(int id) {
		try {
			SocketClient sc = new SocketClient(id);
			sc.start(id);
			map.put(id,sc);
		} catch (Exception e) {
			log.error("异常：",e);
		}
	}
}
