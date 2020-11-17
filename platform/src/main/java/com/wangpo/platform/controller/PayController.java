package com.wangpo.platform.controller;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.wangpo.base.bean.TaskData;
import com.wangpo.base.enums.task.BilliardTaskType;
import com.wangpo.platform.logic.task.TaskHandler;
import com.wangpo.platform.pay.PayHandler;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wangpo.base.bean.PlatFormProto.Award;
import com.wangpo.base.bean.PlatFormProto.S2C_BuyGoodsEnd;
import com.wangpo.base.bean.PlatFormProto.S2C_PushAward;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause.Entry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.dao.BuyOrder;
import com.wangpo.platform.dao.PlayerGift;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.logic.member.MemberHandler;
import com.wangpo.platform.service.BuyOrderService;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerGiftService;
import com.wangpo.platform.service.PlayerMgr;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PayController {
	
	@Resource
	BuyOrderService buyOrderService;
    @Resource
    BaseExcelMgr baseExcelMgr;
	@Resource
	ItemMgr itemMgr;
    @Resource
    PlayerMgr playerMgr;
	@DubboReference
	private BilliardPushService billiardPushService;
	@Resource
	PlayerGiftService playerGiftService;
	@Resource
    MemberHandler memberHandler;
	@Resource
	TaskHandler taskHandler;
	
	@RequestMapping("/buyCommodity")
	@ResponseBody
	public boolean newPayCallBack(String out_trade_no, String trade_no, Float amount, String sign) {
		log.info("收到支付返回");
		//校验签名
        Map<String, Object> data = new HashMap<>();
        data.put("out_trade_no", out_trade_no);
        data.put("trade_no", trade_no);
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        data.put("amount", decimalFormat.format(amount));
        String signLocal = getNewSign(data, PayHandler.sign);
        log.info("支付订单，{}，订单号，{}金额，{}签名，{}整型，{}", out_trade_no, trade_no,amount,sign,decimalFormat.format(amount));
        if (!signLocal.equals(sign)) {
            log.error("支付回调中签名{}与本地签名{}不一致!返回", sign, signLocal);
            return false;
        }
        BuyOrder buyOrder = buyOrderService.selectBuyOrder(out_trade_no);
        if (buyOrder.getOrderStatus() == 0) {
            //回调状态成功
            log.info("回调成功，开始业务处理！");
            chargeSuccessAction(buyOrder);
        }
        return true;
    }
	
	/**
	 * 
	 * @param buyOrder
	 */
	public void chargeSuccessAction(BuyOrder buyOrder) {
		//修改订单状态
    	buyOrder.setOrderStatus(1);
    	buyOrderService.updateBuyOrder(buyOrder);
    	ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(buyOrder.getGoodsId());
    	//记录限购
    	PlayerGift payerGift = null;
    	if(shopConfig.getGoodsType() == 6) {
    		payerGift = playerGiftService.selectPlayerGiftById(9001,buyOrder.getUserId());
    	}else if(shopConfig.getGoodsType() == 7){
    		payerGift = playerGiftService.selectPlayerGiftById(8003,buyOrder.getUserId());
    	}else {
    		payerGift = playerGiftService.selectPlayerGiftById(shopConfig.getGoodsId(),buyOrder.getUserId());
    	}
        savePlayerGift(shopConfig,payerGift,buyOrder.getUserId());
        //发送商品
    	try {
    		S2C s2c = new S2C();
    		s2c.setCid(Cmd.BUY_GOODS_END);
    		s2c.setUid(buyOrder.getUserId());
    		S2C_BuyGoodsEnd.Builder buyGoodsEndBuilder = S2C_BuyGoodsEnd.newBuilder();
    		buyGoodsEndBuilder.setId(shopConfig.getGoodsId());
    		s2c.setBody(buyGoodsEndBuilder.build().toByteArray());
    		billiardPushService.push(s2c);
		} catch (Exception e) {
			log.error("购买商品发送客户端失败:{}");
			e.printStackTrace();
		}
    	Player player = playerMgr.getPlayerByID(buyOrder.getUserId());
		//桌球服，道具让桌球服处理
    	if(shopConfig.getGoodsType() == 6) {
    		itemMgr.addItem(player,shopConfig.getItemId(),shopConfig.getCount(), GameEventEnum.RECHARGE);
    		S2C_PushAward.Builder pushAwardBuilder = S2C_PushAward.newBuilder();
    		Award.Builder award = Award.newBuilder();
    		award.setId(shopConfig.getItemId());
    		award.setNum(shopConfig.getAddCount()/shopConfig.getCycleTimes());
			pushAwardBuilder.addAwards(award);
			pushAwardBuilder.setGoodsId(shopConfig.getGoodsId());
    		S2C pushAwardS2C = new S2C();
    		pushAwardS2C.setCid(Cmd.PUSH_MONCARD_AWARD);
    		pushAwardS2C.setUid(buyOrder.getUserId());
    		pushAwardS2C.setBody(pushAwardBuilder.build().toByteArray());
    		billiardPushService.push(pushAwardS2C);
    	}else {
    		itemMgr.addItem(player,shopConfig.getItemId(),shopConfig.getCount() + shopConfig.getAddCount(), GameEventEnum.RECHARGE);
    	}
    	//增加附赠商品
    	Map<String, String> jsonMap = (Map<String, String>) JSONObject.toJavaObject(shopConfig.getBonusItem(), Map.class);
    	for(Map.Entry<String,String> entry : jsonMap.entrySet()) {
    		itemMgr.addItem(player,Integer.valueOf(entry.getKey()),Integer.valueOf(entry.getValue()), GameEventEnum.RECHARGE);
    	}
    	//充值增加VIP经验
    	memberHandler.modifyPoint(player,shopConfig.getPrice() / 100);
    	//完成任务
		TaskData taskData = new TaskData();
		taskData.setTimes(shopConfig.getPrice() / 100);
		taskData.setTaskType(BilliardTaskType.RECHARGE.code);
		taskHandler.finishTask(buyOrder.getUserId(), 3, taskData);
	}
	
	/**
	 * 记录商品购买次数，到期时间
	 * @param shopConfig
	 * @param payerGift 
	 * @param userId 
	 */
	private void savePlayerGift(ShopConfig shopConfig, PlayerGift payerGift, int userId) {
		if(payerGift== null) {
			payerGift = new PlayerGift();
			//永久购买限制
			if(shopConfig.getEvenLimit() > 0) {
				payerGift.setPermanentBuy(1);
	        }
			//每天购买
			if(shopConfig.getDayLimit() > 0) {
				payerGift.setEveryDayBuy(1);
			}
			//到期时间
			if(shopConfig.getEffectTimes() > 0) {
				long nowTime = System.currentTimeMillis();
				payerGift.setEndTime(payerGift.getEndTime() + nowTime + 30 * 24 * 60 * 60 * 1000l);
			}
			//房间场次
			if(shopConfig.getRoomLimit() > 0) {
				payerGift.setRoomNum(shopConfig.getRoomLimit());
			}
			payerGift.setGoodsId(shopConfig.getGoodsId());
			payerGift.setPlayerId(userId);
			playerGiftService.insertPlayerGift(payerGift);
			return;
		}
		//永久购买限制
		if(shopConfig.getEvenLimit() > 0) {
			payerGift.setPermanentBuy(payerGift.getPermanentBuy() + 1);
        }
		//每天购买
		if(shopConfig.getDayLimit() > 0) {
			payerGift.setEveryDayBuy(payerGift.getEveryDayBuy() + 1);
		}
		//到期时间
		if(shopConfig.getEffectTimes() > 0) {
			long nowTime = System.currentTimeMillis();
			payerGift.setEndTime(payerGift.getEndTime() + nowTime + 30 * 24 * 60 * 60 * 1000l);
		}
		//房间场次
		if(shopConfig.getRoomLimit() > 0) {
			payerGift.setRoomNum(shopConfig.getRoomLimit());
		}
		payerGift.setGoodsId(shopConfig.getGoodsId());
		payerGift.setPlayerId(userId);
		playerGiftService.updatePlayerGift(payerGift);
	}

	public static String getNewSign(Map<String, Object> data, String key) {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            sb.append(k).append("=").append(data.get(k)).append("&");
        }
        sb.append("secret=").append(key);
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }

}
