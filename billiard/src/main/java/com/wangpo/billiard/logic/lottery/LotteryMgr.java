package com.wangpo.billiard.logic.lottery;

import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.base.enums.GlobalEnum;
import com.wangpo.base.enums.ItemEnum;
import com.wangpo.base.excel.BilliardChangConfig;
import com.wangpo.base.excel.BilliardLotteryConfig;
import com.wangpo.billiard.bean.LotteryResult;
import com.wangpo.billiard.excel.ExcelMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LotteryMgr {
	private static final Map<Integer,LotteryVo> map = new ConcurrentHashMap<>();
	private final Random r = new Random();
	//private LotteryVo vo;

	@Resource
	ExcelMgr excelMgr;

	public LotteryVo get(int chang) {
		if( map.containsKey(chang)) {
			return map.get(chang);
		}
		return null;
	}

	/**
	 * 初始化奖池
	 * @return
	 */
	public synchronized LotteryVo newLotteryVo(int chang) {
		int realChang = chang>10000?chang%10000:chang;
		//excel场次配置
		BilliardChangConfig billiardChangConfig = excelMgr.getChangConfigMap().get(realChang);
		if(billiardChangConfig.getLottery() == 0) {
			log.error("场次{} 抽奖开关未开启",realChang);
			return null;
		}


		//全局配置
		int goldExchangeRate = excelMgr.getGlobal().get(GlobalEnum.GOLD_EXCHANGE_RATE.code).intValue();
		int diamondExchangeRate = excelMgr.getGlobal().get(GlobalEnum.DIAMOND_EXCHANGE_RATE.code).intValue();
		int redPacketExchangeRate = excelMgr.getGlobal().get(GlobalEnum.RED_PACKET_EXCHANGE_RATE.code).intValue();
		int strongCardExchangeRate = excelMgr.getGlobal().get(GlobalEnum.STRONG_CARD_EXCHANGE_RATE.code).intValue();

		//场次配置
		CmsChangConfig changConfig = excelMgr.getCmsChangConfigMap().get(realChang);


		LotteryVo vo = new LotteryVo();
		vo.setChang(chang);
		//1,根据配置的上下限随机总奖池数量
		int down = changConfig.getUpLimit();// excelMgr.getGlobal(GlobalEnum.LOTTERY_DOWN.code).intValue();
		int up = changConfig.getDownLimit();//excelMgr.getGlobal(GlobalEnum.LOTTERY_UP.code).intValue();
		log.info("场次：{}，抽奖上限：{}，下线：{}",chang,up,down);
		int totalMoney = (r.nextInt(up-down +1 ) + down)*100;
		vo.setTotalMoney(totalMoney);
		log.info("抽奖总奖池金额随机：{}",totalMoney);
		//2，根据配置生成对应的LotteryItem对象
		List<CmsLotteryConfig> list = new ArrayList<>();
		for(CmsLotteryConfig l:excelMgr.getLotteryConfigMap().values() ){
			if(l.getChang() == realChang) {
				list.add(l);
			}
		}
		list.forEach((award) -> {
			for(int i=0;i<award.getNum();i++) {
				LotteryItem item = new LotteryItem();
				item.setId(award.getType());
				item.setGrade(award.getGrade());
				int base = (totalMoney*award.getWeight()/100)/award.getNum();
				item.setBase(base);
				if(award.getType()== ItemEnum.GOLD.code) {
					item.setNum(base*goldExchangeRate/100);
				} else if(award.getType()==ItemEnum.DIAMOND.code) {
					int num = base*diamondExchangeRate/100 == 0 ? 1 : base*diamondExchangeRate/100;
					item.setNum(num);
				} else if(award.getType()==ItemEnum.RED_PACKET.code) {
					int num = base*redPacketExchangeRate/100 == 0 ? 1 : base*redPacketExchangeRate/100;
					item.setNum(num);
				} else if(award.getType()==ItemEnum.STRONG_CARD.code) {
					int num = base*strongCardExchangeRate/100 == 0 ? 1 : base*strongCardExchangeRate/100;
					item.setNum(num);
				}
				vo.getLotteryItems().add(item);
				if(award.getGrade()==4) {
					vo.setFirst(item);
				}
				if(award.getGrade()==3) {
					vo.setSecond(item);
				}
				if(award.getGrade()==2) {
					vo.setThird(item);
				}
//				log.info("抽奖物品：{}，{}",item.getId(),item.getNum());
			}
		});
		//System.out.println(vo.getLotteryItems());
		log.info("抽奖模块生成奖池，奖品数量：{}",vo.getLotteryItems().size());
		//计算奖品总价值
		double total = 0;
		for(LotteryItem item:vo.getLotteryItems()) {
			if( item.getId() == 1) {
				total += item.getNum()*100/goldExchangeRate;
			} else if( item.getId() == 2) {
				total += item.getNum()*100/diamondExchangeRate;
			} else if ( item.getId() == 7 ) {
				total += item.getNum()*100/redPacketExchangeRate;
			} else if ( item.getId() == 3001) {
				total += item.getNum()*100/strongCardExchangeRate;
			}
		}
		log.info("奖品总价值：{} 分",total);
		return vo;
	}

	/**
	 * 抽奖
	 */
	public LotteryItem lottery(int chang, LotteryResult lotteryResult) {
		LotteryVo lotteryVo = get(chang);
		if( lotteryVo == null ) {
			lotteryVo = newLotteryVo(chang);
			//乱序
			Collections.shuffle(lotteryVo.getLotteryItems());
			map.put(chang,lotteryVo);
		}
//		int total = 0;
//		for(LotteryItem item:vo.getLotteryItems()) {
//			if( item.getTotal()<=0) continue;
//			total += item.getTotal();
//		}
		if( lotteryVo.getLotteryItems().size()<1 ) {
			lotteryVo = newLotteryVo(chang);
			//乱序
			Collections.shuffle(lotteryVo.getLotteryItems());
			map.put(chang,lotteryVo);
		}
		lotteryResult.setTotalMoney(lotteryVo.getTotalMoney());
		/*int hit = r.nextInt(vo.getLotteryItems().)+1;
		total = 0;
		LotteryItem hitItem = null;
		for(LotteryItem item:vo.getLotteryItems()) {
			if( item.getTotal()<=0) continue;
			int start = total;
			total += item.getTotal();
			int end = total;
			if( hit>=start && hit<=end ) {
				hitItem = item;
				item.setTotal(item.getTotal()-1);
				break;
			}
		}*/
		return lotteryVo.getLotteryItems().remove(0);
	}

	/**
	 * 其他的抽奖奖励
	 * @param chang 场次
	 * @return 奖项列表
	 */
	public List<LotteryItem> otherLottery(int chang){
		chang = 10000 + chang;
		LotteryVo lotteryVo = get(chang);
		if( lotteryVo == null ) {
			lotteryVo = newLotteryVo(chang);
			map.put(chang,lotteryVo);
		}
		List<LotteryItem> lotteryItems = lotteryVo.getLotteryItems();

		//存放其他奖项的集合
		List<LotteryItem> otherLotteryList = new ArrayList<>();
		otherLotteryList.add(lotteryVo.getFirst());
		otherLotteryList.add(lotteryVo.getSecond());
				//lotteryVo.getLotteryItems().stream().limit(2).collect(Collectors.toList());
		for (int i = 0; i < 7; i++) {
			LotteryItem lotteryItem = lotteryItems.get((int) (Math.random() * lotteryItems.size()));
			otherLotteryList.add(lotteryItem);
		}
		//乱序
		Collections.shuffle(otherLotteryList);
		return otherLotteryList;
	}


}
