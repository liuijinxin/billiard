package com.wangpo.billiard.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.wangpo.base.cms.CmsChangConfig;
import com.wangpo.base.cms.CmsLotteryConfig;
import com.wangpo.base.cms.MatchConfig;
import com.wangpo.base.excel.*;
import com.wangpo.base.excel.BilliardCueConfig;
import com.wangpo.base.excel.BilliardCueTypeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ExcelMgr {

	public static final Map<Integer, BilliardCueConfig> CUE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardCueTypeConfig> CUE_TYPE_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, GrowthTaskConfig> GREW_UP_TASK_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, DayTaskConfig> DAY_TASK_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, WeekTaskConfig> WEEK_TASK_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, DayActiveConfig> DAY_ACTIVE_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, WeekActiveConfig> WEEK_ACTIVE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardFileCodeConfig> FILE_CODE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardRoleConfig> ROlE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, ItemConfig> ITEM_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardChangConfig> CHANG_CONFIG_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, MemberConfig> MEMBER_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, GlobalConfig> GLOBAL_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardLotteryConfig> AWARD_POOL_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardLuckyCueConfig> LUCKY_CUE_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, GoodsConfig> GOODS_MAP = new ConcurrentHashMap<>();
//	public static final Map<Integer, EveryDaySign> SIGN_MAP = new ConcurrentHashMap<>();

	//后台配置
	public static final Map<Integer, MatchConfig> MATCH_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, CmsLotteryConfig> LOTTERY_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, CmsChangConfig> CMS_CHANG_CONFIG_MAP = new ConcurrentHashMap<>();

	public static Map<Integer, SystemConfig> SYSTEM_CONFIG_MAP = new ConcurrentHashMap<>();

	//文件最后修改日期
	private static final Map<String,Long> fileModifyTimeMap = new ConcurrentHashMap<>();
	//文件对应的类
	private static final Map<String,Class[]> classMap = new HashMap<>();

	private static final String PREFIX;

	static {
		String path3 = System.getProperty("user.dir");
		PREFIX = path3 + File.separator + "excel" + File.separator;
		Class[] cue = {BilliardCueTypeConfig.class, BilliardCueConfig.class};
		classMap.put("球杆配置", cue);
		Class[] role = {BilliardRoleConfig.class};
		classMap.put("角色等级", role);
		Class[] item = {ItemConfig.class};
		classMap.put("道具列表", item);
		Class[] chang = {BilliardChangConfig.class};
		classMap.put("场次配置", chang);
		Class[] global = {GlobalConfig.class};
		classMap.put("全局配置表", global);
		Class[] lottery = {BilliardLotteryConfig.class};
		classMap.put("奖池配置表", lottery);
		Class[] luckyCue = {BilliardLuckyCueConfig.class};
		classMap.put("幸运一杆配置表", luckyCue);
		Class[] task = {DayTaskConfig.class, WeekTaskConfig.class, GrowthTaskConfig.class, DayActiveConfig.class, WeekActiveConfig.class, BilliardFileCodeConfig.class};
		classMap.put("任务系统", task);
	}

	//读取所有的配置文件
	public void readExcel() throws IOException {
		for (Map.Entry<String, Class[]> entry : classMap.entrySet()) {
			readExcel(entry.getKey(),entry.getValue());
		}
	}

	public void readExcel(String name, Class[] clazz) throws IOException {
		File file = new File(PREFIX + name + ".xlsx");
		log.info("读取配置文件：{}", name);
		//存储文件的最后修改日期，方便热加载
		fileModifyTimeMap.put(name, file.lastModified());
		/*InputStream inputStream = new FileInputStream(file);
		DataListener listener = new DataListener();
		EasyExcel.read(inputStream, clazz, listener).sheet().doRead();
		List dataList = listener.list;*/
		ExcelReader excelReader = null;
		try {
			excelReader = EasyExcel.read(file).build();
			// 这里为了简单 所以注册了 同样的head 和Listener 自己使用功能必须不同的Listener
			List<ReadSheet> readSheetList = new ArrayList<>();
			List<DataListener> dataListenerList = new ArrayList<>();
			List<Class> sheetNameList = new ArrayList<>();
			int sheetNo = 0;
			for (Class cls : clazz) {
				DataListener listener = new DataListener();
				ReadSheet rs =
						EasyExcel.readSheet(sheetNo).head(cls).registerReadListener(listener).build();
				readSheetList.add(rs);
				sheetNameList.add(cls);
				dataListenerList.add(listener);
				sheetNo++;
			}
			// 这里注意 一定要把sheet1 sheet2 一起传进去，不然有个问题就是03版的excel 会读取多次，浪费性能
			excelReader.read(readSheetList);
			if (dataListenerList.size() > 0) {
				for (int i = 0; i < dataListenerList.size(); i++) {
					store(sheetNameList.get(i), dataListenerList.get(i).list);
				}
			}
		} finally {
			if (excelReader != null) {
				// 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
				excelReader.finish();
			}
		}
	}

//	public void readExcel(String name, Class clazz) throws IOException {
//		File file = new File(PREFIX + name + ".xlsx");
//		//存储文件的最后修改日期，方便热加载
//		fileModifyTimeMap.put(name,file.lastModified());
//		InputStream inputStream = new FileInputStream(file);
//		DataListener listener = new DataListener();
//		EasyExcel.read(inputStream, clazz, listener).sheet().doRead();
//		List dataList =  listener.list;
//		store(name,dataList);
//	}

	public void store(Class name, List dataList) {
		if (BilliardCueConfig.class.equals(name)) {
			//球杆
			store2map(CUE_MAP, dataList);
		} else if (BilliardCueTypeConfig.class.equals(name)) {
			//球杆类型
			store2map(CUE_TYPE_MAP, dataList);
		} else if (GrowthTaskConfig.class.equals(name)) {
			//成长任务
//			store2map(GROWTH_TASK_MAP, dataList);
		} else if (DayTaskConfig.class.equals(name)) {
			//每日任务
//			store2map(DAY_TASK_MAP, dataList);
		} else if (WeekTaskConfig.class.equals(name)) {
			//每周任务
//			store2map(WEEK_TASK_MAP, dataList);
		} else if (DayActiveConfig.class.equals(name)) {
			//日活跃
//			store2map(DAY_ACTIVE_MAP, dataList);
		} else if (WeekActiveConfig.class.equals(name)) {
			//周活跃
//			store2map(WEEK_ACTIVE_MAP, dataList);
		} else if (BilliardChangConfig.class.equals(name)) {
			//台球场次配置
			store2map(CHANG_CONFIG_MAP, dataList);
		} else if (BilliardRoleConfig.class.equals(name)) {
			//台球角色配置
			store2map(ROlE_MAP, dataList);
		} else if (ItemConfig.class.equals(name)) {
			//物品配置
			store2map(ITEM_MAP, dataList);
		} else if (MemberConfig.class.equals(name)) {
			//vip配置
//			store2map(MEMBER_MAP, dataList);
		}  else if (GlobalConfig.class.equals(name)) {
			//全局配置
			store2map(GLOBAL_MAP, dataList);
		} else if (BilliardLotteryConfig.class.equals(name)) {
			//台球抽奖奖池配置
			store2map(AWARD_POOL_MAP, dataList);
		} else if (GoodsConfig.class.equals(name)) {
			//商城配置
//			store2map(GOODS_MAP, dataList);
		} else if (BilliardLuckyCueConfig.class.equals(name)) {
			//台球幸运一杆配置
			store2map(LUCKY_CUE_MAP, dataList);
		} else if (EveryDaySign.class.equals(name)) {
			//每日签到配置
//			store2map(SIGN_MAP, dataList);
		} else if (BilliardFileCodeConfig.class.equals(name)) {
			store2map(FILE_CODE_MAP,dataList);
		}
	}


	/**
	 * 自动热更新，此方法，需要quartz调用
	 */
	public void hotReload() {
		File file = new File(PREFIX);
		int ok = 0;
		if( file.isDirectory()) {
			String[] files = file.list();
			for(String s:files) {
				File f = new File(PREFIX + s);
				String name = f.getName().substring(0,f.getName().length()-5);
				if( !fileModifyTimeMap.containsKey(name)) {
					continue;
				}
				long t = fileModifyTimeMap.get(name);
				if( f.lastModified() <= t) {
					continue;
				}
				if( f.isFile() && f.getName().endsWith(".xlsx")) {
					//判断xlsx文件是否有更新
					Class[] clazz = classMap.get(name);
					if( clazz != null ) {
						try {
							readExcel(name,clazz);
							ok++;
						} catch (IOException e) {
							log.error("热更新配置文件 {} 异常：",name,e);
						}
					}
				}
			}
		}
		if(ok>0) {
			log.error("热加载完成，更新文件个数：{}",ok);
		}
	}

	public Map<Integer, BilliardLotteryConfig> getAwardPoolMap() {
		return AWARD_POOL_MAP;
	}

	public Map<Integer, BilliardChangConfig> getChangConfigMap() {
		return CHANG_CONFIG_MAP;
	}
	public Map<Integer, BilliardRoleConfig> getROlE_MAP() {
		return ROlE_MAP;
	}
	public BilliardRoleConfig getRoleById(int id) {
		return ROlE_MAP.get(id);
	}
	public Map<Integer, ItemConfig> getItemMap() {
		return ITEM_MAP;
	}
//	public Map<Integer, DayTaskConfig> getDayTaskMap() {
//		return DAY_TASK_MAP;
//	}
//	public Map<Integer, DayActiveConfig> getDayActiveMap() {
//		return DAY_ACTIVE_MAP;
//	}
//	public Map<Integer, WeekActiveConfig> getWeekActiveMap() {
//		return WEEK_ACTIVE_MAP;
//	}
	public Map<Integer, MatchConfig> getMatchConfigMap() {
		return MATCH_CONFIG_MAP;
	}
	public Map<Integer, BilliardCueConfig> getCueMap() {
		return CUE_MAP;
	}
	public BilliardCueTypeConfig getPlayerCueType(int cueId) {
		return CUE_TYPE_MAP.get(cueId);
	}
	public Map<Integer, BilliardCueTypeConfig> getCueTypeMap() {
		return CUE_TYPE_MAP;
	}
	public BilliardCueConfig getPlayerCue(int cueId) {
		return CUE_MAP.get(cueId);
	}
	public Map<Integer, BilliardFileCodeConfig> getFileCodeMap() {
		return FILE_CODE_MAP;
	}
	public BilliardFileCodeConfig getFileCodeById(int id) {
		return FILE_CODE_MAP.get(id);
	}
	/*public Map<Integer, WeekTaskConfig> getWeekTaskMap() {
		return WEEK_TASK_MAP;
	}
	public Map<Integer, GrowthTaskConfig> getGrewUpTaskMap() {
		return GREW_UP_TASK_MAP;
	}
	public Map<Integer, MemberConfig> getMemberMap() {
		return MEMBER_MAP;
	}
	public MemberConfig getMemberMapById(int id) {
		return MEMBER_MAP.get(id);
	}*/
	public Map<Integer, GlobalConfig> getGlobal() {
		return GLOBAL_MAP;
	}
	public Map<Integer, BilliardLuckyCueConfig> getLuckyCueMap() {
		return LUCKY_CUE_MAP;
	}
	public Map<Integer, CmsLotteryConfig> getLotteryConfigMap() {
		return LOTTERY_CONFIG_MAP;
	}
//	public Map<Integer, GoodsConfig> getGoodsMap() {
//		return GOODS_MAP;
//	}
	public Map<Integer, CmsChangConfig> getCmsChangConfigMap() {
		return CMS_CHANG_CONFIG_MAP;
	}

	public GlobalConfig getGlobal(int id) {
		if( !GLOBAL_MAP.containsKey(id)) {
			return null;
		}
		return GLOBAL_MAP.get(id);
	}

	public void store2map(Map map, List list) {
		for(Object o:list) {
			IConfig c = (IConfig)o;
			try{
				c.explain();
			}catch (Exception e) {
				log.error("配置解析异常，o:{}",o);
				e.printStackTrace();
				System.exit(1);
			}
			map.put(c.getId(),c);
		}
		log.info("配置数量：{}",list.size());
	}

//	public T get(int id, Class<T> tClass) {
//
//	}

//	private void storeLuckyCue(List<BilliardLuckyCueConfig> dataList) {
//		for (BilliardLuckyCueConfig luckyCueConfig : dataList) {
//			luckyCueConfig.explain();
//			LUCKY_CUE_MAP.put(luckyCueConfig.getId(),luckyCueConfig);
//		}
//		log.info("存储LuckyCue成功，数据量：{}",LUCKY_CUE_MAP.size());
//	}

//	private void storeGoods(List<GoodsConfig> dataList) {
//		for (GoodsConfig goodsConfig : dataList) {
//			GOODS_MAP.put(goodsConfig.getId(),goodsConfig);
//		}
//		log.info("存储Goods成功，数据量：{}",GOODS_MAP.size());
//	}

	private void storeAwardPool(List<BilliardLotteryConfig> list) {
		for (BilliardLotteryConfig awardPool : list) {
			AWARD_POOL_MAP.put(awardPool.getId(),awardPool);
		}
//		log.info("存储AwardPool成功，数据量：{}",AWARD_POOL_MAP.size());
	}

//	private void storeGlobal(List<GlobalConfig> list) {
//		for (GlobalConfig globalConfig : list) {
//			GLOBAL_MAP.put(globalConfig.getId(), globalConfig);
//		}
//		log.info("存储Global成功，数据量：{}",GLOBAL_MAP.size());
//	}

//	public void storeChang(List<BilliardChangConfig> list) {
//		for (BilliardChangConfig changConfig : list) {
//			String exp = changConfig.getExp();
//			if (!"0".equals(exp)) {
//				String[] split = exp.split(",");
//				changConfig.setWinExp(Integer.parseInt(split[0]));
//				changConfig.setLoseExp(Integer.parseInt(split[1]));
//			}
//			CHANG_CONFIG_MAP.put(changConfig.getId(),changConfig);
//		}
//		log.info("存储ChangConfig成功，数据量：{}",CHANG_CONFIG_MAP.size());
//	}

//	public void storeMember(List<MemberConfig> list) {
//		for (MemberConfig memberConfig : list) {
//			String dayReward = memberConfig.getDayReward();
//			if (!"0".equals(dayReward)) {
//				String[] split = dayReward.split(";");
//				Map<String,Object> dayRewards = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					dayRewards.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(dayRewards);
//				memberConfig.setDayRewards(jsonObject);
//			}
//			String upgradeReward = memberConfig.getUpgradeReward();
//			if (!"0".equals(upgradeReward)) {
//				String[] split2 = upgradeReward.split(";");
//				Map<String,Object> upgradeRewards = new HashMap<>();
//				for (String s1 : split2) {
//					String[] split3 = s1.split(",");
//					upgradeRewards.put(split3[0],split3[1]);
//				}
//				JSONObject jsonObject = new JSONObject(upgradeRewards);
//				memberConfig.setUpgradeRewards(jsonObject);
//			}
//			MEMBER_MAP.put(memberConfig.getId(), memberConfig);
//		}
//		log.info("存储Member成功，数据量：{}",MEMBER_MAP.size());
//	}

//	public void storeItem(List<ItemConfig> list) {
//		for (ItemConfig itemConfig : list) {
//			ITEM_MAP.put(itemConfig.getId(),itemConfig);
//		}
//		log.info("存储Item成功，数据量：{}",ITEM_MAP.size());
//	}

//	public void storeRole(List<BilliardRoleConfig> list) {
//		for (BilliardRoleConfig roleConfig : list) {
//			String price = roleConfig.getPrice();
//			if (!"0".equals(price)) {
//				String[] split = price.split(",");
//				roleConfig.setBuyType(Integer.parseInt(split[0]));
//				roleConfig.setBuyPrice(Integer.parseInt(split[1]));
//			}
//			ROlE_MAP.put(roleConfig.getId(), roleConfig);
//		}
//		log.info("存储Role成功，数据量：{}",ROlE_MAP.size());
//	}

//	public void storeDayActive(List<DayActiveConfig> list) {
//		for (DayActiveConfig dayActiveConfig : list) {
//			String reward = dayActiveConfig.getReward();
//			if (!"0".equals(reward)) {
//				String[] split = reward.split(";");
//				Map<String,Object> dayRewards = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					dayRewards.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(dayRewards);
//				dayActiveConfig.setRewards(jsonObject);
//			}
//			DAY_ACTIVE_MAP.put(dayActiveConfig.getId(), dayActiveConfig);
//		}
//		log.info("存储DayActive成功，数据量：{}",DAY_ACTIVE_MAP.size());
//	}
//
//	public void storeWeekActive(List<WeekActiveConfig> list) {
//		for (WeekActiveConfig weekActiveConfig : list) {
//			String reward = weekActiveConfig.getReward();
//			if (!"0".equals(reward)) {
//				String[] split = reward.split(";");
//				Map<String,Object> dayRewards = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					dayRewards.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(dayRewards);
//				weekActiveConfig.setRewards(jsonObject);
//			}
//			WEEK_ACTIVE_MAP.put(weekActiveConfig.getId(), weekActiveConfig);
//		}
//		log.info("存储WeekActive成功，数据量：{}",WEEK_ACTIVE_MAP.size());
//	}
//
//	public void storeFileCode(List<BilliardFileCodeConfig> list) {
//		for (BilliardFileCodeConfig fileCodeConfig : list) {
//			FILE_CODE_MAP.put(fileCodeConfig.getId(), fileCodeConfig);
//		}
//		log.info("存储FileCode成功，数据量：{}",FILE_CODE_MAP.size());
//	}

//	public void storeDayTask(List<DayTaskConfig> list) {
//		for (DayTaskConfig dayTaskConfig : list) {
//			//条件id
//			String conditionId = dayTaskConfig.getConditionId();
//			List<Integer> conditionIds = new ArrayList<>();
//			if (conditionId.contains(",")) {
//				String[] split = conditionId.split(",");
//				for (String s : split) {
//					conditionIds.add(Integer.valueOf(s));
//				}
//			} else {
//				conditionIds.add(Integer.valueOf(conditionId));
//			}
//			dayTaskConfig.setConditionIds(conditionIds);
//			//将奖励放到jsonobject中
//			String reward = dayTaskConfig.getReward();
//			if (!"0".equals(reward)) {
//				String[] split = reward.split(";");
//				Map<String,Object> rewardMap = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					rewardMap.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(rewardMap);
//				dayTaskConfig.setRewards(jsonObject);
//			}
//			DAY_TASK_MAP.put(dayTaskConfig.getTaskId(), dayTaskConfig);
//		}
//		log.info("存储DayTask成功，数据量：{}",DAY_TASK_MAP.size());
//	}
//
//	public void storeWeekTask(List<WeekTaskConfig> list) {
//		for (WeekTaskConfig weekTaskConfig : list) {
//			//条件id
//			String conditionId = weekTaskConfig.getConditionId();
//			List<Integer> conditionIds = new ArrayList<>();
//			if (conditionId.contains(",")) {
//				String[] split = conditionId.split(",");
//				for (String s : split) {
//					conditionIds.add(Integer.valueOf(s));
//				}
//			} else {
//				conditionIds.add(Integer.valueOf(conditionId));
//			}
//			weekTaskConfig.setConditionIds(conditionIds);
//			//将奖励放到jsonobject中
//			String reward = weekTaskConfig.getReward();
//			if (!"0".equals(reward)) {
//				String[] split = reward.split(";");
//				Map<String,Object> rewardMap = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					rewardMap.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(rewardMap);
//				weekTaskConfig.setRewards(jsonObject);
//			}
//			WEEK_TASK_MAP.put(weekTaskConfig.getTaskId(), weekTaskConfig);
//		}
//		log.info("存储WeekTask成功，数据量：{}",WEEK_TASK_MAP.size());
//	}
//
//	public void storeGrewUpTask(List<GrowthTaskConfig> list) {
//		for (GrowthTaskConfig growthTaskConfig : list) {
//			//条件id
//			String conditionId = growthTaskConfig.getConditionId();
//			List<Integer> conditionIds = new ArrayList<>();
//			if (conditionId.contains(",")) {
//				String[] split = conditionId.split(",");
//				for (String s : split) {
//					conditionIds.add(Integer.valueOf(s));
//				}
//			} else {
//				conditionIds.add(Integer.valueOf(conditionId));
//			}
//			growthTaskConfig.setConditionIds(conditionIds);
//			//将奖励放到jsonobject中
//			String reward = growthTaskConfig.getReward();
//			if (!"0".equals(reward)) {
//				String[] split = reward.split(";");
//				Map<String,Object> rewardMap = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					rewardMap.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(rewardMap);
//				growthTaskConfig.setRewards(jsonObject);
//			}
//			GREW_UP_TASK_MAP.put(growthTaskConfig.getTaskId(), growthTaskConfig);
//		}
//		log.info("存储GrewUpTask成功，数据量：{}",GREW_UP_TASK_MAP.size());
//	}

//	public void storePlayerCue(List<BilliardCueConfig> list) {
//		for (BilliardCueConfig cueConfig : list) {
//			String upgrade = cueConfig.getUpgrade();
//			if (!"0".equals(upgrade)) {
//				String[] split = upgrade.split(",");
//				cueConfig.setUpgradePayType(Integer.parseInt(split[0]));
//				cueConfig.setUpgradePayPrice(Integer.parseInt(split[1]));
//			}
//			String sellPrice = cueConfig.getSellPrice();
//			if (!"0".equals(sellPrice)) {
//				String[] split = sellPrice.split(",");
//				cueConfig.setSellPayType(Integer.parseInt(split[0]));
//				cueConfig.setSellPayPrice(Integer.parseInt(split[1]));
//			}
//			CUE_MAP.put(cueConfig.getId(), cueConfig);
//		}
//		log.info("存储CueData成功，数据量：{}",CUE_MAP.size());
//	}

//	public void storePlayerCueType(List<BilliardCueTypeConfig> list) {
//		for(BilliardCueTypeConfig cueTypeConfig :list) {
//			String price = cueTypeConfig.getPrice();
//			if (!"0".equals(price)) {
//				String[] split = price.split(",");
//				cueTypeConfig.setBuyType(Integer.parseInt(split[0]));
//				cueTypeConfig.setBuyPrice(Integer.parseInt(split[1]));
//			}
//			String defend_30_times = cueTypeConfig.getDefend_30_times();
//			if (!"0".equals(defend_30_times)) {
//				String[] split = defend_30_times.split(",");
//				cueTypeConfig.setDefend_30_times_type(Integer.parseInt(split[0]));
//				cueTypeConfig.setDefend_30_times_price(Integer.parseInt(split[1]));
//			}
//			String defend_3_days = cueTypeConfig.getDefend_3_days();
//			if (!"0".equals(defend_3_days)) {
//				String[] split = defend_3_days.split(",");
//				cueTypeConfig.setDefend_3_days_type(Integer.parseInt(split[0]));
//				cueTypeConfig.setDefend_3_days_price(Integer.parseInt(split[1]));
//			}
//			String defend_7_days = cueTypeConfig.getDefend_7_days();
//			if (!"0".equals(defend_7_days)) {
//				String[] split = defend_7_days.split(",");
//				cueTypeConfig.setDefend_7_days_type(Integer.parseInt(split[0]));
//				cueTypeConfig.setDefend_7_days_price(Integer.parseInt(split[1]));
//			}
//			String defend_30_days = cueTypeConfig.getDefend_30_days();
//			if (!"0".equals(defend_30_days)) {
//				String[] split = defend_30_days.split(",");
//				cueTypeConfig.setDefend_30_days_type(Integer.parseInt(split[0]));
//				cueTypeConfig.setDefend_30_days_price(Integer.parseInt(split[1]));
//			}
//			String defend_365_days = cueTypeConfig.getDefend_365_days();
//			if (!"0".equals(defend_365_days)) {
//				String[] split = defend_365_days.split(",");
//				cueTypeConfig.setDefend_365_days_type(Integer.parseInt(split[0]));
//				cueTypeConfig.setDefend_365_days_price(Integer.parseInt(split[1]));
//			}
//			CUE_TYPE_MAP.put(cueTypeConfig.getId(), cueTypeConfig);
//		}
//		log.info("存储CueType成功，数据量：{}",CUE_TYPE_MAP.size());
//	}

//	private void storeSign(List<EveryDaySign> dataList) {
//		for (EveryDaySign everyDay : dataList) {
//			//将奖励放到jsonobject中
//			String reward = everyDay.getItemNum();
//			if (!"0".equals(reward)) {
//				String[] split = reward.split("_");
//				Map<String,Object> rewardMap = new HashMap<>();
//				for (String s : split) {
//					String[] split1 = s.split(",");
//					rewardMap.put(split1[0],split1[1]);
//				}
//				JSONObject jsonObject = new JSONObject(rewardMap);
//				everyDay.setItemNums(jsonObject);
//			}
//			SIGN_MAP.put(everyDay.getId(), everyDay);
//		}
//	}


}
