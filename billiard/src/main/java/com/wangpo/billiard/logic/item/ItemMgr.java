package com.wangpo.billiard.logic.item;

import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.ItemEnum;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.item.Item;
import com.wangpo.base.service.PlatformService;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.cue.CueMgr;
import com.wangpo.billiard.logic.lucky.LuckyCueHandler;
import com.wangpo.billiard.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class ItemMgr {
	@Resource
	PlayerService playerService;
	@Resource
	BilliardPushService billiardPushService;
	@Resource
	LuckyCueHandler luckyCueHandler;
	@DubboReference
	PlatformService platformService;
	@Resource
	CueMgr cueMgr;

	/**
	 * 发送奖励接口
	 * @param player 玩家
	 * @param award 奖励
	 * @param eventEnum 事件
	 */
	public void sendAward(Player player,List<Item> award,GameEventEnum eventEnum) {
		BilliardProto.S2C_BilliardAward.Builder b = BilliardProto.S2C_BilliardAward.newBuilder();
		for(Item item:award) {
			b.addItems(BilliardProto.Item.newBuilder().setId(item.getId()).setNum(item.getNum()));
		}
		S2C s2c = new S2C();
		s2c.setCid(Cmd.S2C_AWARD);
		s2c.setUid(player.getId());
		s2c.setBody(b.build().toByteArray());
		billiardPushService.push(s2c);
	}

	/**
	 * 添加道具
	 * @param player 玩家id
	 * @param modelId 道具id
	 * @param num 数量
	 * @param gameEventEnum 事件
	 */
	public void addItem(Player player, int modelId, int num, GameEventEnum gameEventEnum){
		//幸运一杆特殊处理
		if( modelId == 3002) {
			player.getLuckyCue().setVipTimes(player.getLuckyCue().getVipTimes()+num);
			luckyCueHandler.pushLuckyCue(player);
			return;
		}
		//球杆特殊处理
		if (modelId == ItemEnum.YONGSHI_CUE.code
				|| modelId == ItemEnum.SHUIJING_CUE.code
				|| modelId == ItemEnum.HUANGJIN_CUE.code
				|| modelId == ItemEnum.ZUANSHI_CUE.code
				|| modelId == ItemEnum.XINGYAO_CUE.code
				|| modelId == ItemEnum.WANGHZHE_CUE.code
				|| modelId == ItemEnum.XIYOU_CUE.code
				|| modelId == ItemEnum.BISHENG_CUE.code
				|| modelId == ItemEnum.TRUMP_CUE.code
				|| modelId == ItemEnum.GOLDEN_CUE.code
				|| modelId == ItemEnum.MAHOGANY_CUE.code
				|| modelId == ItemEnum.MASTER_CUE.code) {
			int cueId = modelId-3000;
			cueMgr.addCue(player,cueId,num);
			return;
		}

		if( modelId <1000) {
			switch (modelId) {
				case 1:
					platformService.modifyGold(player.getId(),num, gameEventEnum.reason);
					break;
				case 2:
					platformService.modifyDiamond(player.getId(),num, gameEventEnum.reason);
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
					platformService.modifyRedPacket(player.getId(),num,gameEventEnum.reason);
					break;
				default:break;
			}
			return;
		}
		List<Item> list = player.getItemList();
		boolean exist = false;
		for(Item item:list) {
			if( item.getModelId() == modelId ) {
				exist = true;
				num = item.getNum()+ num;
				item.setNum(num);
				break;
			}
		}
		if( !exist) {
			Item item = new Item();
			item.setModelId(modelId);
			item.setNum(num);
			list.add(item);
		}
		updateItemPush(player, modelId, num);
		playerService.updatePlayer(player);
	}

	/**
	 * 使用道具
	 * @param player 玩家
	 * @param modelId 道具id
	 * @param num 数量
	 * @return 是否使用成功
	 */
	public boolean use(Player player, int modelId,int num){
		List<Item> list = player.getItemList();
		boolean enough = false;
		for(Item item:list) {
			if( item.getModelId() == modelId && item.getNum()>=num ) {
				num = item.getNum()-num;
				item.setNum(num);
				enough = true;
				break;
			}
		}
		if (enough) {
			updateItemPush(player, modelId, num);
			playerService.updatePlayer(player);
		}
		return enough;
	}

	/**
	 * 通知客户端更新道具
	 * @param player 玩家
	 * @param modelId 道具id
	 * @param num 数量
	 */
	private void updateItemPush(Player player, int modelId, int num) {
		BilliardProto.S2C_updateItem.Builder builder = BilliardProto.S2C_updateItem.newBuilder();
		BilliardProto.Item.Builder itemProto = BilliardProto.Item.newBuilder();
		itemProto.setId(modelId);
		itemProto.setNum(num);
		builder.setItem(itemProto.build());

		S2C s2c = new S2C();
		s2c.setCid(Cmd.UPDATE_ITEM);
		s2c.setUid(player.getId());
		s2c.setBody(builder.build().toByteArray());
		billiardPushService.push(s2c);
	}

	/**
	 * 请求所有道具
	 * @param player 玩家
	 * @param s2c s2c
	 * @return s2c
	 */
	public S2C getAllItem(Player player,S2C s2c){
		List<Item> itemList = player.getItemList();
		BilliardProto.S2C_AllItem.Builder builder = BilliardProto.S2C_AllItem.newBuilder();
		for (Item item : itemList) {
			BilliardProto.Item.Builder itemProto = BilliardProto.Item.newBuilder();
			itemProto.setId(item.getModelId());
			itemProto.setNum(item.getNum());
			builder.addItem(itemProto.build());
		}
		s2c.setBody(builder.build().toByteArray());
		return s2c;
	}

}
