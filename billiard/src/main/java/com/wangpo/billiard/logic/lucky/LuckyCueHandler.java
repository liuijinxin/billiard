package com.wangpo.billiard.logic.lucky;


import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.excel.BilliardLuckyCueConfig;
import com.wangpo.base.item.Item;
import com.wangpo.billiard.bean.LuckyCue;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.item.ItemMgr;
import com.wangpo.billiard.service.LuckyCueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class LuckyCueHandler {
	@Resource
	PlayerMgr playerMgr;
	@Resource
	LuckyCueService luckyCueService;
	@DubboReference
	BilliardPushService billiardPushService;
	@Resource
	ExcelMgr excelMgr;
	@Resource
	ItemMgr itemMgr;

	//幸运一杆
	public S2C luckyCue(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null ) {
			BilliardProto.C2S_Lucky proto = BilliardProto.C2S_Lucky.parseFrom(c2s.getBody());
			int luckyType = proto.getLuckyType();//类型，1-免费，2-vip
			int result = proto.getResult(); //中环，1-2-3-4
//			log.info("幸运一杆结果：{}，{}",luckyType,result);
			if( luckyType<1 ||luckyType >2) {
				log.error("幸运一杆参数异常，luckType:{}",luckyType);
				return null;
			}
			if( result<0 || result>4) {
				log.error("幸运一杆参数异常，result:{}",result);
				return null;
			}
			if( luckyType==1 && result==4) {
				return null;
			}



			int and = 1<<(result-1);
			LuckyCue luckyCue = player.getLuckyCue();
			//没有领奖次数，返回错误
			if(luckyCue.getRewardTimes()>=0) {
				log.error("幸运一杆没有领奖次数");
				return null;
			}

			if(luckyCue.getRewardType()!=luckyType) {
				log.error("幸运一杆领奖方式错误");
				return null;
			}
			if( luckyType == 1) {
				//免费击球
				if( result==0 ) {
					if( luckyCue.getFreeTimes() <=0 ) {
						//开始加cd
						luckyCue.setFreeTime(System.currentTimeMillis() + 48*60*60*1000);
					}
				} else {
					/*if(luckyCue.getFreeTime()>0) {
						long t = luckyCue.getFreeTime() ;
						if( t > System.currentTimeMillis() ) {
							log.error("免费cd未结束，无法击球");
							pushError(c2s,1);
							return null;
						}
					}*/
					if((luckyCue.getFreeFlag()&and) == and) {
						log.error("该环奖励已领取,rewardFlag:{},id:{}",luckyCue.getFreeFlag(),player.getId());
//						luckyCue.setFreeTimes(luckyCue.getFreeTimes()-1);
						pushError(c2s,2);
					} else {
						luckyCue.setFreeFlag(luckyCue.getFreeFlag()+and);
//						log.info("配置后，rewardFlag:{}",luckyCue.getFreeFlag());
//						luckyCue.setFreeTimes(luckyCue.getFreeTimes()-1);
						if( luckyCue.getFreeTimes() <=0 ) {
							//开始加cd
							luckyCue.setFreeTime(System.currentTimeMillis()+ 48*60*60*1000);
						}
						//读配置给奖励。
						BilliardLuckyCueConfig config = ExcelMgr.LUCKY_CUE_MAP.get(luckyCue.getLevel());
						if( config!=null) {
							if( result <= config.getFreeAwardList().size() ) {
								List<Item> award = config.getFreeAwardList().get(result-1);
								for(Item item:award){
									itemMgr.addItem(player,item.getId(),item.getNum(),GameEventEnum.LUCKY_CUE);
								}
								itemMgr.sendAward(player,award,GameEventEnum.LUCKY_CUE);
							}
						}
					}
				}

			} else  {
				/*if( luckyCue.getVipTimes() <1 ) {
					log.error("vip次数已用完");
					return null;
				}*/

				if( result == 0 ) {
//					luckyCue.setVipTimes(luckyCue.getVipTimes()-1);
				} else {
					if((luckyCue.getVipFlag()&and) == and) {
						log.error("该环奖励已领取,rewardFlag:{},id:{}",luckyCue.getVipFlag(),player.getId());
//						luckyCue.setVipTimes(luckyCue.getVipTimes()-1);
						pushError(c2s,2);
					} else {
//						luckyCue.setVipTimes(luckyCue.getVipTimes()-1);
						luckyCue.setVipFlag(luckyCue.getVipFlag()+and);
						//读配置给奖励。
						BilliardLuckyCueConfig config = excelMgr.getLuckyCueMap().get(luckyCue.getLevel());
						if( config!=null) {

							if( result <= config.getVipAwardList().size() ) {

								List<Item> award = config.getVipAwardList().get(result-1);
								for(Item item:award){
									itemMgr.addItem(player,item.getId(),item.getNum(),GameEventEnum.LUCKY_CUE);
								}
								itemMgr.sendAward(player,award,GameEventEnum.LUCKY_CUE);
							}
						}
						//中四环，升级
						if( result == 4) {
							luckyCue.setLevel(luckyCue.getLevel()+1);
							luckyCue.setFreeFlag(0);
							luckyCue.setVipFlag(0);
							//如果到达最高级，重置当前等级数据
							config = excelMgr.getLuckyCueMap().get(luckyCue.getLevel());
							if( config == null ) {
								luckyCue.setLevel(luckyCue.getLevel()-1);
							}
						}
					}
				}

			}
			pushLuckyCue(player);
			//更新
			luckyCueService.updateLuckyCue(luckyCue);
		}
		return null;
	}

	public S2C go(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null ) {
			BilliardProto.C2S_LuckyCueOpt proto = BilliardProto.C2S_LuckyCueOpt.parseFrom(c2s.getBody());
			int luckyType = proto.getLuckyType();//类型，1-免费，2-vip

			if( luckyType<1 ||luckyType >2) {
				log.error("幸运一杆参数异常，luckType:{}",luckyType);
				return null;
			}
			LuckyCue luckyCue = player.getLuckyCue();
//			log.info("幸运一杆击球：{},免费次数：{}",luckyType,luckyCue.getFreeTimes());
			if( luckyType == 1) {
				//免费击球
				if(luckyCue.getFreeTime()>0) {
					long t = luckyCue.getFreeTime() ;
					if( t > System.currentTimeMillis() ) {
						log.error("免费cd未结束，无法击球");
						pushError(c2s,1);
						return null;
					}
				}

				if( luckyCue.getFreeTimes() <=0 ) {
					log.error("免费次数已经用完");
					pushError(c2s,1);
					return null;
				}

				luckyCue.setFreeTimes(luckyCue.getFreeTimes()-1);
				if( luckyCue.getFreeTimes()<= 0 ) {
					luckyCue.setFreeTime(System.currentTimeMillis() + 48*60*60*1000);
				}

//				log.info("幸运一杆击球2：{},免费次数：{}",luckyType,luckyCue.getFreeTimes());
			} else  {
				if( luckyCue.getVipTimes() <1 ) {
					log.error("vip次数已用完");
					pushError(c2s,2);
					return null;
				}

				luckyCue.setVipTimes(luckyCue.getVipTimes()-1);
			}

			luckyCue.setRewardTimes(-1);
			luckyCue.setRewardType(luckyType);
			luckyCueService.updateLuckyCue(luckyCue);
		}
		return null;
	}

	public void pushLuckyCue(Player player){
		S2C s2c = new S2C();
		s2c.setCid(Cmd.LUCKY_CUE_DATA);
		s2c.setUid(player.getId());
		long remainTime = (player.getLuckyCue().getFreeTime() - System.currentTimeMillis() )/1000;
		if(remainTime < 0 ) {
			remainTime = 0;
		} else {
			log.info("幸运一杆倒计时：{}",remainTime);
		}
		int freeTimes = player.getLuckyCue().getFreeTimes();
//		log.info("幸运一杆次数：{}，id：{}",freeTimes,player.getId());
		if( freeTimes <=0 && remainTime ==0) {
			//cd时间到，重置免费奖励
			player.getLuckyCue().setFreeTimes(1);
			player.getLuckyCue().setFreeFlag(0);
			freeTimes = 1;
		}
//		log.info("幸运一杆次数：{}，id：{}",freeTimes,player.getId());
		s2c.setBody(BilliardProto.S2C_LuckCue.newBuilder()
				.setLevel(player.getLuckyCue().getLevel())
				.setFreeTimes(freeTimes)
				.setVipTimes(player.getLuckyCue().getVipTimes())
				.setFreeFlag(player.getLuckyCue().getFreeFlag())
				.setVipFlag(player.getLuckyCue().getVipFlag())
				.setRemainTime(remainTime)
			.build().toByteArray());
		billiardPushService.push(s2c);
	}

	public void pushError(C2S c2s,int code){
		S2C error = new S2C();
		error.setCode(code);
		error.setCid(c2s.getCid());
		error.setUid(c2s.getUid());
		billiardPushService.push(error);
	}
}
