package com.wangpo.platform.logic.task;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.kits.FormatKit;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.service.BilliardService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.bean.*;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.enums.task.BilliardTaskType;
import com.wangpo.base.enums.task.TaskType;
import com.wangpo.base.excel.*;
import com.wangpo.platform.bean.Player;
import com.wangpo.base.bean.TaskCondition;
import com.wangpo.platform.dao.TaskEntity;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.PlayerService;
import com.wangpo.platform.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class TaskHandler {
	@Resource
	private TaskService taskService;
	@Resource
	private BaseExcelMgr baseExcelMgr;
	@DubboReference
	BilliardPushService billiardPushService;
	@DubboReference
	BilliardService billiardService;
	@Resource
	PlatformService platformService;
	@Resource
	PlayerMgr playerMgr;
	@Resource
	ItemMgr itemMgr;
	@Resource
	TaskMgr taskMgr;
	@Resource
	PlayerService playerService;

	/**
	 * 获取任务
	 */
	public S2C getTask(C2S c2s) throws Exception{
		PlatFormProto.C2S_GetTask proto = PlatFormProto.C2S_GetTask.parseFrom(c2s.getBody());
		int taskType = proto.getTaskType();//1每日任务，2每周任务，3成长任务
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.GET_TASK);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		if (player == null) {
			log.error("玩家不存在");
			s2c.setCode(1);
			return s2c;
		}
		//查找玩家的任务,将玩家任务封装到protobuf里
		taskMgr.getTask(s2c,player,taskType);
		return s2c;
	}

	/**
	 * 分享
	 */
	public S2C share(C2S c2s) {
		int uid = c2s.getUid();
		TaskData taskData = new TaskData();
		taskData.setConditionId(1000);
		taskData.setTimes(1);
		taskData.setTaskType(BilliardTaskType.SHARE.code);
		List<Task> taskList = finishTask(uid, 3, taskData);
		S2C s2c = new S2C();
		s2c.setCid(Cmd.SHARE);
		s2c.setUid(uid);
//		for (Task task : taskList) {
//			PlatFormProto.PlayerTask.Builder proto = taskMgr.Task2Proto(task);
//			PlatFormProto.S2C_UpdateTask.Builder builder = PlatFormProto.S2C_UpdateTask.newBuilder();
//			builder.setId(uid);
//			builder.setTask(proto.build());
//		}
		//s2c.setBody(builder.build().toByteArray());
		return s2c;
	}

	/**
	 * 完成任务
	 * @return 有变化的任务列表
	 */
	public List<Task> finishTask(int uid, int sid, TaskData taskData) {
		List<Task> taskList = taskMgr.finishTask(uid, sid, taskData);
		for (Task task : taskList) {
			TaskEntity taskEntity = taskMgr.task2TaskEntity(task);
			taskService.updateTask(taskEntity);
			if(task.getTaskStatus() == 1 ) {
				PlatFormProto.S2C_UpdateTask.Builder b = PlatFormProto.S2C_UpdateTask.newBuilder();
				b.setId(uid);
				b.setTask(taskMgr.Task2Proto(task));
				S2C s2c = new S2C();
				s2c.setUid(uid);
				s2c.setCid(Cmd.UPDATE_TASK);
				s2c.setBody(b.build().toByteArray());
				billiardPushService.push(s2c);
			}
		}
		return taskList;
	}

	/**
	 * 查询所有任务
	 * @param playerId 玩家id
	 * @return 玩家任务列表
	 */
	public List<Task> queryAllTask(Integer playerId) {
		//玩家任务列表
		List<TaskEntity> taskEntities = taskService.selectAllTask(playerId);
		List<Task> taskList = new ArrayList<>();
		taskMgr.queryAllTask(playerId, taskEntities);
		for (TaskEntity entity : taskEntities) {
			//如果是已经领取的成长任务，则不放进去
			if (entity.getTaskType() == TaskType.GROWING.code && entity.getTaskStatus() == 2) {
				continue;
			}
			taskList.add(taskMgr.taskEntity2Task(entity));
		}
		return taskList;
	}

	/**
	 * 领取任务奖励
	 */
	public S2C getTaskReward(C2S c2s) throws Exception {
		PlatFormProto.C2S_GetTaskReward proto = PlatFormProto.C2S_GetTaskReward.parseFrom(c2s.getBody());
		int id = proto.getId();
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.GET_TASK_REWARD);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		if (player == null) {
			log.error("玩家不存在");
			s2c.setCode(1);
			return s2c;
		}
		List<Task> taskList = player.getTaskList();

		Task task = new Task();
		//遍历玩家任务列表，找到指定任务
		for (Task task1 : taskList) {
			if (task1.getId() == id) {
				task = task1;
				break;
			}
		}
		if (task.getTaskStatus() == 2) {
			log.error("奖励已领取");
			s2c.setCode(2);
			return s2c;
		}
		int taskType = task.getTaskType();
		int taskId = task.getTaskId();
		task.setTaskStatus(2);
		taskService.updateTask(taskMgr.task2TaskEntity(task));
		PlatFormProto.S2C_Award.Builder builder = PlatFormProto.S2C_Award.newBuilder();
		builder.setId(id);
		builder.setTaskType(taskType);
		//存放任务的奖励
		JSONObject jsonObject = null;
		if (taskType == TaskType.DAY.code) {
			//每日任务
			DayTaskConfig dayTaskConfig = BaseExcelMgr.DAY_TASK_MAP.get(taskId);
			jsonObject = dayTaskConfig.getRewards();
			int dayActive = player.getDayActive() + dayTaskConfig.getDayActive();
			player.setDayActive(dayActive);
			builder.setActive(dayActive);
		} else if (taskType == TaskType.WEEK.code) {
			//每周任务
			WeekTaskConfig weekTaskConfig = BaseExcelMgr.WEEK_TASK_MAP.get(taskId);
			jsonObject = weekTaskConfig.getRewards();
			int weekActive = player.getWeekActive() + weekTaskConfig.getWeekActive();
			player.setWeekActive(weekActive);
			builder.setActive(weekActive);
		} else if (taskType == TaskType.GROWING.code) {
			//成长任务
			player.getTaskList().remove(task);
			GrowthTaskConfig growthTaskConfig = BaseExcelMgr.GROWTH_TASK_MAP.get(taskId);
			int afterTaskId = growthTaskConfig.getAfterTaskId();
			GrowthTaskConfig growthTaskConfig1 = BaseExcelMgr.GROWTH_TASK_MAP.get(afterTaskId);
			if (growthTaskConfig1 != null) {
				TaskEntity taskEntity = taskMgr.grewUpTask2TaskEntity(growthTaskConfig1);
				List<TaskCondition> conditions = taskEntity.getConditionIds();
				//判断满足条件的次数
				int finishCount = 0;
				for (TaskCondition condition : conditions) {
					for (TaskCondition taskCondition : task.getConditionIds()) {
						if (taskCondition.getConditionId() == condition.getConditionId()) {
							//将之前成长任务的进度放到后置成长任务的进度中
							condition.setCurrentProgress(taskCondition.getCurrentProgress());
						}
					}
					if (condition.getCurrentProgress() >= condition.getTotalProgress()) {
						finishCount++;
					}
				}
				if (finishCount == conditions.size()) {
					taskEntity.setTaskStatus(1);
				}
				taskEntity.setTaskType(TaskType.GROWING.code);
				taskEntity.setPlayerId(uid);
				taskService.insertTask(taskEntity);
				Task task1 = taskMgr.taskEntity2Task(taskEntity);
				//将新的成长任务放到玩家任务列表中
				player.getTaskList().add(task1);
				//推送新任务给客户端
				taskMgr.pushNewTask(uid, task1);
			}
			jsonObject = growthTaskConfig.getRewards();
		}
		if (jsonObject != null) {
			for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
				int type = Integer.parseInt(entry.getKey());
				int price = Integer.parseInt(entry.getValue().toString());
				//桌球服，道具让桌球服处理
				itemMgr.addItem(player,type,price, GameEventEnum.TASK_REWARD);
				PlatFormProto.Award.Builder award = PlatFormProto.Award.newBuilder();
				award.setId(type).setNum(price);
				builder.addAwards(award.build());
			}
		}
		s2c.setBody(builder.build().toByteArray());
		return s2c;
	}

	/**
	 * 领取活跃度奖励
	 */
	public S2C getActiveReward(C2S c2s) throws Exception {
		PlatFormProto.C2S_GetActiveReward reward = PlatFormProto.C2S_GetActiveReward.parseFrom(c2s.getBody());
		int uid = c2s.getUid();
		int taskType = reward.getTaskType();
		int active = reward.getActive();
		int activeStatus = 0;
		S2C s2c = new S2C();
		s2c.setCid(Cmd.GET_ACTIVE_REWARD);
		s2c.setUid(uid);
		Player player = playerMgr.getPlayerByID(uid);
		if (player == null) {
			log.error("玩家不存在");
			s2c.setCode(1);
			return s2c;
		}
		//存放活跃度奖励
		JSONObject jsonObject = null;
		if (taskType == TaskType.DAY.code) {
			Map<Integer, DayActiveConfig> dayActiveMap = BaseExcelMgr.DAY_ACTIVE_MAP;
			for (DayActiveConfig dayActiveConfig : dayActiveMap.values()) {
				//根据里程碑找到对应的活跃度奖励
				if (dayActiveConfig.getMilepost() == active) {
					jsonObject = dayActiveConfig.getRewards();
					//存放领取活跃度奖励状态
					int dayActiveStatus = player.getDayActiveStatus();
					//判断里程碑奖励是否已领取
					if ((dayActiveStatus >> dayActiveConfig.getId() & 1) == 1) {
						log.error("奖励已领取");
						s2c.setCode(2);
						return s2c;
					}
					int status = 1 << dayActiveConfig.getId();
					activeStatus = dayActiveStatus + status;
					player.setDayActiveStatus(activeStatus);
				}
			}
		} else if (taskType == TaskType.WEEK.code) {
			Map<Integer, WeekActiveConfig> weekActiveMap = BaseExcelMgr.WEEK_ACTIVE_MAP;
			for (WeekActiveConfig weekActiveConfig : weekActiveMap.values()) {
				//根据里程碑找到对应的活跃度奖励
				if (weekActiveConfig.getMilepost() == active) {
					jsonObject = weekActiveConfig.getRewards();
					//存放领取活跃度奖励状态
					int weekActiveStatus = player.getWeekActiveStatus();
					//判断里程碑奖励是否已领取
					if ((weekActiveStatus >> weekActiveConfig.getId() & 1) == 1) {
						log.error("奖励已领取");
						s2c.setCode(2);
						return s2c;
					}
					int status = 1 << weekActiveConfig.getId();
					activeStatus = weekActiveStatus + status;
					player.setWeekActiveStatus(activeStatus);
				}
			}
		}
		PlatFormProto.S2C_ActiveAward.Builder builder = PlatFormProto.S2C_ActiveAward.newBuilder();
		builder.setActiveStatus(activeStatus);
		builder.setTaskType(taskType);
		builder.setActive(active);
		if (jsonObject != null) {
			for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
				int type = Integer.parseInt(entry.getKey());
				int price = Integer.parseInt(entry.getValue().toString());
				//桌球服，道具让桌球服处理
				itemMgr.addItem(player,type,price, GameEventEnum.TASK_REWARD);
				PlatFormProto.Award.Builder award = PlatFormProto.Award.newBuilder();
				award.setId(type).setNum(price);
				builder.addAwards(award.build());
			}
		}
		s2c.setBody(builder.build().toByteArray());
		return s2c;
	}


	/**
	 * 重置玩家任务
	 * @param player 玩家
	 * @param taskType 任务类型
	 */
	public void resetPlayerTask(Player player, int taskType) {
		//重置任务进度
		List<Task> taskList = player.getTaskList();
		if (taskList != null) {
			for (Task task : taskList) {
				if (task.getTaskType() == taskType) {
					List<TaskCondition> conditionIds = task.getConditionIds();
					for (TaskCondition taskCondition : conditionIds) {
						taskCondition.setCurrentProgress(0);
					}
					if (task.getTaskStatus() == 1 || task.getTaskStatus() == 2) {
						task.setTaskStatus(0);
					}
					taskService.updateTask(taskMgr.task2TaskEntity(task));
				}
			}
		}
		//重置活跃度
		if (taskType == TaskType.DAY.code) {
			player.setLastDay(FormatKit.today10());
			player.setDayActive(0);
			player.setDayActiveStatus(0);
			playerService.updatePlayer(player);
		} else if (taskType == TaskType.WEEK.code) {
			player.setLastMonday(FormatKit.lastMonday());
			player.setWeekActive(0);
			player.setWeekActiveStatus(0);
			playerService.updatePlayer(player);
		}
	}

}
