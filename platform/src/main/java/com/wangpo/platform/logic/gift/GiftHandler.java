package com.wangpo.platform.logic.gift;

import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.excel.GoodsConfig;
import com.wangpo.base.item.Item;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.service.PlayerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 游戏礼包处理
 */

@Component
@Slf4j
public class GiftHandler {
	@Resource
	PlayerMgr playerMgr;
	@Resource
	BaseExcelMgr excelMgr;
	@Resource
	ItemMgr itemMgr;

	/**
	 * 给玩家发放礼包
	 * @param uid 玩家ID
	 * @param id 礼包ID
	 */
	public void addGift(int uid, int id) {
		Player player = playerMgr.getPlayerByID(uid);
		if( player != null ) {
			GoodsConfig goodsConfig = BaseExcelMgr.GOODS_MAP.get(id);
			if( goodsConfig != null ) {
				for(Item item:goodsConfig.getItemList()) {
					itemMgr.addItem(player,item.getModelId(),item.getNum(), GameEventEnum.GIFT_LUCKY_CUE);
				}
			}
		}
	}
}
