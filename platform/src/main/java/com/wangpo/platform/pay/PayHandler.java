package com.wangpo.platform.pay;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto.Award;
import com.wangpo.base.bean.PlatFormProto.C2S_BuyGoods;
import com.wangpo.base.bean.PlatFormProto.C2S_MonCardGiftBag;
import com.wangpo.base.bean.PlatFormProto.C2S_ResurrectionGiftBag;
import com.wangpo.base.bean.PlatFormProto.C2S_ResurrectionNum;
import com.wangpo.base.bean.PlatFormProto.PayInfo;
import com.wangpo.base.bean.PlatFormProto.S2C_BuyGoods;
import com.wangpo.base.bean.PlatFormProto.S2C_BuyGoodsEnd;
import com.wangpo.base.bean.PlatFormProto.S2C_MonCardGiftBag;
import com.wangpo.base.bean.PlatFormProto.S2C_PayInfo;
import com.wangpo.base.bean.PlatFormProto.S2C_ResurrectionNum;
import com.wangpo.base.excel.ActivityConfig;
import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.controller.PayController;
import com.wangpo.platform.dao.BuyOrder;
import com.wangpo.platform.dao.PlayerGift;
import com.wangpo.platform.enums.ErrorTipsEnum;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.logic.member.MemberHandler;
import com.wangpo.platform.logic.task.TaskHandler;
import com.wangpo.platform.service.BuyOrderService;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerGiftService;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.util.HTTPUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PayHandler {
	
	public static int appId = 10005;
	public static String sign = "4b0d5210fadfb1d64c1a8bf84e43fdc4";
	@Resource
	BuyOrderService buyOrderService;
    @Resource
    BaseExcelMgr baseExcelMgr;
	@Resource
	PlayerGiftService playerGiftService;
	@Resource
	ItemMgr itemMgr;
    @Resource
    PlayerMgr playerMgr;
	@Resource
	PlatformService platformService;
	@Resource
	PayController payController;
    

	@DubboReference
	private BilliardPushService billiardPushService;
	@Resource
    MemberHandler memberHandler;
	@Resource
	TaskHandler taskHandler;
	public S2C getPayOptions(C2S c2s) {
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.PAY_INFO);
        s2c.setUid(uid);
        Map<String, Object> data = new HashMap<>();
        data.put("app_id", appId);
        data.put("sign", HTTPUtil.getNewSign(data,sign));
        String resStr = HTTPUtil.wxHttpPostRequest("http://kyspay.uybeliq.com/api/pay/index/appPay",
        		HTTPUtil.buildReqString(data));
        JSONObject response = JSONObject.parseObject(resStr);
        JSONArray array = response.getJSONArray("data");
        S2C_PayInfo.Builder s2cPayInfo = S2C_PayInfo.newBuilder();
        for (int i = 0; i < array.size(); i++) {
            JSONObject temp = array.getJSONObject(i);
            PayInfo.Builder payBuilder = PayInfo.newBuilder();
            payBuilder.setPayType(temp.getIntValue("pay_type"));
            payBuilder.setPreferred(temp.getBooleanValue("preferred"));
            payBuilder.setMethod(temp.getIntValue("method"));
            s2cPayInfo.addPayInfo(payBuilder);
        }
        s2c.setBody(s2cPayInfo.build().toByteArray());
        return s2c;
    }
	
	

	public S2C newPrePayOrder(C2S c2s) throws Exception {
        C2S_BuyGoods c2sProto = C2S_BuyGoods.parseFrom(c2s.getBody());
        int userId = c2s.getUid();
        int goodsId = c2sProto.getId();
        int payType = c2sProto.getPayType();
        log.info("玩家{}微信支付下单开始！"+userId);
        S2C s2c = new S2C();
        s2c.setCid(Cmd.BUY_SHOP);
        s2c.setUid(userId);
        // 获取商品信息
        ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(goodsId);
        if(shopConfig == null) {
        	log.info("根据配置ID{}从hopConfig中获取到的配置为null!！"+goodsId);
        	s2c.setCode(3);
        	s2c.setErrStr("商品不存在："+goodsId);
        	return s2c;
        }
        //检测商品是否为活动商品
        if(checkActivity(goodsId) || shopConfig.getStatus() == 0) {
        	log.error("商品已下架：{}" , goodsId);
        	s2c.setCode(ErrorTipsEnum.TIPS_INFO1016.getTipsType());
        	return s2c;
        }
        //检测商品购买限制
        PlayerGift payerGift = playerGiftService.selectPlayerGiftById(shopConfig.getGoodsId(),userId);
        if(checkShopBuyNum(userId,shopConfig,payerGift)) {
        	log.info("商品已限购！"+goodsId);
        	s2c.setCode(3);
        	s2c.setErrStr("商品已限购："+goodsId);
        	return s2c;
        }
        //游戏道具购买商品
        if(shopConfig.getPayType() > 2) {
        	s2c = gameItemPay(s2c,shopConfig,userId);
        	return s2c;
        }
        //生成商户订单号，不可重复
        int limitLength;
        String mark;
        if (payType == 3) {
            mark = "A";
            limitLength = 40;
        } else {
            mark = "-";
            limitLength = 32;
        }
        String out_trade_no = payType + mark + userId + mark + goodsId + mark + System.currentTimeMillis();
        if (out_trade_no.length() > limitLength) {
            log.info("订单由于长度过长，从{}转为{}",out_trade_no,out_trade_no.substring(0, limitLength));
            out_trade_no = out_trade_no.substring(0, limitLength);
        }
        // 修改了支付界面维语
        String body;
        // 微信下单的维文说明,重新赋值
        String GoodsType = "";
        if(shopConfig.getGoodsType() == 1) {
        	GoodsType = "gold";
        }else if(shopConfig.getGoodsType() == 2) {
        	GoodsType = "diamond";
        }
        String total_fee = String.valueOf(shopConfig.getPrice()/100);
        int countPrice = shopConfig.getCount() + shopConfig.getAddCount();
        switch (GoodsType) {
            case "gold":
                body = countPrice + "x" + "ئالتۇن پۇرچاق";
                break;
            case "diamond":
                body = countPrice  + "x" + "ئالماس";
                break;
            default:
                body = "تاۋار";
                break;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("app_id", appId);
        data.put("goods_name", body);
        data.put("out_trade_no", out_trade_no);
        data.put("pay_type", payType);
        data.put("total_amount", total_fee);
        String signInfo = HTTPUtil.getNewSign(data, sign);
        data.put("sign", signInfo);
        Map<String, String> returnMap = new HashMap<>();
        try {
            //使用官方API请求预付订单
            //打印下单参数
            log.info("下单参数：{}" ,data.toString());
            String resStr = HTTPUtil.wxHttpPostRequest("http://kyspay.uybeliq.com/api/pay", HTTPUtil.buildReqString(data));
            JSONObject response = JSONObject.parseObject(resStr);
            log.info("下单成功，下单返回参数：{}",response.toString());
            int returnCode = response.getIntValue("code");    //获取返回码
            //若返回码为SUCCESS，则会返回一个result_code,再对该result_code进行判断
            if (returnCode == 1) {//主要返回以下5个参数
                String dataStr = response.getString("data");
                JSONObject dataJson = JSONObject.parseObject(dataStr);
                returnMap.put("pay", dataJson.getString("pay"));
                returnMap.put("out_trade_no", out_trade_no);
                //记录订单
                BuyOrder buyOrder = new BuyOrder();
                buyOrder.setPaymentSn(out_trade_no);
                buyOrder.setAddTime(new Date());
                buyOrder.setUserId(userId);
                buyOrder.setGoodsId(goodsId);
                buyOrder.setGoodsTipName("充值");
                buyOrder.setPaymentCode((byte) 1);
                buyOrder.setOrderStatus(0);
                buyOrder.setOrderSource(appId);
                buyOrder.setOrderAmount(Integer.valueOf(total_fee));
                buyOrder.setPayConfigId(payType);
                buyOrderService.insertBuyOrder(buyOrder);
                log.info("下单完成后，给大厅的参数：{}" ,dataJson.getString("pay"));
                log.info("微信支付下单正常结束！");
                S2C_BuyGoods.Builder s2cBuyGoodsBuilder = S2C_BuyGoods.newBuilder();
                s2cBuyGoodsBuilder.setBody(dataJson.getString("pay"));
                s2cBuyGoodsBuilder.setId(goodsId);
                s2cBuyGoodsBuilder.setOutTradeNo(out_trade_no);
                s2cBuyGoodsBuilder.setPayType(payType);
                s2c.setBody(s2cBuyGoodsBuilder.build().toByteArray());
                return s2c;
            } else {
                log.info("下单返回失败");
                s2c.setCode(2);
                return s2c;
            }
        } catch (Exception e) {
        	 log.info("微信支付报错!:{}",e);
            //系统等其他错误的时候
        }
        s2c.setCode(2);
        return s2c;
    }
	



	/**
	 * 检测商品是否已经下架
	 * @param goodsId
	 * @return
	 */
	private boolean checkActivity(int goodsId) {
		Map<Integer, ActivityConfig> activityMap = baseExcelMgr.getActivityMap();
		for(Map.Entry<Integer, ActivityConfig> entry : activityMap.entrySet()) {
			String[] goodsIdList = entry.getValue().getActivityShop().split(",");
			for(int i = 0;i<goodsIdList.length;i++) {
				if(Integer.valueOf(goodsIdList[i]) == goodsId) {
					if(Integer.valueOf(entry.getValue().getActivityStatus()) == 0) {
						return true;
					}
					break;
				}
			}
		}
		return false;
	}



	/**
	 * 
	 * @param s2c
	 * @param shopConfig
	 * @param userId
	 * @return
	 */
	private S2C gameItemPay(S2C s2c, ShopConfig shopConfig, int userId) {
		Player player = PlayerMgr.idMap.get(userId);
		if(shopConfig.getPayType() == 3) {
			if(player.getDiamond() >= shopConfig.getPrice()) {
				platformService.modifyDiamond(userId, -shopConfig.getPrice(), GameEventEnum.SHOPBUY.reason);
				s2c.setCid(Cmd.BUY_GOODS_END);
	    		s2c.setUid(userId);
	    		S2C_BuyGoodsEnd.Builder buyGoodsEndBuilder = S2C_BuyGoodsEnd.newBuilder();
	    		buyGoodsEndBuilder.setId(shopConfig.getGoodsId());
	    		s2c.setBody(buyGoodsEndBuilder.build().toByteArray());
	    		itemMgr.addItem(player,shopConfig.getItemId(),shopConfig.getCount() + shopConfig.getAddCount(), GameEventEnum.SHOPBUY);
			}
		}
		return s2c;
	}



	/**
	 * 检测商品购买限制
	 * @param userId
	 * @param shopConfig
	 * @return
	 */
	public boolean checkShopBuyNum(int userId, ShopConfig shopConfig,PlayerGift payerGift) {
		if(payerGift == null) {
			return false;
		}
		//永久购买限制
		if(shopConfig.getEvenLimit() > 0 && payerGift.getPermanentBuy() >= shopConfig.getEvenLimit()) {
			return true;
        }
		//每天购买
		if(shopConfig.getDayLimit() > 0 && payerGift.getEveryDayBuy() >= shopConfig.getDayLimit()) {
			return true;
		}
		//TODO 循环周期商品，待讨论配置
		return false;
	}



	/**
	 * 每日重置礼包次数
	 * @param player
	 * @param code
	 */
	public void resetGift(Player player, int code) {
		log.info("重置礼包");
		List<PlayerGift> playerGiftList = playerGiftService.selectPlayerGift(player.getId());
		if(playerGiftList == null) {
			return;
		}
		long nowTime = System.currentTimeMillis();
		for(PlayerGift playerGift : playerGiftList) {
			//过滤永久限购
			if(playerGift.getPermanentBuy() > 0) {
				continue;
			}
			//每日限购和到期礼包进行删除操作
			if(playerGift.getEndTime() == 0 || nowTime > playerGift.getEndTime()) {
				playerGiftService.deletePlayerGiftById(playerGift.getId());
				continue;
			}
			playerGift.setEveryDayBuy(0);
			playerGift.setTodayUse(0);
			playerGiftService.updatePlayerGift(playerGift);
		}
	}


	/**
	 * 每日月卡奖励
	 * @param c2s
	 * @return
	 * @throws Exception 
	 */
	public S2C monCardGiftBag(C2S c2s) throws Exception {
		C2S_MonCardGiftBag c2SMonCardGiftBag = C2S_MonCardGiftBag.parseFrom(c2s.getBody());
		int userId = c2s.getUid();
		log.info("领取月卡奖励：{}玩家id:{}",c2SMonCardGiftBag.getGoodsId(),userId);
		S2C s2c = new S2C();
        s2c.setCid(Cmd.MONCARD_GIFTBAG);
        s2c.setUid(userId);
		PlayerGift payerGift = playerGiftService.selectPlayerGiftById(c2SMonCardGiftBag.getGoodsId(),userId);
		if(payerGift == null) {
			log.info("没有月卡：{}玩家id:{}",c2SMonCardGiftBag.getGoodsId(),userId);
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1010.getTipsType());
			return s2c;
		}
		if(payerGift.getTodayUse() >= 1) {
			log.info("已经领取过月卡奖励了");
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1011.getTipsType());
			return s2c;
		}
		Player player = playerMgr.getPlayerByID(userId);
		ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(c2SMonCardGiftBag.getGoodsId());
		itemMgr.addItem(player, shopConfig.getItemId(), shopConfig.getAddCount()/shopConfig.getCycleTimes(), GameEventEnum.MON_CARD);
		S2C_MonCardGiftBag.Builder S2CMonCardAward = S2C_MonCardGiftBag.newBuilder();
		Award.Builder award = Award.newBuilder();
		award.setId(shopConfig.getItemId());
		award.setNum(shopConfig.getAddCount()/shopConfig.getCycleTimes());
		S2CMonCardAward.addAwards(award);
		S2CMonCardAward.setGoodsId(c2SMonCardGiftBag.getGoodsId());
		s2c.setBody(S2CMonCardAward.build().toByteArray());
		//更新领取记录
		payerGift.setTodayUse(payerGift.getTodayUse() + 1);
		playerGiftService.updatePlayerGift(payerGift);
		return s2c;
	}


	/**
	 * 请求复活次数
	 * @param c2s
	 * @return
	 */
	public S2C getResrrectionNum(C2S c2s) throws Exception {
		C2S_ResurrectionNum c2sResurrectionNum = C2S_ResurrectionNum.parseFrom(c2s.getBody());
		int userId = c2s.getUid();
		int roomNum = c2sResurrectionNum.getRoomNum();
		log.info("请求复活奖励次数：{}玩家id:{}",roomNum,userId);
		S2C s2c = new S2C();
        s2c.setCid(Cmd.RESURRECTION_NUM);
        s2c.setUid(userId);
		PlayerGift payerGift = null;
		List<PlayerGift> payerGiftLisst = playerGiftService.selectPlayerGift(userId);
		for(PlayerGift playerGiftOne : payerGiftLisst) {
			if(playerGiftOne.getGoodsId() != 8003) {
				continue;
			}
			if(roomNum == playerGiftOne.getRoomNum() || playerGiftOne.getRoomNum() == 0) {
				payerGift = playerGiftOne;
				break;
			}
		}
		if(payerGift == null) {
			payerGift = new PlayerGift();
			//每天购买
			payerGift.setGoodsId(8003);
			payerGift.setPlayerId(userId);
			playerGiftService.insertPlayerGift(payerGift);
		}
//		ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(payerGift.getGoodsId());
		S2C_ResurrectionNum.Builder s2cResurrectionNum = S2C_ResurrectionNum.newBuilder();
		if(payerGift.getEndTime() == 0) {
			s2cResurrectionNum.setNum(1 - payerGift.getTodayUse());
		}else if(payerGift.getEndTime() > 0){
			s2cResurrectionNum.setNum(2 - payerGift.getTodayUse());
		}
		s2c.setBody(s2cResurrectionNum.build().toByteArray());
		return s2c;
	}

	/**
	 * 请求领取复活奖励
	 * @param c2s
	 * @return
	 */
	public S2C getResrrectionGiftBag(C2S c2s) throws Exception {
		C2S_ResurrectionGiftBag c2sResurrectionNum = C2S_ResurrectionGiftBag.parseFrom(c2s.getBody());
		int userId = c2s.getUid();
		int roomNum = c2sResurrectionNum.getRoomNum();
		log.info("领取复活奖励：{}玩家id:{}",roomNum,userId);
		S2C s2c = new S2C();
        s2c.setCid(Cmd.RESURRECTION_GIFTBAG);
        s2c.setUid(userId);
		PlayerGift payerGift = null;
		List<PlayerGift> payerGiftLisst = playerGiftService.selectPlayerGift(userId);
		for(PlayerGift playerGiftOne : payerGiftLisst) {
			if(playerGiftOne.getGoodsId() != 8003) {
				continue;
			}
			if(roomNum == playerGiftOne.getRoomNum() || playerGiftOne.getRoomNum() == 0) {
				payerGift = playerGiftOne;
				break;
			}
		}
		if(payerGift == null) {
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1012.getTipsType());
			return s2c;
		}
		if(payerGift.getEndTime() == 0 && 1 - payerGift.getTodayUse() <= 0) {
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1011.getTipsType());
			return s2c;
		}else if(payerGift.getEndTime() > 0 && 2 - payerGift.getTodayUse() <= 0){
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1011.getTipsType());
			return s2c;
		}
		//更新领取记录
		payerGift.setTodayUse(payerGift.getTodayUse() + 1);
		playerGiftService.updatePlayerGift(payerGift);
		//发送奖励
		ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(8003);
		BilliardChangConfig billiardChangConfig = baseExcelMgr.getChangConfigMap().get(roomNum);
		Player player = playerMgr.getPlayerByID(userId);
		int lowerLimit = billiardChangConfig.getLowerLimit();
		log.info("自己的金币：{}自己的钻石：{}商品金币：{}商品钻石：{}",player.getGold(),player.getDiamond());
		if(billiardChangConfig.getMoneyType() == 1) {
			if(player.getGold() >= lowerLimit) {
				return null;
			}
			itemMgr.addItem(player, shopConfig.getItemId(), lowerLimit - player.getGold(), GameEventEnum.RESRRECTION);
		}else if(billiardChangConfig.getMoneyType() == 2){
			if(player.getDiamond() >= lowerLimit) {
				return null;
			}
			itemMgr.addItem(player, shopConfig.getItemId(), lowerLimit - player.getDiamond(), GameEventEnum.RESRRECTION);
		}
		return null;
	}
    
	
}
