package com.wangpo.platform.redenvelopes;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.ali.AliAccount;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto.AliInfo;
import com.wangpo.base.bean.PlatFormProto.C2S_RedPackage;
import com.wangpo.base.bean.PlatFormProto.S2C_AliInfo;
import com.wangpo.base.bean.PlatFormProto.S2C_BuyGoodsEnd;
import com.wangpo.base.bean.PlatFormProto.S2C_RedPackage;
import com.wangpo.base.bean.PlatFormProto.S2C_WxOpenIdInfo;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.excel.MemberConfig;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.dao.BuyOrder;
import com.wangpo.platform.dao.PlayerGift;
import com.wangpo.platform.dao.WxFollower;
import com.wangpo.platform.enums.ErrorTipsEnum;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.pay.PayHandler;
import com.wangpo.platform.service.BuyOrderService;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerGiftService;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.PlayerService;
import com.wangpo.platform.service.WxFollowerService;
import com.wangpo.platform.util.HTTPUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedEnvelopesHandler {
	@Resource
	BuyOrderService buyOrderService;

	@Resource
	BaseExcelMgr baseExcelMgr;

	@Resource
	PlayerMgr playerMgr;
	
	@Resource
	PlayerService playerService;
	
	@Resource
	WxFollowerService wxFollowerService;
	
	@Resource
	ItemMgr itemMgr;
	
	@Resource
	PlayerGiftService playerGiftService;
	
	@Resource
	PlatformService platformService;
	
	@DubboReference
	private BilliardPushService billiardPushService;

	/**
	 * 获取支付宝账号信息
	 * 
	 * @param c2s
	 * @return
	 */
	public S2C getAliInfo(C2S c2s) {
		log.info("请求支付宝账号信息");
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.ALI_INFO);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		S2C_AliInfo.Builder s2cAliInfoBuilder = S2C_AliInfo.newBuilder();
		if(player.getPhone() == null) {
			Player newPlayer = playerService.selectPlayerById(player.getId());
			if(newPlayer.getPhone()!= null) {
				player.setPhone(newPlayer.getPhone());
				s2cAliInfoBuilder.setPhone(Long.valueOf(newPlayer.getPhone()));
			}
		}else {
			s2cAliInfoBuilder.setPhone(Long.valueOf(player.getPhone()));
		}
		if (player.getAlipay().size() == 0) {
			s2c.setBody(s2cAliInfoBuilder.build().toByteArray());
			return s2c;
		}
		Map<Integer, JSON> jsonMap = (Map<Integer, JSON>) JSONObject.toJavaObject(player.getAlipay(), Map.class);
		for (int i = 1; i <= jsonMap.size(); i++) {
			JSON json = jsonMap.get(i);
			if(json == null) {
				continue;
			}
			JSONObject obja = com.alibaba.fastjson.JSON.parseObject(json.toJSONString());
			AliInfo.Builder aliInfoBuilder = AliInfo.newBuilder();
			aliInfoBuilder.setAliAccount(obja.getString("account"));
			aliInfoBuilder.setAliName(obja.getString("name"));
			s2cAliInfoBuilder.addAliInfo(aliInfoBuilder);
		}
		s2c.setBody(s2cAliInfoBuilder.build().toByteArray());
		return s2c;
	}
	
	/**
	 * 获取微信信息
	 * @param c2s
	 * @return
	 */
	public S2C getWxInfo(C2S c2s) {
		log.info("请求支付宝账号信息");
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.WX_INFO);
		s2c.setUid(uid);
		S2C_WxOpenIdInfo.Builder s2cWxOpenIdInfo = S2C_WxOpenIdInfo.newBuilder();
		Player player = playerMgr.getPlayerByID(uid);
		WxFollower wxFollower = wxFollowerService.selectByID(player.getUnionId());
		if(wxFollower != null) {
			s2cWxOpenIdInfo.setSubscribe(1);
		}
		if(player.getPhone() == null) {
			Player newPlayer = playerService.selectPlayerById(player.getId());
			if(newPlayer.getPhone() != null) {
				player.setPhone(newPlayer.getPhone());
				s2cWxOpenIdInfo.setPhone(newPlayer.getPhone());
			}
		}else {
			s2cWxOpenIdInfo.setPhone(player.getPhone());
		}
		s2c.setBody(s2cWxOpenIdInfo.build().toByteArray());
		return s2c;
	}

	/**
	 * 兑换红包
	 * 
	 * @param c2s
	 * @return
	 * @throws Exception
	 */
	public S2C getRedEnvelops(C2S c2s) throws Exception {
		C2S_RedPackage c2sAliRedPackage = C2S_RedPackage.parseFrom(c2s.getBody());
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.REDPACKAGE);
		s2c.setUid(uid);
		int goodsId = c2sAliRedPackage.getId();
		log.info("兑换红包:{}",goodsId);
		Player player = playerMgr.getPlayerByID(uid);
		ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(goodsId);
		if (shopConfig == null) {
			log.error("兑换红包未找到相关配置：{}" , goodsId);
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1001.getTipsType());
			return s2c;
		}
		if(shopConfig.getStatus() == 0) {
        	log.error("商品已下架：{}" , goodsId);
        	s2c.setCode(ErrorTipsEnum.TIPS_INFO1016.getTipsType());
        	return s2c;
        }
		// 检测兑换红包
		if (shopConfig.getPrice() > player.getRedPacket()) {
			log.error("红包数量不足,配置数量：{},玩家红包数：{}",shopConfig.getPrice(),player.getRedPacket());
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1002.getTipsType());
			return s2c;
		}
		//检测红包兑换限制
		PlayerGift payerGift = playerGiftService.selectPlayerGiftById(shopConfig.getGoodsId(),uid);
		if(checkRedEnvelopsEveryDay(shopConfig,player,payerGift)) {
			log.info("达到红包最大兑换次数：{}",uid);
			s2c.setCode(ErrorTipsEnum.TIPS_INFO1014.getTipsType());
			return s2c;
		}
		//扣除红包
		platformService.modifyRedPacket(player.getId(),  -shopConfig.getPrice(), GameEventEnum.RED_ENVELOPER.reason );
		//记录限购
        savePlayerGift(shopConfig,payerGift,uid);
		int returnCode = 0;
		if(shopConfig.getGoodsType() == 1 || shopConfig.getGoodsType() == 2) {
			itemMgr.addItem(player,shopConfig.getItemId(),shopConfig.getCount() + shopConfig.getAddCount(), GameEventEnum.REDENVELOPS);
			S2C s2cGoods = new S2C();
			s2cGoods.setCid(Cmd.BUY_GOODS_END);
			s2cGoods.setUid(uid);
    		S2C_BuyGoodsEnd.Builder buyGoodsEndBuilder = S2C_BuyGoodsEnd.newBuilder();
    		buyGoodsEndBuilder.setId(shopConfig.getGoodsId());
    		s2c.setBody(buyGoodsEndBuilder.build().toByteArray());
    		billiardPushService.push(s2cGoods);
		}else if(shopConfig.getGoodsType() == 3 && shopConfig.getGoodsType() == 3) {
			returnCode = sendWxPayRedBag(uid, 2, shopConfig);
			if (returnCode == 0) {
				platformService.modifyRedPacket(player.getId(),  shopConfig.getPrice(), GameEventEnum.RED_ENVELOPER.reason );
				s2c.setCode(ErrorTipsEnum.TIPS_INFO1003.getTipsType());
				return s2c;
			}
		}else if(shopConfig.getGoodsType() == 11 && shopConfig.getGoodsType() == 11) {
			String account = c2sAliRedPackage.getAliAccount();
			if (account == null) {
				platformService.modifyRedPacket(player.getId(),  shopConfig.getPrice(), GameEventEnum.RED_ENVELOPER.reason );
				s2c.setCode(ErrorTipsEnum.TIPS_INFO1004.getTipsType());
				return s2c;
			}
			String name = c2sAliRedPackage.getAliName();
			if (name == null) {
				platformService.modifyRedPacket(player.getId(),  shopConfig.getPrice(), GameEventEnum.RED_ENVELOPER.reason );
				s2c.setCode(ErrorTipsEnum.TIPS_INFO1005.getTipsType());
				return s2c;
			}
			returnCode = sendAliPayRedBag(uid, 1, shopConfig, account, name);
			if (returnCode == 0) {
				platformService.modifyRedPacket(player.getId(),  shopConfig.getPrice(), GameEventEnum.RED_ENVELOPER.reason );
				s2c.setCode(ErrorTipsEnum.TIPS_INFO1003.getTipsType());
				return s2c;
			}
			List<String> accountList = new ArrayList<String>();
			accountList.add(account);
			Map<Integer, JSON> jsonMap = (Map<Integer, JSON>) JSONObject.toJavaObject(player.getAlipay(), Map.class);
			Map<Integer, AliAccount> accountMap = new HashMap<Integer, AliAccount>();
			AliAccount aliAccount = new AliAccount();
			aliAccount.setAccount(account);
			aliAccount.setName(name);
			accountMap.put(1, aliAccount);
			for (int i = 1; i <= jsonMap.size(); i++) {
				JSON ali = jsonMap.get(i);
				if(ali == null) {
					continue;
				}
				JSONObject obja = com.alibaba.fastjson.JSON.parseObject(ali.toJSONString());
				String objaAccount = obja.getString("account");
				if(accountList.contains(objaAccount)) {
					continue;
				}
				accountList.add(objaAccount);
				AliAccount nextAliAccount = new AliAccount();
				nextAliAccount.setAccount(obja.getString("account"));
				nextAliAccount.setName(obja.getString("name"));
				accountMap.put(i + 1, nextAliAccount);
				if (accountMap.size() >= 5) {
					break;
				}
			}
			String json = JSON.toJSONString(accountMap);
			JSONObject obj = com.alibaba.fastjson.JSON.parseObject(json);
			player.setAlipay(obj);
			playerService.updateAlipayById(player.getId(), obj);
		}
		S2C_RedPackage.Builder s2cAliRedPackageBuilder = S2C_RedPackage.newBuilder();
		s2cAliRedPackageBuilder.setId(goodsId);
		s2c.setBody(s2cAliRedPackageBuilder.build().toByteArray());
		return s2c;
	}
	
	/**
	 * 检测红包兑换次数，红包只有每日限购
	 * @param shopConfig
	 * @param player
	 * @param payerGift
	 * @return
	 */
	private boolean checkRedEnvelopsEveryDay(ShopConfig shopConfig, Player player, PlayerGift payerGift) {
		if(payerGift == null) {
			return false;
		}
		MemberConfig memberConfig = baseExcelMgr.getMemberMap().get(player.getPlayerVip().getLevelGift());
		if(payerGift.getEveryDayBuy() >= memberConfig.getExchangeTimes()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 记录商品购买次数，红包只有每日限购
	 * @param shopConfig
	 * @param payerGift 
	 */
	private void savePlayerGift(ShopConfig shopConfig, PlayerGift payerGift,int userId) {
		if(shopConfig.getDayLimit() == 0) {
			return;
		}
		if(payerGift== null) {
			payerGift = new PlayerGift();
			payerGift.setGoodsId(shopConfig.getGoodsId());
			payerGift.setPlayerId(userId);
			payerGift.setEveryDayBuy(payerGift.getEveryDayBuy() + 1);
			playerGiftService.insertPlayerGift(payerGift);
			return;
		}
		//每天购买
		payerGift.setEveryDayBuy(payerGift.getEveryDayBuy() + 1);
		playerGiftService.updatePlayerGift(payerGift);
	}

	/**
	 * 微信红包
	 * @param userId
	 * @param payType
	 * @param shopConfig
	 * @return
	 * @throws Exception 
	 */
	private int sendWxPayRedBag(int userId, int payType, ShopConfig shopConfig) throws Exception {
		log.info("玩家{}-{}微信红包支付下单开始！", userId, payType);
		Player player = playerMgr.getPlayerByID(userId);
		WxFollower wxFollower = wxFollowerService.selectByID(player.getUnionId());
		BigDecimal price = new BigDecimal(shopConfig.getCount());
		price = price.setScale(0, BigDecimal.ROUND_DOWN);
		String total_fee = String.valueOf(price);
		// 生成商户订单号，不可重复
		String out_trade_no = String.valueOf(payType)  + userId  + shopConfig.getGoodsId() + System.currentTimeMillis();
		if (out_trade_no.length() > 32) {
			log.info("订单由于长度过长，从{}转为{}", out_trade_no, out_trade_no.substring(0, 32));
			out_trade_no = out_trade_no.substring(0, 32);
		}
		// 修改了支付界面维语
		String body;
		// 微信下单的维文说明,重新赋值
		switch (shopConfig.getGoodsType()) {
		case 1:
			body = shopConfig.getCount() + "x" + "红包";
			break;
		case 2:
			body = shopConfig.getCount() + "x" + "红包";
			break;
		default:
			body = "推广奖励";
			break;
		}
		Map<String, Object> data = new HashMap<>();
		data.put("app_id", PayHandler.appId);
		data.put("pay_type", payType);
		data.put("out_trade_no", out_trade_no);
		data.put("total_amount", total_fee);
		String chiStr = URLDecoder.decode(body, "utf-8");
		data.put("goods_name", chiStr);
		data.put("wxpay_type", 1);
		data.put("openid", wxFollower.getOpenId());
		data.put("wishing", "恭喜获得大红包");
		data.put("act_name", "红包奖励");
		data.put("remark", "微信红包");
		String signInfo = HTTPUtil.getNewSign(data, PayHandler.sign);
		data.put("sign", signInfo);
		try {
			// 使用官方API请求预付订单
			// 打印下单参数
			log.info("微信下单参数：{}" ,data.toString());
			String resStr = HTTPUtil.wxHttpPostRequest("http://kyspay.uybeliq.com/api/pay/disburse",
					HTTPUtil.buildReqString(data));
			JSONObject response = JSONObject.parseObject(resStr);
			log.info("微信下单完毕，下单返回参数：" + response.toString());
			int returnCode = response.getIntValue("code"); // 获取返回码
			// 若返回码为SUCCESS，则会返回一个result_code,再对该result_code进行判断
			if (returnCode == 1) {
				// 记录订单
                BuyOrder buyOrder = new BuyOrder();
                buyOrder.setPaymentSn(out_trade_no);
                buyOrder.setAddTime(new Date());
                buyOrder.setUserId(userId);
                buyOrder.setGoodsId(shopConfig.getGoodsId());
                buyOrder.setGoodsTipName("微信红包");
                buyOrder.setPaymentCode((byte) 1);
                buyOrder.setOrderStatus(1);
                buyOrder.setOrderSource(1005);
                buyOrder.setOrderAmount(Integer.valueOf(total_fee));
                buyOrder.setPayConfigId(0);
                buyOrderService.insertBuyOrder(buyOrder);
			}
			return returnCode;
		} catch (Exception e) {
			log.error("阿支付报错!", e);
		}
		return 0;
	}

	/**
	 * 支付宝红包
	 * 
	 * @param userId
	 * @param payType
	 * @param aliAccount
	 * @param aliName
	 * @return
	 */
	private int sendAliPayRedBag(int userId, int payType, ShopConfig shopConfig, String aliAccount, String aliName) {
		log.info("玩家{}-{}红包支付下单开始！", userId, payType);
		BigDecimal price = new BigDecimal(shopConfig.getCount());
		price = price.setScale(0, BigDecimal.ROUND_DOWN);
		String total_fee = String.valueOf(price);
		// 获取随机支付商户
//        PayConfig payConfig = shopDubboHandler.getPayConfig(payType);
//        if (payConfig == null) {
//            log.error("根据配置ID{}从tp_pay_config中获取到的配置为null!", payType);
//            return 100;
//        }
		// 生成商户订单号，不可重复
		String out_trade_no = payType + "-" + userId + "-" + shopConfig.getGoodsId() + "-" + System.currentTimeMillis();
		if (out_trade_no.length() > 32) {
			log.info("订单由于长度过长，从{}转为{}", out_trade_no, out_trade_no.substring(0, 32));
			out_trade_no = out_trade_no.substring(0, 32);
		}
		// 修改了支付界面维语
		String body;
		// 微信下单的维文说明,重新赋值
		switch (shopConfig.getGoodsType()) {
		case 1:
			body = shopConfig.getCount() + "x" + "ئالتۇن پۇرچاق";
			break;
		case 2:
			body = shopConfig.getCount() + "x" + "ئالماس";
			break;
		default:
			body = "تاۋار";
			break;
		}
		Map<String, Object> data = new HashMap<>();
		data.put("app_id", PayHandler.appId);
		data.put("goods_name", body);
		data.put("out_trade_no", out_trade_no);
		data.put("pay_type", payType);
		data.put("total_amount", total_fee);
		data.put("payee_logon_id", aliAccount);
		data.put("real_name", aliName);
		String signInfo = HTTPUtil.getNewSign(data, PayHandler.sign);
		data.put("sign", signInfo);
		try {
			// 使用官方API请求预付订单
			// 打印下单参数
			String resStr = HTTPUtil.wxHttpPostRequest("http://kyspay.uybeliq.com/api/pay/disburse",
					HTTPUtil.buildReqString(data));
			JSONObject response = JSONObject.parseObject(resStr);
			log.info("下单完毕，下单返回参数：" + response.toString());
			int returnCode = response.getIntValue("code"); // 获取返回码
			// 若返回码为SUCCESS，则会返回一个result_code,再对该result_code进行判断
			if (returnCode == 1) {
				// 记录订单
                BuyOrder buyOrder = new BuyOrder();
                buyOrder.setPaymentSn(out_trade_no);
                buyOrder.setAddTime(new Date());
                buyOrder.setUserId(userId);
                buyOrder.setGoodsId(shopConfig.getGoodsId());
                buyOrder.setGoodsTipName("支付宝红包");
                buyOrder.setPaymentCode((byte) 1);
                buyOrder.setOrderStatus(1);
                buyOrder.setOrderSource(PayHandler.appId);
                buyOrder.setOrderAmount(Integer.valueOf(total_fee));
                buyOrder.setPayConfigId(0);
                buyOrderService.insertBuyOrder(buyOrder);
			}
			return returnCode;
		} catch (Exception e) {
			log.error("阿支付报错!", e);
			// 系统等其他错误的时候
		}
		return 0;
	}

	public static void main(String[] args) {
		Map<Integer, AliAccount> accountMap = new HashMap<Integer, AliAccount>();
		AliAccount account1 = new AliAccount();
		account1.setAccount(132456789l + "");
		account1.setName("fasfa");
		AliAccount account2 = new AliAccount();
		account2.setAccount(987654321l + "");
		account2.setName("frfs");
		accountMap.put(1, account1);
		accountMap.put(2, account2);
//		JSONArray array= JSONArray.parseArray(JSON.toJSONString(accountList));
//		String json = JSON.toJSONString(array);
//		JSONArray tableData = JSONArray.parseArray(json);
//		List<AliAccount> list = JSONObject.parseArray(tableData.toJSONString(), AliAccount.class);
		String json = JSON.toJSONString(accountMap);
		JSONObject obj = com.alibaba.fastjson.JSON.parseObject(json);
		Player player = new Player();
		if (player.getAlipay().isEmpty()) {
		}
		JSONObject.parseObject(obj.toJSONString());
		player.setAlipay(obj);
		Map<Integer, JSON> jsonMap = (Map<Integer, JSON>) JSONObject.toJavaObject(player.getAlipay(), Map.class);
		for (int i = 1; i <= jsonMap.size(); i++) {
			JSON aaaa = jsonMap.get(i);
			JSONObject obja = com.alibaba.fastjson.JSON.parseObject(aaaa.toJSONString());
			String name = obja.getString("name");
			AliInfo.Builder aliInfoBuilder = AliInfo.newBuilder();
			aliInfoBuilder.setAliAccount("");
			aliInfoBuilder.setAliName("");
		}
	}


}
