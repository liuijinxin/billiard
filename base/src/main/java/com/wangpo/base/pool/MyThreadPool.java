package com.wangpo.base.pool;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author Administrator
 */
public class MyThreadPool {
	/**
	 * 根据名称创建线程池
	 * @param threadName
	 * @return
	 */
	public static ExecutorService create(String threadName,int corePoolSize,int maximumPoolSize){
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat(threadName).build();

		//Common Thread Pool
		ExecutorService pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

		return pool;
	}

	/**
	 * 自定义调度线程
	 * @param threadName
	 * @param corePoolSize
	 * @return
	 */
	public static ScheduledExecutorService createScheduled(String threadName,int corePoolSize){
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat(threadName).build();
		return new ScheduledThreadPoolExecutor(corePoolSize,namedThreadFactory);
	}
}
