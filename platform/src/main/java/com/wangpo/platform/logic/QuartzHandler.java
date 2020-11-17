package com.wangpo.platform.logic;

import com.wangpo.base.enums.task.TaskType;
import com.wangpo.base.kits.FormatKit;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.bean.StockLog;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.member.MemberHandler;
import com.wangpo.platform.logic.task.TaskHandler;
import com.wangpo.platform.pay.PayHandler;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.StockLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Slf4j
public class QuartzHandler {

	@Resource
	BaseExcelMgr excelMgr;

	@Resource
	TaskHandler taskHandler;

	@Resource
	PlayerMgr playerMgr;

	@Resource
	MemberHandler memberHandler;
	
	@Resource
	PayHandler payHandler;

	@Resource
	StockLogService stockLogService;

	@DubboReference
	BilliardPushService pushService;

	@Scheduled(cron = "0 * * * * ?")
	public void reloadExcel(){
//		log.info("开始热更新配置文件..");
		excelMgr.hotReload();
	}

	/**
	 * 每天凌晨4点调度
	 */
	//@Scheduled(cron = "0 0/2 * * * ?")
	@Scheduled(cron = "0 0 4 * * ?")
	public void cronScheduledDay(){
		log.info("每日重置任务！！！");
		resetTask(1);
		memberHandler.declineVip();
	}

	/**
	 * 每日记录库存，每天3点59分记录总库存
	 */
	@Scheduled(cron = "0 59 23 * * ?")
	public void cronScheduledStock(){
		String day = FormatKit.today10();

		log.info("每日记录库存。");
		StockLog stockLog1 = stockLogService.sumStock( day );
//		log.info("stockLog1:{}",stockLog1);
		StockLog stockLog2 = stockLogService.sumActive( day );
//		log.info("stockLog2:{}",stockLog2);

		StockLog stockLog = new StockLog();
		stockLog.setLogDay(day);
		stockLog.setStockGold(stockLog1.getStockGold());
		stockLog.setStockDiamond(stockLog1.getStockDiamond());
		stockLog.setStockRedPacket(stockLog1.getStockRedPacket());

		stockLog.setActiveGold(stockLog2.getActiveGold());
		stockLog.setActiveDiamond(stockLog2.getActiveDiamond());
		stockLog.setActiveRedPacket(stockLog2.getActiveRedPacket());
		stockLogService.insertStockLog(stockLog);
	}

	/**
	 * 每周一凌晨4点调度
	 */
	@Scheduled(cron = "0 0 4 * * MON")
	public void cronScheduledWeek(){
		log.info("每周重置任务！！！");
		resetTask(3);
	}


	@Scheduled(cron = "0 */10 * * * ?")
	public void cronOffline(){
		for(Player player: playerMgr.allPlayer().values()) {
			//还未有心跳
			if( player.getHeartTime()<=0) {
				continue;
			}
			//已经离线
			if( !player.isOnline()) {
				continue;
			}
			long t = System.currentTimeMillis() - player.getHeartTime();
			if( t > 5*60*1000 ) {
				//超过5分钟没有心跳则判断为死链接，直接删除。
				pushService.close(player.getId());
//				log.error("玩家：{} 5分钟未有心跳，断开链接。",player.getId());
			}
		}
	}


	/**
	 * 重置缓存中的玩家任务
	 * @param taskType 任务类型
	 */
	private void resetTask(int taskType) {
		//重置缓存里的任务
		Map<Integer, Player> playerMap = playerMgr.getIdMap();
		if (playerMap != null) {
			for (Player player : playerMap.values()) {
				//重置任务进度
				taskHandler.resetPlayerTask(player,taskType);
				payHandler.resetGift(player, TaskType.DAY.code);
				//重置签到信息
				player.setSignStatus(0);
			}
		}
	}
}
