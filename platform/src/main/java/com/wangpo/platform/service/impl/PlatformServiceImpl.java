package com.wangpo.platform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wangpo.base.cms.*;
import com.wangpo.base.enums.ItemEnum;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.excel.SystemConfig;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.BilliardService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.bean.*;
import com.wangpo.base.bean.PlatFormProto.Award;
import com.wangpo.base.bean.PlatFormProto.C2S_UpdateGuide;
import com.wangpo.base.bean.PlatFormProto.S2C_PushAuthentication;
import com.wangpo.base.bean.PlatFormProto.S2C_PushAward;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.GlobalEnum;
import com.wangpo.base.enums.task.TaskType;
import com.wangpo.base.excel.ActivityConfig;
import com.wangpo.base.excel.GlobalConfig;
import com.wangpo.base.kits.FormatKit;
import com.wangpo.platform.bean.GameLog;
import com.wangpo.platform.bean.LoginLog;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.bean.PlayerVip;
import com.wangpo.platform.config.ConfigMgr;
import com.wangpo.platform.dao.PlayerGift;
import com.wangpo.platform.data.ShopIdData;
import com.wangpo.platform.enums.ExcelEnum;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.activity.SignHandler;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.logic.login.WXLogin;
import com.wangpo.platform.logic.mail.MailHandler;
import com.wangpo.platform.logic.mail.MailMgr;
import com.wangpo.platform.logic.member.MemberHandler;
import com.wangpo.platform.logic.notice.NoticeHandler;
import com.wangpo.platform.logic.redpacket.RedPacketHandler;
import com.wangpo.platform.logic.task.TaskHandler;
import com.wangpo.platform.logic.task.TaskMgr;
import com.wangpo.platform.pay.PayHandler;
import com.wangpo.platform.phone.PhoneHandler;
import com.wangpo.platform.redenvelopes.RedEnvelopesHandler;

import com.wangpo.platform.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DubboService
@Slf4j
public class PlatformServiceImpl implements PlatformService {
	@Resource
	private UserService userService;
	@DubboReference
	private BilliardPushService billiardPushService;
	@Resource
	private TaskService taskService;
	@Resource
	private BaseExcelMgr baseExcelMgr;
	@Resource
	TaskHandler taskHandler;
	@Resource
	TaskMgr taskMgr;
	@Resource
	PlayerMgr playerMgr;
	@Resource
	PlayerService playerService;
	@DubboReference
	BilliardService billiardService;
	@Resource
	ItemMgr itemMgr;
	@Resource
	RedPacketHandler redPacketHandler;
	@Resource
	MemberHandler memberHandler;
	@Resource
	MailHandler mailHandler;
	@Resource
	MailService mailService;
	@Resource
	MemberService memberService;
	@Resource
	MailMgr mailMgr;
	@Resource
	SignHandler signHandler;
	@Resource
	ConfigMgr configMgr;
	@Resource
	PayHandler payHandler;
	@Resource
	GameLogService  gameLogService;
	@Resource
	RedEnvelopesHandler redEnvelopesHandler;
	@Resource
	NoticeHandler noticeHandler;
	@Resource
	PhoneHandler phoneHandler;
	@Resource
	LoginLogService loginLogService;
	@Resource
	PlayerGiftService playerGiftService;


	@Override
	public S2C request(C2S c2s) {
		try {
			int cid = c2s.getCid();
			switch (cid) {
				case Cmd.HEART:
					return heart(c2s);
				case Cmd.REQ_CONFIG:
					return reqConfig(c2s);
				case Cmd.GET_TASK:
					return taskHandler.getTask(c2s);
				case Cmd.GET_TASK_REWARD:
					return taskHandler.getTaskReward(c2s);
				case Cmd.GET_ACTIVE_REWARD:
					return taskHandler.getActiveReward(c2s);
				case Cmd.SHARE:
					return taskHandler.share(c2s);
				case Cmd.GET_RED_PACKET:
					return redPacketHandler.allRedPacket(c2s);
				case Cmd.DRAW_LOTTERY:
					return redPacketHandler.drawLottery(c2s);
				case Cmd.MEMBER_AWARD:
					return memberHandler.getDayReward(c2s);
				case Cmd.GET_MAIL:
					return mailHandler.getMail(c2s);
				case Cmd.MAIL_AWARD:
					return mailHandler.mailAward(c2s);
				case Cmd.MEMBER_INFO:
					return memberHandler.getVIPInfo(c2s);
				case Cmd.LEVEL_AWARD:
					return memberHandler.getLevelReward(c2s);
				case Cmd.SIGN:
					return signHandler.sign(c2s);
				case Cmd.SIGN_INFO:
					return signHandler.signIfo(c2s);
				case Cmd.PAY_INFO:
					return payHandler.getPayOptions(c2s);
				case Cmd.BUY_SHOP:
					return payHandler.newPrePayOrder(c2s);
				case Cmd.ALI_INFO:
					return redEnvelopesHandler.getAliInfo(c2s);
				case Cmd.WX_INFO:
					return redEnvelopesHandler.getWxInfo(c2s);
				case Cmd.REDPACKAGE:
					return redEnvelopesHandler.getRedEnvelops(c2s);
				case Cmd.BINGDING_PHONE:
					return phoneHandler.getSmsCode(c2s);
				case Cmd.PHONE_CODE:
					return phoneHandler.getBindTel(c2s);
				case Cmd.AUTHENTICATION:
					return phoneHandler.getAuthentication(c2s);
				case Cmd.GET_NOTICE:
					return noticeHandler.getNotice(c2s);
				case Cmd.GET_SYSTEM_TIP:
					return noticeHandler.getSystemTip(c2s);
				case Cmd.MONCARD_GIFTBAG:
					return payHandler.monCardGiftBag(c2s);
				case Cmd.RESURRECTION_NUM:
					return payHandler.getResrrectionNum(c2s);
				case Cmd.RESURRECTION_GIFTBAG:
					return payHandler.getResrrectionGiftBag(c2s);
				case Cmd.PUSH_PLATFORM:
					return pushPlatform(c2s);
				case Cmd.NOVICEGUIDE:
					return updateNoviceGuide(c2s);
				default:
					break;
			}
			return null;
		} catch (Exception e) {
			log.error("处理台球rpc异常，cmd：{}，错误：{}",c2s.getCid(),e);
			return null;
		} finally {

		}
	}
	
	private S2C updateNoviceGuide(C2S c2s) throws Exception {
		int uid =c2s.getUid();
		C2S_UpdateGuide guide = C2S_UpdateGuide.parseFrom(c2s.getBody());
		String noviceGuide = guide.getNum();
		Player player = playerMgr.getPlayerByID(uid);
		JSONObject obj = com.alibaba.fastjson.JSON.parseObject(noviceGuide);
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.toJavaObject(obj, Map.class);
		if(jsonMap == null) {
			jsonMap = new HashMap<String, Object>();
		}
		jsonMap.put("newUser", 0);
		String s = JSONObject.toJSONString(jsonMap);
		JSONObject obj1 = com.alibaba.fastjson.JSON.parseObject(s);
		log.info(s);
		player.setNoviceGuide(obj1);
		return null;
	}
	
	
	private S2C pushPlatform(C2S c2s) {
		int uid =c2s.getUid();
	 	List<PlayerGift> playerGiftList = playerGiftService.selectPlayerGift(uid);
		S2C s2c = new S2C();
		s2c.setCid(Cmd.PUSH_PLATFORM);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		S2C_PushAuthentication.Builder builder = S2C_PushAuthentication.newBuilder();
		if(player.getName() != null) {
			builder.setAuthentication(1);
		}else {
			builder.setAuthentication(0);
		}
		if(playerGiftList == null) {
			s2c.setBody(builder.build().toByteArray());
			billiardPushService.push(s2c);
			return s2c;
		}
		long nowTime = System.currentTimeMillis();
		for(PlayerGift playerGift : playerGiftList) {
			if(playerGift.getGoodsId() == ShopIdData.SHOPID7003) {
				builder.setFirstCharge(1);
			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID7002) {
				builder.setEveryDayFirstCharge(1);
			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID9001) {
				builder.setMonCard(1);
				builder.setMonCardTime(playerGift.getEndTime() - nowTime);
			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID8003) {
				builder.setResurrection(1);
				builder.setResurrectionTime(playerGift.getEndTime() - nowTime);
			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID9003) {
  				builder.setWeekCard(1);
  			}
		}
		ShopConfig shopConfig1 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID7003);
  		if(shopConfig1.getStatus() == 0) {
  			builder.setFirstCharge(2);
  		}
  		ShopConfig shopConfig2 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID7002);
  		if(shopConfig2.getStatus() == 0) {
  			builder.setEveryDayFirstCharge(2);
  		}
  		ShopConfig shopConfig3 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID9001);
  		if(shopConfig3.getStatus() == 0) {
  			builder.setMonCard(2);
  		}
  		ShopConfig shopConfig4 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID8003);
  		if(shopConfig4.getStatus() == 0) {
  			builder.setResurrection(2);
  		}
  		ShopConfig shopConfig5 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID9003);
  		if(shopConfig5.getStatus() == 0) {
  			builder.setWeekCard(2);
  		}
  		ShopConfig shopConfig6 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID13003);
  		ShopConfig shopConfig7 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID13002);
  		ShopConfig shopConfig8 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID13001);
  		if(shopConfig6.getStatus() == 1 || shopConfig7.getStatus() == 1 || shopConfig8.getStatus() == 1) {
  			builder.setLuckyRod(1);
  		}else {
  			builder.setLuckyRod(2);
  		}
		s2c.setBody(builder.build().toByteArray());
		billiardPushService.push(s2c);
		return s2c;
   }

   private S2C heart(C2S c2s) {
		if( c2s.getUid()<=0) {
			return null;
		}
	   Player player = playerMgr.getPlayerByID(c2s.getUid());
	   if( player != null ) {
	   	    player.setHeartTime(System.currentTimeMillis());
		   S2C s2c = new S2C();
		   s2c.setCid(Cmd.HEART);
		   s2c.setUid(c2s.getUid());
		   s2c.setBody(c2s.getBody());
		   return s2c;
	   }
		return null;
   }

	private S2C reqConfig(C2S c2s) throws Exception {
		BilliardProto.C2S_GetConfig proto = BilliardProto.C2S_GetConfig.parseFrom(c2s.getBody());
		int type = proto.getConfigType();

		for(ExcelEnum e:ExcelEnum.values()) {
			if( type == e.getCode()) {
				return e.config(c2s.getCid());
			}
		}
		return null;
	}

	@Resource
	WXLogin wxLogin;
	@Override
	public CommonUser login(C2S c2s) {
		PlatFormProto.C2S_Login  b = null;
		try {
			b = PlatFormProto.C2S_Login.parseFrom(c2s.getBody());
		} catch (InvalidProtocolBufferException e) {
			log.error("登录解析异常:",e);
			return null;
		}
//		log.info("登录设备：{}",b.getDevice());
		int loginType = b.getLoginType();
//		loginType = 2;
		String token = b.getCode();//对应openID，
		String origin = b.getOrigin();//渠道
		int parentId = b.getParentId();
		log.error("登录，类型：{}，token:{},渠道：{}",loginType,token,origin);
		if( origin ==null || "".equals(origin.trim())) {
			origin = "test";
		}
		Player player = playerMgr.getPlayerByToken(token);
		if( player == null ) {
			player = playerService.selectPlayerByOpenid(token);
			if (player == null) {
				if( loginType == 0 ) {
					//1，模拟微信登录
					player = new Player();
					player.setParentId(parentId);
					player.setOpenId(token);
					player.setOrigin(origin);
					player.setNewOrigin(origin);
					player.setSex(1);
					player.setNick(token.length()>8?token.substring(0,8):token);
					player.setHead("test.jpg");
					String noviceGuide = "{\"newUser\": 0}";
					JSONObject obj = com.alibaba.fastjson.JSON.parseObject(noviceGuide);
					player.setNoviceGuide(obj);
					initNewPlayer(player);
				} else if( loginType == 1) {
					//2，微信code登录，微信首次登录
					try {
						JSONObject userInfo = wxLogin.weChatLogin(token);
						log.error("微信首次登录:{}",userInfo);
						String openId;
						if( userInfo == null || !userInfo.containsKey("openid")) {
							//微信登录失败
							log.error("微信登录失败,userInfo:{}，code:{}",userInfo,token);
							return null;
						}

						openId = userInfo.getString("openid");
						//通过openid查询玩家
						player = playerService.selectPlayerByOpenid(openId);
						if( player == null ) {
//							log.info("微信第一次登录，创建账号，openId:{}",openId);
							player = new Player();
							player.setParentId(parentId);
							player.setToken(userInfo.getString("access_token"));
							player.setNick(userInfo.getString("nickname"));
							player.setHead(userInfo.getString("headimgurl"));
							player.setSex(userInfo.getInteger("sex"));
							player.setUnionId(userInfo.getString("unionid"));
							//TODO 渠道是否需要换？
							player.setOrigin(origin);
							player.setNewOrigin(origin);
							String noviceGuide = "{\"newUser\": 0}";
							JSONObject obj = com.alibaba.fastjson.JSON.parseObject(noviceGuide);
							player.setNoviceGuide(obj);
							initNewPlayer(player);
							player.setParentId(parentId);
						} else {
							player.setParentId(parentId);
							player.setToken(userInfo.getString("access_token"));
							player.setNick(userInfo.getString("nickname"));
//							log.info("微信登录昵称：{}",userInfo.getString("nickname"));
							player.setHead(userInfo.getString("headimgurl"));
							player.setSex(userInfo.getInteger("sex"));
							player.setUnionId(userInfo.getString("unionid"));
							player.setNewOrigin(origin);
							player.setOpenId(openId);
							JSONObject noviceGuide = player.getNoviceGuide();
							if(noviceGuide.get("newUser") == null) {
								String noviceGuideInfo = "{\"newUser\":0,\"wh\": 104, \"ganUp\": true, \"ganWH\": true, \"train\": true, \"upgrade\": 204, \"guideIdx\": 0}";
								JSONObject obj = com.alibaba.fastjson.JSON.parseObject(noviceGuideInfo);
								player.setNoviceGuide(obj);
							}
							//更换apk，有可能更换渠道，这里进行更新
//							log.info("微信非第一次登录，openId:{},nick:{}",player.getOpenId(),player.getNick());
						}
						player.setOpenId(openId);
					} catch (Exception e) {
						log.error("微信登录异常,token:{}",token,e);
						return null;
					}
				} else if( loginType == 2 ) {
					//3，通过openid查询玩家
//					log.info("微信openid登录：{}",token);
					Player dbPlayer = playerService.selectPlayerByOpenid(token);

					if( dbPlayer == null ) {
						log.error("微信openid登录失败，不存在该openid：{}",token);
						CommonUser cu = new CommonUser();
						cu.setStatus(2);
						return cu;
					}
				} else {
					log.error("通过access_token登录找不到玩家，access_token：{}",token);
					return null;
				}
			}else {
				JSONObject noviceGuide = player.getNoviceGuide();
				if(noviceGuide.get("newUser") == null) {
					String noviceGuideInfo = "{\"newUser\":0,\"wh\": 104, \"ganUp\": true, \"ganWH\": true, \"train\": true, \"upgrade\": 204, \"guideIdx\": 0}";
					JSONObject obj = com.alibaba.fastjson.JSON.parseObject(noviceGuideInfo);
					player.setNoviceGuide(obj);
				}
			}
			//账号被封
			if (player.getStatus() == 1 ) {
				return player.toCommonUser();
			}
			//获取任务列表
			List<Task> taskList = taskHandler.queryAllTask(player.getId());
			player.setTaskList(taskList);
//			player.setUserEntity(userEntity);
			//获取会员等级
			memberHandler.getVIPLevel(player);
			//将用户添加到缓存
			playerMgr.addPlayerByID(player);
			playerMgr.addPlayerByToken(player);
			player.setOnline(true);
		} else {
			playerMgr.addPlayerByID(player);
			JSONObject noviceGuide = player.getNoviceGuide();
			if(noviceGuide == null || noviceGuide.get("newUser") == null) {
				String noviceGuideInfo = "{\"newUser\":0,\"wh\": 104, \"ganUp\": true, \"ganWH\": true, \"train\": true, \"upgrade\": 204, \"guideIdx\": 0}";
				JSONObject obj = com.alibaba.fastjson.JSON.parseObject(noviceGuideInfo);
				player.setNoviceGuide(obj);
			}
			player.setOnline(true);
		}

		return player.toCommonUser();
	}

	//TODO 登录的后续处理，比如推送实名认证信息。
	@Override
	public void afterLogin(int uid) {
		//推送实名认证
		List<PlayerGift> playerGiftList = playerGiftService.selectPlayerGift(uid);
  		S2C s2c = new S2C();
  		s2c.setCid(Cmd.PUSH_PLATFORM);
  		s2c.setUid(uid);
  		Player player = playerMgr.getPlayerByID(uid);
  		S2C_PushAuthentication.Builder builder = S2C_PushAuthentication.newBuilder();
  		S2C_PushAward.Builder pushAwardBuilder = S2C_PushAward.newBuilder();
  		if(player.getName() != null) {
  			builder.setAuthentication(1);
  		}else {
  			builder.setAuthentication(0);
  		}
  		long nowTime = System.currentTimeMillis();
//  		log.info("登录后推送");
  		for(PlayerGift playerGift : playerGiftList) {
//  			log.info("登录后推送:{}",playerGift.getGoodsId());
  			if(playerGift.getGoodsId() == ShopIdData.SHOPID7003) {
  				builder.setFirstCharge(1);
  			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID7002) {
  				builder.setEveryDayFirstCharge(1);
  			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID9001) {
  				builder.setMonCard(1);
  				builder.setMonCardTime(playerGift.getEndTime() - nowTime);
  				if(playerGift.getTodayUse() == 0) {
  					Award.Builder award = Award.newBuilder();
  					ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(playerGift.getGoodsId());
  					award.setId(shopConfig.getItemId());
  					award.setNum(shopConfig.getAddCount()/shopConfig.getCycleTimes());
  					pushAwardBuilder.addAwards(award);
  					pushAwardBuilder.setGoodsId(ShopIdData.SHOPID9001);
  				}
  			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID8003) {
  				builder.setResurrection(1);
  				builder.setResurrectionTime(playerGift.getEndTime() - nowTime);
  			}else if(playerGift.getGoodsId() == ShopIdData.SHOPID9003) {
  				builder.setWeekCard(1);
  				if(playerGift.getTodayUse() == 0) {
  					Award.Builder award = Award.newBuilder();
  	  				ShopConfig shopConfig = baseExcelMgr.getShopConfigMap().get(playerGift.getGoodsId());
  	  				award.setId(shopConfig.getItemId());
  					award.setNum(shopConfig.getAddCount()/shopConfig.getCycleTimes());
  					pushAwardBuilder.addAwards(award);
  					pushAwardBuilder.setGoodsId(ShopIdData.SHOPID9003);
  				}
  			}
  		}
  		ShopConfig shopConfig1 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID7003);
  		if( shopConfig1!= null && shopConfig1.getStatus() == 0) {
  			builder.setFirstCharge(2);
  		}
  		ShopConfig shopConfig2 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID7002);
  		if(shopConfig2!= null && shopConfig2.getStatus() == 0) {
  			builder.setEveryDayFirstCharge(2);
  		}
  		ShopConfig shopConfig3 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID9001);
  		if(shopConfig3!= null && shopConfig3.getStatus() == 0) {
  			builder.setMonCard(2);
  		}
  		ShopConfig shopConfig4 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID8003);
  		if(shopConfig4!= null && shopConfig4.getStatus() == 0) {
  			builder.setResurrection(2);
  		}
  		ShopConfig shopConfig5 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID9003);
  		if(shopConfig5!= null && shopConfig5.getStatus() == 0) {
  			builder.setWeekCard(2);
  		}
  		ShopConfig shopConfig6 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID13003);
  		ShopConfig shopConfig7 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID13002);
  		ShopConfig shopConfig8 = baseExcelMgr.getShopConfigMap().get(ShopIdData.SHOPID13001);
  		if(shopConfig6.getStatus() == 1 || shopConfig7.getStatus() == 1 || shopConfig8.getStatus() == 1) {
  			builder.setLuckyRod(1);
  		}else {
  			builder.setLuckyRod(2);
  		}
  		//已被限购的商品
  		for(PlayerGift playerGift : playerGiftList) {
  			ShopConfig shopConfig9 = baseExcelMgr.getShopConfigMap().get(playerGift.getGoodsId());
  			if(payHandler.checkShopBuyNum(uid, shopConfig9, playerGift)) {
  				builder.addShopBuyNum(playerGift.getGoodsId());
  			}
  		}
  		
  		//活动信息
  		Map<Integer, ActivityConfig> activityMap = baseExcelMgr.getActivityMap();
  		List<ActivityConfig> activityList = new ArrayList<ActivityConfig>();
  		for(Map.Entry<Integer, ActivityConfig> entry : activityMap.entrySet()) {
  			if(Integer.valueOf(entry.getValue().getActivityStatus()) == 1) {
  				activityList.add(entry.getValue());
  			}
  		}
  		if(activityList.size() > 0) {
  			String  param = JSON.toJSONString(activityList);
  	  		builder.setActivityInfo(param);
  		}
  		s2c.setBody(builder.build().toByteArray());
		billiardPushService.push(s2c);
		S2C pushAwardS2C = new S2C();
		pushAwardS2C.setCid(Cmd.PUSH_MONCARD_AWARD);
		pushAwardS2C.setUid(uid);
		pushAwardS2C.setBody(pushAwardBuilder.build().toByteArray());
		billiardPushService.push(pushAwardS2C);


		//初始化玩家的信息
		long t1 = System.currentTimeMillis();
		//获取用户邮件
		mailHandler.getMailList(player);
		//获取系统邮件
		mailMgr.initPlayerSystemMail(player);
//		log.info("邮件初始化时间：{} ms",(System.currentTimeMillis()-t1));
		//登录衰退玩家VIP
		memberHandler.declineVip(player);
		player.setOnline(true);
		//更新用户登录时间
		player.setLoginTime(new Date());
		String today = FormatKit.today10();
		String lastMonday = FormatKit.lastMonday();
		PlayerVip playerVip = player.getPlayerVip();
		String today1 = playerVip.getToday();
		//每日重置
		if (!today.equals(today1)) {
			playerVip.setDayGift(0);
			playerVip.setToday(today);
			memberService.updateMember(playerVip);
			//重置各种礼包的每日领取数量
			resetGift(player, TaskType.DAY.code);
			//重置签到信息
			player.setSignStatus(0);
		}
		//如果隔天登录，则重置每日任务
		if (!today.equals(player.getLastDay()) && FormatKit.hour() >= 4){
			taskHandler.resetPlayerTask(player, TaskType.DAY.code);
		}
		if (!lastMonday.equals(player.getLastMonday()) && FormatKit.hour() >= 4) {
			taskHandler.resetPlayerTask(player,TaskType.WEEK.code);
		}
		playerService.updatePlayer(player);

		//记录登录日志
		LoginLog loginLog = player.getLoginLog();
		if( loginLog == null || !today.equals( loginLog.getLoginDay() )) {
			loginLog = loginLogService.selectLoginLogByPlayerId(player.getId(),today);
			if(loginLog==null) {
				loginLog = new LoginLog();
				loginLog.setLoginDay(today);
				loginLog.setCreateDay(FormatKit.today10(player.getCreateTime()));
				loginLog.setPlayerId(player.getId());
				loginLog.setLoginTimes(1);
				loginLogService.insertLoginLog(loginLog);
			}
			player.setLoginLog(loginLog);
		} else  {
			loginLog.setLoginTimes(loginLog.getLoginTimes()+1);
			loginLogService.updateLoginLog(loginLog);
		}
	}

	/**
	 * 每日重置礼包次数
	 * @param player
	 * @param code
	 */
	private void resetGift(Player player, int code) {
		payHandler.resetGift(player, TaskType.DAY.code);
	}

	private void initNewPlayer(Player player) {
		//初始金币
		GlobalConfig gold =BaseExcelMgr.GLOBAL_MAP.get(GlobalEnum.GOLD.code);
		if (gold != null) {
			player.setGold(gold.intValue());
		} else {
			player.setGold(0);
		}
		//初始钻石
		GlobalConfig diamond =  BaseExcelMgr.GLOBAL_MAP.get(GlobalEnum.DIAMOND.code);
		if (diamond != null) {
			player.setDiamond(diamond.intValue());
		} else {
			player.setDiamond(0);
		}

		player.setRedPacket(0);
		player.setDayActive(0);
		player.setDayActiveStatus(0);
		player.setWeekActive(0);
		player.setWeekActiveStatus(0);
		player.setSignDay(0);
		player.setSignStatus(0);
		player.setLastDay(FormatKit.today10());
		player.setLastMonday(FormatKit.lastMonday());
		player.setLoginTime(new Date());
		player.setStatus(0);
		playerService.insertPlayer(player);
		int playerGold = player.getGold();
		if (playerGold > 0) {
			GameLog gameLog = new GameLog(1,player.getId(),ItemEnum.GOLD.code,playerGold,GameEventEnum.NEW_USER.reason);
			gameLogService.insertGameLog(gameLog);
		}
		int playerDiamond = player.getDiamond();
		if (playerDiamond > 0) {
			GameLog gameLog = new GameLog(1,player.getId(),ItemEnum.DIAMOND.code,playerDiamond,GameEventEnum.NEW_USER.reason);
			gameLogService.insertGameLog(gameLog);
		}
	}

	@Override
	public void logout(int uid) {
		try {
			Player player = playerMgr.getPlayerByID(uid);
			if( player != null && player.isOnline() ) {
				log.error("玩家离线，玩家ID：{}",player.getId());
				player.setOfflineTime(System.currentTimeMillis());
				player.setOnline(false);
				//更新邮件状态
				List<Mail> mailList = player.getMailList();
				for (Mail mail : mailList) {
					mailService.updateMail(mail);
				}
				//更新vip状态
				PlayerVip playerVip = player.getPlayerVip();
				memberService.updateMember(playerVip);
				//提交在线玩家缓存删除
				playerMgr.submitOfflinePlayer(player.getId());
				player.setLogoutTime(new Date());
				playerService.updatePlayer(player);
				//更新任务
				List<Task> taskList = player.getTaskList();
				for (Task task : taskList) {
					taskService.updateTask(taskMgr.task2TaskEntity(task));
				}

				//记录玩家登录日志
				LoginLog loginLog = player.getLoginLog();
				if( loginLog != null ) {
					int onlineTime = (int)((System.currentTimeMillis() - player.getLoginTime().getTime()) /1000);
					loginLog.setOnline(loginLog.getOnline()+onlineTime);
					loginLogService.updateLoginLog(loginLog);
				}
			}
		} catch (Exception e) {
			log.error("玩家退出登录处理异常：",e);
		}
	}

	@Override
	public CommonUser queryUserByID(Integer uid) {
		Player player = playerMgr.getPlayerByID(uid);
		if( player == null ) {
			player = playerService.selectPlayerById(uid);
		}
		if( player != null ) {
			return player.toCommonUser();
		}
		return null;
	}

	/**
	 * 修改金币接口
	 * @param uid
	 * @param modifyNum
	 * @param reason
	 * @return  null 扣除失败，非null，扣除成功并返回玩家基础对象
	 */
	@Override
	public CommonUser modifyGold(int uid, int modifyNum, String reason) {
		Player player = playerMgr.getPlayerByID(uid);
		if( player != null ) {
			//扣除后小于0，则不处理
			if( player.getGold()+modifyNum <0 ) {
				return null;
			}
			player.setGold(player.getGold()+modifyNum);
			playerService.updateGoldById(uid,modifyNum);
			CommonUser commonUser = player.toCommonUser();
			//通知用户
			S2C s2c = new S2C();
			s2c.setCid(Cmd.UPDATE_GOLD);
			s2c.setUid(uid);
			s2c.setBody(PlatFormProto.S2C_UpdateGold.newBuilder().setId(uid).setGold(commonUser.getGold()).build().toByteArray());
			billiardPushService.push(s2c);
			//通知游戏服更新玩家信息
			billiardService.syncUser(commonUser);

			//保存日志
			gameLog(player,ItemEnum.GOLD.code,modifyNum,player.getGold(),reason);
			return commonUser;
		} else if( uid>0 ){
//			log.info("更新金币，玩家不在线：{}",uid);
			playerService.updateGoldById(uid,modifyNum);

			//保存日志
			gameLog(uid,ItemEnum.GOLD.code,modifyNum,reason);
			return null;
//			commonUser = userService.selectByID(uid);
//			log.info("modifyGold,uid:{},modifyNum:{},reason:{}",uid,modifyNum,reason);
//			commonUser.setGold(commonUser.getGold()+modifyNum);
		}
		return null;
//		userService.update(userEntity);
//		return commonUser;
	}


	/**
	 * 修改钻石接口
	 * @param id
	 * @param modifyNum
	 * @param reason
	 * @return  null 扣除失败，非null，扣除成功并返回玩家基础对象
	 */
	@Override
	public CommonUser modifyDiamond(int id, int modifyNum, String reason) {
		Player player = playerMgr.getPlayerByID(id);
		if( player != null ) {
			//扣除后小于0，则不处理
			if( player.getDiamond()+modifyNum <0 ) {
				return null;
			}
			player.setDiamond(player.getDiamond()+modifyNum);
			playerService.updateDiamondById(id,modifyNum);
			CommonUser userEntity = player.toCommonUser();

			//通知用户
			S2C s2c = new S2C();
			s2c.setUid(id);
			s2c.setCid(Cmd.UPDATE_DIAMOND);
			s2c.setBody(PlatFormProto.S2C_UpdateDiamond.newBuilder().setId(id).setDiamond(userEntity.getDiamond()).build().toByteArray());
			billiardPushService.push(s2c);
			//通知游戏服更新玩家信息
			billiardService.syncUser(player.toCommonUser());

			//保存日志
			gameLog(player,ItemEnum.DIAMOND.code,modifyNum,player.getDiamond(),reason);
			return userEntity;
		} else if( id>0 ) {
//			log.info("更新钻石，玩家不在线：{}",id);
			playerService.updateDiamondById(id,modifyNum);
			//保存日志
			gameLog(id,ItemEnum.DIAMOND.code,modifyNum,reason);
			return null;
		}
		return null;
	}


	/**
	 * 修改红包接口
	 * @param id
	 * @param num
	 * @param reason
	 * @return  null 扣除失败，非null，扣除成功并返回玩家基础对象
	 */
	@Override
	public CommonUser modifyRedPacket(int id, int num, String reason) {
		Player player = playerMgr.getPlayerByID(id);
		if( player != null ) {
			//扣除后小于0，则不处理
			if( player.getRedPacket()+num <0 ) {
				return null;
			}
			player.setRedPacket(player.getRedPacket()+num);
			playerService.updateRedPacketById(id,num);
			CommonUser userEntity = player.toCommonUser();

			//通知用户
			S2C s2c = new S2C();
			s2c.setUid(id);
			s2c.setCid(Cmd.UPDATE_RED_PACKET);
			s2c.setBody(PlatFormProto.S2C_UpdateRedPacket.newBuilder().setId(id).setRedPacket(userEntity.getRedPacket()).build().toByteArray());
			billiardPushService.push(s2c);
			//通知游戏服更新玩家信息
			billiardService.syncUser(player.toCommonUser());

			//保存日志
			gameLog(player,ItemEnum.RED_PACKET.code,num,player.getRedPacket(),reason);
			return userEntity;
		} else if( id>0 ) {
//			log.info("更新红包券，玩家不在线：{}",id);
			playerService.updateRedPacketById(id,num);
			//保存日志
			gameLog(id,ItemEnum.RED_PACKET.code,num,reason);
			return null;
		}
		return null;
	}

	public void gameLog(Player player, int modelId,int num,int remainNum,String reason){
		GameLog gameLog = new GameLog();
		gameLog.setPlayerId(player.getId());
		gameLog.setCreateTime(new Date());
		gameLog.setItemId(modelId);
		gameLog.setItemNum(num);
		gameLog.setRemainNum(remainNum);
		gameLog.setReason(reason);
		gameLog.setLogType(1);
		gameLogService.insertGameLog(gameLog);
	}

	public void gameLog(int id, int modelId,int num,String reason){
		GameLog gameLog = new GameLog();
		gameLog.setPlayerId(id);
		gameLog.setCreateTime(new Date());
		gameLog.setItemId(modelId);
		gameLog.setItemNum(num);
		gameLog.setReason(reason);
		gameLog.setLogType(1);
		gameLogService.insertGameLog(gameLog);
	}

	@Override
	public void addItem(int uid, int modelId, int num, GameEventEnum eventEnum) {
		Player player = playerMgr.getPlayerByID(uid);
		if (player != null) {
			itemMgr.addItem(player,modelId,num,eventEnum);
		}
	}

	@Override
	public boolean useItem(int uid, int modelId, int num) {
		Player player = playerMgr.getPlayerByID(uid);
		if (player == null) {
			return false;
		}
		return itemMgr.use(player,modelId,num);
	}

	@Override
	public void finishTask(int uid, int sid, TaskData taskData) {
		taskHandler.finishTask(uid, sid, taskData);
	}

	@Override
	public void sendMail(Mail mail) {
		if (mail.getEndTime() == null) {
			mail.setEndTime(FormatKit.nextYears(10));
		}
		//判断是系统邮件，还是个人邮件
		if ("".equals(mail.getPlayerIds())) {
			mailMgr.addSystemMail(mail);
		} else {
			mailMgr.addPersonalMail(mail);
		}
	}

	@Override
	public void addRedPacket(int uid, int chang, int num) {
		Player player = playerMgr.getPlayerByID(uid);
		if (player != null) {
			redPacketHandler.addRedPacket(player,chang,num);
		}
	}

	@Override
	public void modifyShopConfig(int type, ShopConfig shopConfig) {
		configMgr.modifyShopConfig(type, shopConfig);
	}

	@Override
	public void modifySystemConfig(int type, SystemConfig systemConfig) {
		configMgr.modifySystemConfig(type,systemConfig);
	}

	@Override
	public void modifyAppVersion(int type, APPVersion appVersion) {
		configMgr.modifyAppVersion(type,appVersion);
	}

	@Override
	public void modifyChannelConfig(int type, ChannelConfig channelConfig) {
		configMgr.modifyChannelConfig(type,channelConfig);
	}

	@Override
	public void modifyResourceConfig(int type, ResourceConfig resourceConfig) {
		configMgr.modifyResourceConfig(type,resourceConfig);
	}

	@Override
	public void modifyNotice(int type, Notice notice) {
		configMgr.modifyNotice(type,notice);
	}

	@Override
	public void modifyCmsSystemNotice(int type, CmsSystemNotice notice) {
		configMgr.modifyCmsSystemNotice(type,notice);
	}

	@Override
	public boolean getUserName(int id) {
		Player player = playerMgr.getPlayerByID(id);
		return player.getName() != null;
	}

    @Override
    public int getOnlineCount(String origin) {
		int onlineCount = 0;
		if (origin == null) {
			for (Player player : playerMgr.getIdMap().values()) {
				if (player.isOnline()) {
					onlineCount++;
				}
			}
		} else {
			for (Player player : playerMgr.getIdMap().values()) {
				if (player.isOnline() && player.getOrigin().equals(origin)) {
					onlineCount++;
				}
			}
		}
		log.error("实时统计，在线人数：{}",onlineCount);
        return onlineCount;
    }

	@Override
	public void freezePlayer(int id, int status) {
		Player player = playerMgr.getPlayerByID(id);
		if (player != null) {
			player.setStatus(status);
		}
		billiardPushService.close(id);
	}

}
