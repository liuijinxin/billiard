package com.wangpo.platform.charge;

import com.wangpo.base.service.BilliardService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.member.MemberHandler;
import com.wangpo.platform.service.PlayerMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ChargeHandler {
	@Resource
	private BaseExcelMgr baseExcelMgr;
	@Resource
	PlatformService platformService;
	@Resource
	PlayerMgr playerMgr;
	@DubboReference
	BilliardService billiardService;
	@Resource
    MemberHandler memberHandler;

	/**
	 * 充值成功回调
	 * @param uid   用户id
	 * @param chargeNum 充值数量
	 */
	public void afterCharge(int uid, int chargeNum) {
		Player player = playerMgr.getPlayerByID(uid);
		if(player!=null) {
			//1，添加玩家钻石
			int num = chargeNum*10;//1比10的比例
			platformService.modifyDiamond(uid, num, GameEventEnum.CHARGE.reason);
			//2，同步玩家钻石和金币信息到各个游戏服
			billiardService.syncUser(player.toCommonUser());
			memberHandler.modifyPoint(player,num);
		}
	}
}
