package com.wangpo.platform.logic.task;

import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.*;
import com.wangpo.base.enums.task.BilliardTaskType;
import com.wangpo.base.enums.task.TaskType;
import com.wangpo.base.excel.DayTaskConfig;
import com.wangpo.base.excel.GrowthTaskConfig;
import com.wangpo.base.excel.WeekTaskConfig;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.dao.TaskEntity;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.TaskService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TaskMgr {
    @Resource
    PlayerMgr playerMgr;
    @Resource
    BaseExcelMgr baseExcelMgr;
    @Resource
    TaskService taskService;
    @DubboReference
    BilliardPushService billiardPushService;

    /**
     * 获取任务
     * @param s2c s2c
     * @param player 玩家
     * @param taskType 任务类型
     * @return s2c
     */
    public void getTask(S2C s2c, Player player,int taskType) {
        List<Task> taskList = player.getTaskList();
        List<Task> tasks = new ArrayList<>();
        if (taskList != null) {
            taskList.forEach(task -> {
//                if (taskType == task.getTaskType()) {
//                    tasks.add(task);
//                }
                if (taskType == TaskType.DAY.code) {
                    Map<Integer, DayTaskConfig> dayTaskMap = BaseExcelMgr.DAY_TASK_MAP;
                    if (dayTaskMap.containsKey(task.getTaskId())) {
                        tasks.add(task);
                    }
                } else if (taskType == TaskType.WEEK.code) {
                    Map<Integer, WeekTaskConfig> weekTaskMap = BaseExcelMgr.WEEK_TASK_MAP;
                    if (weekTaskMap.containsKey(task.getTaskId())) {
                        tasks.add(task);
                    }
                } else if (taskType == TaskType.GROWING.code) {
                    Map<Integer, GrowthTaskConfig> growthTaskMap = BaseExcelMgr.GROWTH_TASK_MAP;
                    if (growthTaskMap.containsKey(task.getTaskId())) {
                        tasks.add(task);
                    }
                }
            });
        }
        PlatFormProto.S2C_Task.Builder builder = PlatFormProto.S2C_Task.newBuilder();
        for (Task task : tasks) {
            PlatFormProto.PlayerTask.Builder playerTask = Task2Proto(task);
            builder.addTasks(playerTask.build());
        }
        if (taskType == 1) {
            builder.setActive(player.getDayActive());
            builder.setActiveStatus(player.getDayActiveStatus());
        } else if (taskType == 2) {
            builder.setActive(player.getWeekActive());
            builder.setActiveStatus(player.getWeekActiveStatus());
        }
        builder.setId(player.getId());
        s2c.setBody(builder.build().toByteArray());
    }

    /**
     * 查找玩家的所有任务
     * @param playerId 玩家id
     * @param taskEntities 任务列表
     */
    public void queryAllTask(Integer playerId, List<TaskEntity> taskEntities) {
        //查询每日任务
        Map<Integer, DayTaskConfig> dayTaskMap = BaseExcelMgr.DAY_TASK_MAP;
        if (dayTaskMap.size() > 0) {
            dayTaskMap.forEach((taskId, dayTaskConfig) -> {
                //如果玩家列表中没有该任务，则新增任务
                boolean match = taskEntities.stream().anyMatch(taskEntity -> taskEntity.getTaskId() == taskId && taskEntity.getTaskType() == TaskType.DAY.code);
                if (!match) {
                    dayTaskConfig.setCurrentProgress(0);
                    TaskEntity taskEntity = dayTask2TaskEntity(dayTaskConfig);
                    initNewTask(playerId, taskEntity, TaskType.DAY.code);
                    taskService.insertTask(taskEntity);
                    taskEntities.add(taskEntity);
                } else {
                    //判断任务的场次条件和总进度是否修改
                    Optional<TaskEntity> first = taskEntities.stream().filter(taskEntity -> taskEntity.getTaskId() == taskId && taskEntity.getTaskType() == TaskType.DAY.code).findFirst();
                    if (first.isPresent()) {
                        TaskEntity taskEntity = first.get();
                        TaskCondition taskCondition = taskEntity.getConditionIds().get(0);
                        int conditionId = Integer.parseInt(dayTaskConfig.getConditionId());
                        int totalProgress = dayTaskConfig.getTotalProgress();
                        if (conditionId != taskCondition.getConditionId() || totalProgress != taskCondition.getTotalProgress()) {
                            //修改为最新的场次条件和总进度
                            taskCondition.setConditionId(conditionId);
                            taskCondition.setTotalProgress(totalProgress);
                            if (taskCondition.getCurrentProgress() < totalProgress && taskEntity.getTaskStatus() == 1) {
                                taskEntity.setTaskStatus(0);
                            }
                        }
                    }
                }
            });
        }
        //查询每周任务
        Map<Integer, WeekTaskConfig> weekTaskMap = BaseExcelMgr.WEEK_TASK_MAP;
        if (weekTaskMap.size() > 0) {
            weekTaskMap.forEach((taskId, weekTaskConfig) -> {
                //如果玩家列表中没有该任务，则新增任务
                boolean match = taskEntities.stream().anyMatch(taskEntity -> taskEntity.getTaskId() == taskId && taskEntity.getTaskType() == TaskType.WEEK.code);
                if (!match) {
                    weekTaskConfig.setCurrentProgress(0);
                    TaskEntity taskEntity = weekTask2TaskEntity(weekTaskConfig);
                    initNewTask(playerId, taskEntity, TaskType.WEEK.code);
                    taskService.insertTask(taskEntity);
                    taskEntities.add(taskEntity);
                } else {
                    //判断任务的场次条件和总进度是否修改
                    Optional<TaskEntity> first = taskEntities.stream().filter(taskEntity -> taskEntity.getTaskId() == taskId && taskEntity.getTaskType() == TaskType.WEEK.code).findFirst();
                    if (first.isPresent()) {
                        TaskEntity taskEntity = first.get();
                        TaskCondition taskCondition = taskEntity.getConditionIds().get(0);
                        int conditionId = Integer.parseInt(weekTaskConfig.getConditionId());
                        int totalProgress = weekTaskConfig.getTotalProgress();
                        if (conditionId != taskCondition.getConditionId() || totalProgress != taskCondition.getTotalProgress()) {
                            //修改为最新的场次条件和总进度
                            taskCondition.setConditionId(conditionId);
                            taskCondition.setTotalProgress(totalProgress);
                            if (taskCondition.getCurrentProgress() < totalProgress && taskEntity.getTaskStatus() == 1) {
                                taskEntity.setTaskStatus(0);
                            }
                        }
                    }
                }
            });
        }
        //查询成长任务
        Map<Integer, GrowthTaskConfig> grewUpTaskMap = BaseExcelMgr.GROWTH_TASK_MAP;
        if (grewUpTaskMap.size() > 0) {
            //查看当前成长任务列表，匹配成长任务的初始编号，若无该任务，则新增任务
            Set<Integer> taskIdSet = new TreeSet<>();
            grewUpTaskMap.forEach((taskId,grewUpTask) -> {
                taskIdSet.add(1000 + (taskId%100/10*10 +1));
            });
            taskIdSet.forEach(taskId -> {
                //判断玩家是否有匹配的成长任务，没有则新增
                boolean match = taskEntities.stream().anyMatch(taskEntity -> taskEntity.getTaskId() == taskId && taskEntity.getTaskType() == TaskType.GROWING.code);
                if (!match) {
                    GrowthTaskConfig growthTaskConfig = grewUpTaskMap.get(taskId);
                    growthTaskConfig.setCurrentProgress(0);
                    TaskEntity taskEntity = grewUpTask2TaskEntity(growthTaskConfig);
                    initNewTask(playerId, taskEntity, TaskType.GROWING.code);
                    taskService.insertTask(taskEntity);
                    taskEntities.add(taskEntity);
                }
            });
        }
    }

    /**
     * 初始化新任务状态
     * @param playerId 玩家id
     * @param taskEntity 任务
     * @param taskType 任务类型
     */
    private void initNewTask(Integer playerId, TaskEntity taskEntity, int taskType) {
        taskEntity.setTaskType(taskType);
        taskEntity.setPlayerId(playerId);
        List<TaskCondition> conditions = taskEntity.getConditionIds();
        //判断满足条件的次数
        int finishCount = 0;
        for (TaskCondition condition : conditions) {
            if (condition.getCurrentProgress() >= condition.getTotalProgress()) {
                finishCount++;
            }
        }
        if (finishCount == conditions.size()) {
            taskEntity.setTaskStatus(1);
        }
    }

    /**
     * 完成任务
     * @return 有变化的任务列表
     */
    public List<Task> finishTask(int uid, int sid, TaskData taskData) {
        Player player = playerMgr.getPlayerByID(uid);
        List<Task> taskList = new ArrayList<>();
        if( player != null ) {
            //玩家的任务列表
            List<Task> playerTaskList = player.getTaskList();
            for(Task task :playerTaskList) {
                int taskType = task.getTaskType();
                if( taskType == TaskType.GROWING.code) {//成长任务
                    GrowthTaskConfig growthTaskConfig = BaseExcelMgr.GROWTH_TASK_MAP.get(task.getTaskId());
                    //如果是充值或者签到，直接加进度
                    if (taskData.getTaskType() == BilliardTaskType.SIGN.code || taskData.getTaskType() == BilliardTaskType.RECHARGE.code) {
                        if( growthTaskConfig.getGameType()==sid && task.getTaskStatus() != 2 && growthTaskConfig.getTaskType()==taskData.getTaskType()){
                            signProgress(taskData, taskList, task);
                        }
                    } else {
                        //赢球要同时加赢比赛和任意比赛
                        boolean flag = (taskData.getTaskType() == BilliardTaskType.WIN.code && growthTaskConfig.getTaskType() == BilliardTaskType.GAME.code) || growthTaskConfig.getTaskType()==taskData.getTaskType();
                        //只判断桌球，已领取奖励的任务不判断
                        if( growthTaskConfig.getGameType()==sid && flag && task.getTaskStatus() != 2){
                            getTaskStatus(taskData, taskList, task);
                        }
                    }
                } else if (taskType == TaskType.WEEK.code ) {//每周任务
                    WeekTaskConfig weekTaskConfig = BaseExcelMgr.WEEK_TASK_MAP.get(task.getTaskId());
                    //如果是充值或者签到，直接加进度
                    if (taskData.getTaskType() == BilliardTaskType.SIGN.code || taskData.getTaskType() == BilliardTaskType.RECHARGE.code) {
                        if( weekTaskConfig.getGameType()==sid && task.getTaskStatus() != 2 && weekTaskConfig.getTaskType()==taskData.getTaskType()){
                            signProgress(taskData, taskList, task);
                        }
                    } else {
                        boolean flag = (taskData.getTaskType() == BilliardTaskType.WIN.code && weekTaskConfig.getTaskType() == BilliardTaskType.GAME.code) || weekTaskConfig.getTaskType()==taskData.getTaskType();
                        if( weekTaskConfig.getGameType()==sid && flag && task.getTaskStatus() != 2){
                            getTaskStatus(taskData, taskList, task);
                        }
                    }
                } else if (taskType == TaskType.DAY.code ) {//每日任务
                    DayTaskConfig dayTaskConfig = BaseExcelMgr.DAY_TASK_MAP.get(task.getTaskId());
                    //如果是充值或者签到，直接加进度
                    if (taskData.getTaskType() == BilliardTaskType.SIGN.code || taskData.getTaskType() == BilliardTaskType.RECHARGE.code) {
                        if( dayTaskConfig.getGameType()==sid && task.getTaskStatus() != 2 && dayTaskConfig.getTaskType()==taskData.getTaskType()){
                            signProgress(taskData, taskList, task);
                        }
                    } else {
                        boolean flag = (taskData.getTaskType() == BilliardTaskType.WIN.code && dayTaskConfig.getTaskType() == BilliardTaskType.GAME.code) || dayTaskConfig.getTaskType()==taskData.getTaskType();
                        if( dayTaskConfig.getGameType()==sid && flag && task.getTaskStatus() != 2){
                            getTaskStatus(taskData, taskList, task);
                        }
                    }
                }
            }
        }
        return taskList;
    }

    /**
     * 修改签到或者充值的进度
     * @param taskData 任务条件
     * @param taskList 存放任务的列表
     * @param task 任务
     */
    private void signProgress(TaskData taskData, List<Task> taskList, Task task) {
        List<TaskCondition> conditionList = task.getConditionIds();
        //判断满足条件的次数
        int finishCount = 0;
        for (TaskCondition taskCondition : conditionList) {
            finishCount = modifyProgress(taskData, finishCount, taskCondition);
            taskList.add(task);
        }
        if (finishCount == conditionList.size()) {
            task.setTaskStatus(1);
        }
    }

    /**
     * 修改任务状态
     * @param taskData 任务条件
     * @param taskList 存放任务列表
     * @param task 任务
     */
    private void getTaskStatus(TaskData taskData, List<Task> taskList, Task task) {
        List<TaskCondition> conditionList = task.getConditionIds();
        int conditionId = taskData.getConditionId();
        //判断满足条件的次数
        int finishCount = 0;
        for (TaskCondition taskCondition : conditionList) {
            if (taskCondition.getConditionId() == conditionId) {
                finishCount = modifyProgress(taskData, finishCount, taskCondition);
                taskList.add(task);
            } else if (taskCondition.getConditionId() == 9999) {//任意场次
                if (conditionId < 3000) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            } else if (taskCondition.getConditionId() == 1999) {//金币任意
                if (conditionId/1000 == 1) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            } else if (taskCondition.getConditionId() == 2999) {//钻石任意
                if (conditionId/1000 == 2) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            } else if (taskCondition.getConditionId() == 9019) {//任意8球
                if (conditionId/10%100 == 1 || conditionId/10%200 == 1) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            } else if (taskCondition.getConditionId() == 9029) {//任意红球
                if (conditionId/10%100 == 2 || conditionId/10%200 == 2) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            } else if (taskCondition.getConditionId() == 9039) {//任意15张抽牌
                if (conditionId/10%100 == 3 || conditionId/10%200 == 3) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            } else if (taskCondition.getConditionId() == 9049) {//任意54张抽牌
                if (conditionId/10%100 == 4 || conditionId/10%200 == 4) {
                    finishCount = modifyProgress(taskData, finishCount, taskCondition);
                    taskList.add(task);
                }
            }
        }
        if (finishCount == conditionList.size()) {
            task.setTaskStatus(1);
        }
    }

    /**
     * 修改当前进度
     * @param taskData 任务条件数据
     * @param finishCount 完成次数
     * @param taskCondition 任务条件
     * @return 任务完成条件次数
     */
    private int modifyProgress(TaskData taskData, int finishCount, TaskCondition taskCondition) {
        int progress = taskCondition.getCurrentProgress() + taskData.getTimes();
        taskCondition.setCurrentProgress(progress);
        if (progress >= taskCondition.getTotalProgress()) {
            finishCount++;
        }
        return finishCount;
    }

    /**
     * 将任务封装到proto里
     * @param task 任务
     * @return playerTaskProto
     */
    public PlatFormProto.PlayerTask.Builder Task2Proto(Task task) {
        PlatFormProto.PlayerTask.Builder playerTask = PlatFormProto.PlayerTask.newBuilder();
        for (TaskCondition condition : task.getConditionIds()) {
            PlatFormProto.TaskCondition.Builder taskCondition = PlatFormProto.TaskCondition.newBuilder();
            taskCondition.setConditionId(condition.getConditionId());
            taskCondition.setProgress(condition.getCurrentProgress());
            taskCondition.setTotalProgress(condition.getTotalProgress());
            playerTask.addConditions(taskCondition.build());
        }
        playerTask.setId(task.getId());
        playerTask.setTaskType(task.getTaskType());
        playerTask.setState(task.getTaskStatus());
        playerTask.setTaskId(task.getTaskId());
        return playerTask;
    }

    /**
     * 每周任务转换为任务实体类
     * @param weekTaskConfig 每周任务
     * @return 任务实体类
     */
    public TaskEntity weekTask2TaskEntity(WeekTaskConfig weekTaskConfig) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskId(weekTaskConfig.getId());
        List<TaskCondition> conditionList = new ArrayList<>();
        List<Integer> conditionIds = weekTaskConfig.getConditionIds();
        for (Integer conditionId : conditionIds) {
            TaskCondition taskCondition = new TaskCondition();
            taskCondition.setConditionId(conditionId);
            taskCondition.setCurrentProgress(weekTaskConfig.getCurrentProgress());
            taskCondition.setTotalProgress(weekTaskConfig.getTotalProgress());
            conditionList.add(taskCondition);
        }
        taskEntity.setConditionIds(conditionList);
        taskEntity.setGameType(weekTaskConfig.getGameType());
        return taskEntity;
    }

    /**
     * 每日任务转换为任务实体类
     * @param dayTaskConfig 每日任务
     * @return 任务实体类
     */
    public TaskEntity dayTask2TaskEntity(DayTaskConfig dayTaskConfig) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskId(dayTaskConfig.getId());
        List<TaskCondition> conditionList = new ArrayList<>();
        List<Integer> conditionIds = dayTaskConfig.getConditionIds();
        for (Integer conditionId : conditionIds) {
            TaskCondition taskCondition = new TaskCondition();
            taskCondition.setConditionId(conditionId);
            taskCondition.setCurrentProgress(dayTaskConfig.getCurrentProgress());
            taskCondition.setTotalProgress(dayTaskConfig.getTotalProgress());
            conditionList.add(taskCondition);
        }
        taskEntity.setConditionIds(conditionList);
        taskEntity.setGameType(dayTaskConfig.getGameType());
        return taskEntity;
    }

    /**
     * 成长任务转换为任务实体类
     * @param growthTaskConfig 成长任务
     * @return 任务实体类
     */
    public TaskEntity grewUpTask2TaskEntity(GrowthTaskConfig growthTaskConfig) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setBeforeTaskId(growthTaskConfig.getAfterTaskId());
        taskEntity.setTaskId(growthTaskConfig.getId());
        List<TaskCondition> conditionList = new ArrayList<>();
        List<Integer> conditionIds = growthTaskConfig.getConditionIds();
        for (Integer conditionId : conditionIds) {
            TaskCondition taskCondition = new TaskCondition();
            taskCondition.setConditionId(conditionId);
            taskCondition.setCurrentProgress(growthTaskConfig.getCurrentProgress());
            taskCondition.setTotalProgress(growthTaskConfig.getTotalProgress());
            conditionList.add(taskCondition);
        }
        taskEntity.setConditionIds(conditionList);
        taskEntity.setGameType(growthTaskConfig.getGameType());
        return taskEntity;
    }

    public Task taskEntity2Task(TaskEntity taskEntity) {
        Task task = new Task();
        task.setId(taskEntity.getId());
        task.setTaskId(taskEntity.getTaskId());
        task.setAfterTaskId(taskEntity.getBeforeTaskId());
        task.setTaskStatus(taskEntity.getTaskStatus());
        task.setTaskType(taskEntity.getTaskType());
        task.setGameType(taskEntity.getGameType());
        task.setPlayerId(taskEntity.getPlayerId());
        task.setConditionIds(taskEntity.getConditionIds());
        return task;
    }

    public TaskEntity task2TaskEntity(Task task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(task.getId());
        taskEntity.setTaskId(task.getTaskId());
        taskEntity.setBeforeTaskId(task.getAfterTaskId());
        taskEntity.setTaskStatus(task.getTaskStatus());
        taskEntity.setTaskType(task.getTaskType());
        taskEntity.setGameType(task.getGameType());
        taskEntity.setPlayerId(task.getPlayerId());
        taskEntity.setConditionIds(task.getConditionIds());
        return taskEntity;
    }

    /**
     * 推送新任务
     * @param uid 玩家id
     * @param task 任务
     */
    public void pushNewTask(int uid, Task task) {
        PlatFormProto.S2C_NewTask.Builder builder = PlatFormProto.S2C_NewTask.newBuilder();
        PlatFormProto.PlayerTask.Builder playerTask = Task2Proto(task);
        builder.setTask(playerTask.build());
        //通知用户
        S2C s2c = new S2C();
        s2c.setUid(uid);
        s2c.setCid(Cmd.NEW_TASK);
        s2c.setBody(builder.build().toByteArray());
        billiardPushService.push(s2c);
    }
}
