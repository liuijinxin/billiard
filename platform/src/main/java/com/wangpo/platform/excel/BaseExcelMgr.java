package com.wangpo.platform.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.wangpo.base.cms.*;
import com.wangpo.base.excel.*;
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
public class BaseExcelMgr {
	public static final Map<Integer, BilliardCueConfig> CUE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardCueTypeConfig> CUE_TYPE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, GrowthTaskConfig> GROWTH_TASK_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, DayTaskConfig> DAY_TASK_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, WeekTaskConfig> WEEK_TASK_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, DayActiveConfig> DAY_ACTIVE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, WeekActiveConfig> WEEK_ACTIVE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardFileCodeConfig> FILE_CODE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardRoleConfig> ROlE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, ItemConfig> ITEM_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardChangConfig> CHANG_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, MemberConfig> MEMBER_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, GlobalConfig> GLOBAL_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardLotteryConfig> AWARD_POOL_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, GoodsConfig> GOODS_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, EveryDaySign> SIGN_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, BilliardLuckyCueConfig> LUCKY_CUE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, ShopConfig> SHOP_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, ActivityConfig> ACTIVITY_MAP = new ConcurrentHashMap<>();

	//后台配置
	public static final Map<Integer, SystemConfig> SYSTEM_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, APPVersion> APP_VERSION_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, ChannelConfig> CHANNEL_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, ResourceConfig> RESOURCE_CONFIG_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, Notice> NOTICE_MAP = new ConcurrentHashMap<>();
	public static final Map<Integer, CmsSystemNotice> SYSTEM_NOTICE_MAP = new ConcurrentHashMap<>();


	//文件最后修改日期
	private static final Map<String, Long> fileModifyTimeMap = new ConcurrentHashMap<>();
	//文件对应的类
	private static final Map<String, Class[]> classMap = new HashMap<>();

	private static final String PREFIX;


	static {
		String path3 = System.getProperty("user.dir");
		PREFIX = path3 + File.separator + "excel" + File.separator;
		Class[] activity = {ActivityConfig.class};
		classMap.put("活动配置表", activity);
		Class[] cue = {BilliardCueTypeConfig.class, BilliardCueConfig.class};
		classMap.put("球杆配置", cue);
		Class[] task = {DayTaskConfig.class, WeekTaskConfig.class, GrowthTaskConfig.class, DayActiveConfig.class, WeekActiveConfig.class, BilliardFileCodeConfig.class};
		classMap.put("任务系统", task);
		Class[] role = {BilliardRoleConfig.class};
		classMap.put("角色等级", role);
		Class[] item = {ItemConfig.class};
		classMap.put("道具列表", item);
		Class[] chang = {BilliardChangConfig.class};
		classMap.put("场次配置", chang);
		Class[] vip = {MemberConfig.class};
		classMap.put("VIP配置", vip);
		Class[] global = {GlobalConfig.class};
		classMap.put("全局配置表", global);
		Class[] lottery = {BilliardLotteryConfig.class};
		classMap.put("奖池配置表", lottery);
		Class[] luckyCue = {BilliardLuckyCueConfig.class};
		classMap.put("幸运一杆配置表", luckyCue);
		Class[] goods = {GoodsConfig.class};
		classMap.put("商品配置表", goods);
		Class[] sign = {EveryDaySign.class};
		classMap.put("新手签到表", sign);
	}

	//读取所有的配置文件
	public void readExcel() throws IOException {
		for (Map.Entry<String, Class[]> entry : classMap.entrySet()) {
			readExcel(entry.getKey(), entry.getValue());
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

//	/**
//	 * 读取excel表，单独抽出来，方便做热更新
//	 * 热更新调用方式：
//	 * <pre> {@code
//	 *  excelMgr.readExcel(global,Global.class)
//	 * }</pre>
//	 *
//	 * @param name  表名
//	 * @param clazz 表对应类名
//	 * @return
//	 * @throws IOException
//	 */
//	public void readExcel(String name, Class clazz) throws IOException {
//		File file = new File(PREFIX + name + ".xlsx");
//		//存储文件的最后修改日期，方便热加载
//		fileModifyTimeMap.put(name, file.lastModified());
//		InputStream inputStream = new FileInputStream(file);
//		DataListener listener = new DataListener();
//		EasyExcel.read(inputStream, clazz, listener).sheet().doRead();
//		List dataList = listener.list();
//		store(name, dataList);
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
			store2map(GROWTH_TASK_MAP, dataList);
		} else if (DayTaskConfig.class.equals(name)) {
			//每日任务
			store2map(DAY_TASK_MAP, dataList);
		} else if (WeekTaskConfig.class.equals(name)) {
			//每周任务
			store2map(WEEK_TASK_MAP, dataList);
		} else if (DayActiveConfig.class.equals(name)) {
			//日活跃
			store2map(DAY_ACTIVE_MAP, dataList);
		} else if (WeekActiveConfig.class.equals(name)) {
			//周活跃
			store2map(WEEK_ACTIVE_MAP, dataList);
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
			store2map(MEMBER_MAP, dataList);
		}  else if (GlobalConfig.class.equals(name)) {
			//全局配置
			store2map(GLOBAL_MAP, dataList);
		} else if (BilliardLotteryConfig.class.equals(name)) {
			//台球抽奖奖池配置
			store2map(AWARD_POOL_MAP, dataList);
		} else if (GoodsConfig.class.equals(name)) {
			//商城配置
			store2map(GOODS_MAP, dataList);
		} else if (BilliardLuckyCueConfig.class.equals(name)) {
			//台球幸运一杆配置
			store2map(LUCKY_CUE_MAP, dataList);
		} else if (EveryDaySign.class.equals(name)) {
			//每日签到配置
			store2map(SIGN_MAP, dataList);
		}else if (ActivityConfig.class.equals(name)) {
			//活动配置
			store2map(ACTIVITY_MAP, dataList);
		}
	}

	public void store2map(Map map, List list) {
		for (Object o : list) {
			IConfig c = (IConfig) o;
			try{
				c.explain();
			}catch (Exception e) {
				log.error("配置解析异常，o:{}",o);
				e.printStackTrace();
				System.exit(1);
			}
			map.put(c.getId(), c);
		}
		log.info("配置数量：{}",list.size());
	}

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

	/**
	 * 自动热更新，此方法，需要quartz调用
	 */
	public void hotReload() {
		File file = new File(PREFIX);
		int ok = 0;
		if (file.isDirectory()) {
			String[] files = file.list();
			for (String s : files) {
				File f = new File(PREFIX + s);
				String name = f.getName().substring(0, f.getName().length() - 5);
				if (!fileModifyTimeMap.containsKey(name)) continue;
				long t = fileModifyTimeMap.get(name);
				if (f.lastModified() <= t) continue;
				if (f.isFile() && f.getName().endsWith(".xlsx")) {
					//判断xlsx文件是否有更新
					Class[] clazz = classMap.get(name);
					if (clazz != null) {
						try {
							readExcel(name, clazz);
							ok++;
						} catch (IOException e) {
							log.error("热更新配置文件 {} 异常：", name, e);
						}
					}
				}
			}
		}
		if (ok > 0)
			log.error("热加载完成，更新文件个数：{}", ok);
	}

	//	public Map<Integer, BilliardLotteryConfig> getAwardPoolMap() {
//		return AWARD_POOL_MAP;
//	}
//
	public Map<Integer, BilliardChangConfig> getChangConfigMap() {
		return CHANG_CONFIG_MAP;
	}
//	public Map<Integer, BilliardRoleConfig> getROlE_MAP() {
//		return ROlE_MAP;
//	}
//	public BilliardRoleConfig getRoleById(int id) {
//		return ROlE_MAP.get(id);
//	}
//	public Map<Integer, ItemConfig> getItemMap() {
//		return ITEM_MAP;
//	}
//	public Map<Integer, DayTaskConfig> getDayTaskMap() {
//		return DAY_TASK_MAP;
//	}

//	public Map<Integer, DayActiveConfig> getDayActiveMap() {
//		return DAY_ACTIVE_MAP;
//	}

//	public Map<Integer, WeekActiveConfig> getWeekActiveMap() {
//		return WEEK_ACTIVE_MAP;
//	}

//	public Map<Integer, BilliardFileCodeConfig> getFileCodeMap() {
//		return FILE_CODE_MAP;
//	}

//	public Map<Integer, WeekTaskConfig> getWeekTaskMap() {
//		return WEEK_TASK_MAP;
//	}
//
//	public Map<Integer, GrowthTaskConfig> getGrewUpTaskMap() {
//		return GROWTH_TASK_MAP;
//	}
//
	public Map<Integer, MemberConfig> getMemberMap() {
		return MEMBER_MAP;
	}

//	public MemberConfig getMemberMapById(int id) {
//		return MEMBER_MAP.get(id);
//	}

	//	public Map<Integer, GlobalConfig> getGlobal() {
//		return GLOBAL_MAP;
//	}
//	public GlobalConfig getGlobal(int id) {
//		if (!GLOBAL_MAP.containsKey(id)) return null;
//		return GLOBAL_MAP.get(id);
//	}

//	public Map<Integer, GoodsConfig> getGoodsMap() {
//		return GOODS_MAP;
//	}

	public Map<Integer, EveryDaySign> getSign() {
		return SIGN_MAP;
	}

	public Map<Integer, SystemConfig> getSystemConfigMap() {
		return SYSTEM_CONFIG_MAP;
	}

	public Map<Integer, ShopConfig> getShopConfigMap() {
		return SHOP_CONFIG_MAP;
	}

	public Map<Integer, APPVersion> getAppVersionMap() {
		return APP_VERSION_MAP;
	}

	public Map<Integer, ChannelConfig> getChannelConfigMap() {
		return CHANNEL_CONFIG_MAP;
	}

	public Map<Integer, ResourceConfig> getResourceConfigMap() {
		return RESOURCE_CONFIG_MAP;
	}

	public Map<Integer, Notice> getNoticeMap() {
		return NOTICE_MAP;
	}

	public Map<Integer, CmsSystemNotice> getSystemNoticeMap() {
		return SYSTEM_NOTICE_MAP;
	}
	
	public Map<Integer, ActivityConfig> getActivityMap() {
		return ACTIVITY_MAP;
	}



}
