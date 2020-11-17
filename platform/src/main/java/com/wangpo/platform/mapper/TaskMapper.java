package com.wangpo.platform.mapper;

import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlHandlerType;
import com.wangpo.platform.dao.TaskEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("SELECT id,player_id,task_id,before_task_id,game_type,task_type,condition_id,task_status,create_time,update_time FROM task where player_id = #{playerId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "beforeTaskId", column = "before_task_id"),
            @Result(property = "gameType", column = "game_type"),
            @Result(property = "taskType", column = "task_type"),
            @Result(property = "taskStatus", column = "task_status"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "conditionIds", column = "condition_id", jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlListHandler.class)
    })
    List<TaskEntity> selectAllTask(int playerId);

    @Update("update task set task_status = #{taskStatus},condition_id = #{conditionIds,jdbcType=OTHER,typeHandler=com.wangpo.platform.mapper.MySqlListHandler},update_time = #{updateTime} where id = #{id}")
    int updateTask(TaskEntity task);

    @Select("SELECT id,player_id as playerId,task_id as taskId,before_task_id as beforeTaskId,game_type as gameType,task_type as taskType,condition_id as conditionIds," +
            "task_status as taskStatus,create_time as createTime,update_time as updateTime FROM task where player_id=#{playerId} and task_type=#{taskType}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "playerId", column = "player_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "beforeTaskId", column = "before_task_id"),
            @Result(property = "gameType", column = "game_type"),
            @Result(property = "taskType", column = "task_type"),
            @Result(property = "taskStatus", column = "task_status"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "conditionIds", column = "condition_id",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlListHandler.class)
    })
    List<TaskEntity> selectTask(@Param("playerId") int playerId ,@Param("taskType") int taskType);

    @Insert("insert into task(player_id,task_id,before_task_id,game_type,task_type,condition_id,task_status,create_time,update_time) " +
            "values(#{playerId},#{taskId},#{beforeTaskId},#{gameType},#{taskType},#{conditionIds,jdbcType=OTHER,typeHandler=com.wangpo.platform.mapper.MySqlListHandler},#{taskStatus},#{createTime},#{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTask(TaskEntity taskEntity);


}
