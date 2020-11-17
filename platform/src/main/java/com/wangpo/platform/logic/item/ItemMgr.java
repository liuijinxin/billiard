package com.wangpo.platform.logic.item;

import com.wangpo.base.service.BilliardService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.item.Item;
import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.service.GameLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ItemMgr {
	@Resource
	PlatformService platformService;
	@DubboReference
	BilliardService billiardService;

	@Resource
	GameLogService  gameLogService;
	/**
	 * 添加道具
	 * @param player    玩家
	 * @param modelId   道具配置id
	 * @param num   数量
	 */
	public void addItem(Player player,int modelId,int num, GameEventEnum eventEnum){
		if( modelId <1000) {
			switch (modelId) {
				case 1:
					platformService.modifyGold(player.getId(),num, eventEnum.reason);
					break;
				case 2:
					platformService.modifyDiamond(player.getId(),num, eventEnum.reason);
					break;
//				case 3:
//					//日活跃
//				case 4:
//					//周活跃
//				case 5:
//					//角色经验
//				case 6:
//					//vip经验
				case 7:
					platformService.modifyRedPacket(player.getId(),num,eventEnum.reason);
					break;
				default:
					break;
			}
			return;
		} else {
			//通知游戏服更新道具
			billiardService.syncItem(player.getId(),modelId,num,eventEnum);
		}

		GameLog gameLog = new GameLog(1,player.getId(),modelId,num,eventEnum.reason);
		gameLogService.insertGameLog(gameLog);
	}

	/**
	 * 使用道具
	 * @param player 玩家
	 * @param modelId 道具id
	 * @param num 数量
	 * @return 是否使用成功
	 */
	public boolean use(Player player, int modelId, int num){
		if(modelId<1000) {
			log.error("小于1000的道具ID，不能直接使用，道具ID：{}，道具数量：{}，玩家ID：{}",modelId,num,player.getId());
			return false;
		}
		/*List<Item> list = player.getItemList();
		boolean enough = false;
		for(Item item:list) {
			if( item.getModelId() == modelId && item.getNum()>=num ) {
				item.setNum(item.getNum()-num);
				enough = true;
				break;
			}
		}*/
		//TODO 道具持久化
		return false;
	}
}
