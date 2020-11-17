package com.wangpo.robot.net;

import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.net.ISocketServer;
import com.wangpo.robot.logic.RobotCmd;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Time;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * tcp server
 * Created by bovy lau on 7/30/2020
 */
@Slf4j
@Data
public class SocketClient {
	public final LinkedBlockingQueue<S2C> queue = new LinkedBlockingQueue<>();

	private final WebSocketServerInitializer serverInitializer ;
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	private Channel channel;
	private int id;
	private int playerId;
//	private static final String url ="ws://192.168.1.145:9090";
	private static final String url ="ws://39.97.171.53:8081"; //成都测试服
	private Task task = new Task();

	public SocketClient(int id) {
		this.id = id;
		serverInitializer = new WebSocketServerInitializer(id,url);
	}

	public void start(int id) throws Exception {
		try {
//			ServerBootstrap b = new ServerBootstrap()
//					.group(bossGroup, workGroup)
//					.channel(NioServerSocketChannel.class)
//					.childHandler(serverInitializer);


			Bootstrap bb = new Bootstrap();
			bb.group(bossGroup).channel(NioSocketChannel.class).handler(serverInitializer);
			channel = bb.connect("39.97.171.53",8081).sync().channel();

//			log.info("机器人[{}]连接成功",id);

			//1登录
			executor.schedule(this::login,5, TimeUnit.SECONDS);
//			executor.scheduleAtFixedRate(this::logic,5,1, TimeUnit.SECONDS);
			new Thread(this.task).start();
//			channelFuture = b.bind(port).sync();
		} catch (Exception e){
			log.error("异常：",e);
		}
	}

	class Task implements Runnable {
		private Random r = new Random();
		@Override
		public void run() {
//			log.info("机器人逻辑线程开始跑了...");
			while( true ) {
				try{
					S2C s2c = queue.take();
					int cmd = s2c.getCid();
//					log.info("收到逻辑处理消息：{}",s2c.getCid());
					if( 100 == cmd ) {
						//登录成功，直接发匹配
						PlatFormProto.S2C_Login b = PlatFormProto.S2C_Login.parseFrom(s2c.getBody());
						playerId = (int)b.getId();
						log.info("{}登录成功，开始发送匹配",playerId);
						Thread.sleep(r.nextInt(10)*1000);
						send(RobotCmd.MATCH,BilliardProto.C2S_Match.newBuilder()
								.setChangId(1)
								.setGameId(1)
								.setMoneyId(1)
								.build().toByteArray());
					} else if( 2005 == cmd) {

						Thread.sleep(2000);
						send(RobotCmd.SYNC_POS,BilliardProto.C2S_SyncPos2.newBuilder()
								.setHitKu(0)
								.setGan(0)
								.build().toByteArray());
						log.info("{}匹配成功后发送同步",playerId);
					} else if ( 2010 == cmd) {
						log.info("{}游戏结算",playerId);
					} else if ( 2002 == cmd) {
//						log.info("当前操作玩家");
						BilliardProto.S2C_OptPlayer b = BilliardProto.S2C_OptPlayer.parseFrom(s2c.getBody());
						if( playerId == b.getId()) {
							//轮到当前玩家击球
//							log.info("{} 击球玩家",playerId);
							Thread.sleep(r.nextInt(5)*1000);
							BilliardProto.C2S_Batting.Builder bb = BilliardProto.C2S_Batting.newBuilder();
							bb.setForce(BilliardProto.Vec3.newBuilder().setX(200).setY(0).setZ(0).build());
							bb.setAngle(100);
							bb.setPowerScale(0.5);
							bb.setPlayerID(playerId);
							bb.setContactPoint(BilliardProto.Vec2.newBuilder().setX(0).setY(0).build());
							bb.setGasserAngle(0);
//							bb.setVelocity(BilliardProto.Vec3.newBuilder().setX(200).setY(0).setZ(0).build());
							send(RobotCmd.PLAYER_OPT,bb.build().toByteArray());
						}
					} else if( 1104 == cmd) {
						//玩家击球，随便发个同步
						Thread.sleep(r.nextInt(5)*1000+5000);
						send(RobotCmd.SYNC_POS,BilliardProto.C2S_SyncPos2.newBuilder()
								.setHitKu(0)
								.setHitFirstBall(1)
								.build().toByteArray());
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public void gotMsg(S2C s2c) {
		queue.offer(s2c);
	}
	public void login(){
//		log.info("5秒后发送登录消息");
		send(RobotCmd.LOGIN,PlatFormProto.C2S_Login.newBuilder()
				.setLoginType(0)
				.setOrigin("test")
				.setCode("PC_"+id)
				.build().toByteArray());
	}

	public void send(int cmd,byte[] body) {
		C2S c2s = new C2S();
		c2s.setSid(5);
		c2s.setCid(cmd);
		if(body!=null) {
			c2s.setBody(body);
		}
		channel.writeAndFlush(c2s);
	}

}
