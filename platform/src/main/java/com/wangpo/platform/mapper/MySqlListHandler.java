package com.wangpo.platform.mapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.TaskCondition;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 用以mysql中json格式的字段，进行转换的自定义转换器，转换为实体类的List属性
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class MySqlListHandler extends BaseTypeHandler<List<TaskCondition>> {

	/**
	 * 设置非空参数
	 * @param ps
	 * @param i
	 * @param parameter
	 * @param jdbcType
	 * @throws SQLException
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, List<TaskCondition> parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, String.valueOf(JSONArray.parseArray(JSON.toJSONString(parameter))));
	}

	/**
	 * 根据列名，获取可以为空的结果
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	@Override
	public List<TaskCondition> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String sqlJson = rs.getString(columnName);
		if (null != sqlJson){
			List<TaskCondition> taskConditions = new ArrayList<>();
			List<JSONObject> list = JSON.toJavaObject(JSON.parseArray(sqlJson), List.class);
			for (JSONObject jsonObject : list) {
				TaskCondition condition = JSON.toJavaObject(jsonObject, TaskCondition.class);
				taskConditions.add(condition);
			}
			return taskConditions;
		}
		return null;
	}

	/**
	 * 根据列索引，获取可以为空的结果
	 * @param rs
	 * @param columnIndex
	 * @return
	 * @throws SQLException
	 */
	@Override
	public List<TaskCondition> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String sqlJson = rs.getString(columnIndex);
		if (null != sqlJson){
			List<TaskCondition> taskConditions = new ArrayList<>();
			List<JSONObject> list = JSON.toJavaObject(JSON.parseArray(sqlJson), List.class);
			for (JSONObject jsonObject : list) {
				TaskCondition condition = JSON.toJavaObject(jsonObject, TaskCondition.class);
				taskConditions.add(condition);
			}
			return taskConditions;
		}
		return null;
	}

	@Override
	public List<TaskCondition> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String sqlJson = cs.getString(columnIndex);
		if (null != sqlJson){
			List<TaskCondition> taskConditions = new ArrayList<>();
			List<JSONObject> list = JSON.toJavaObject(JSON.parseArray(sqlJson), List.class);
			for (JSONObject jsonObject : list) {
				TaskCondition condition = JSON.toJavaObject(jsonObject, TaskCondition.class);
				taskConditions.add(condition);
			}
			return taskConditions;
		}
		return null;
	}
}
