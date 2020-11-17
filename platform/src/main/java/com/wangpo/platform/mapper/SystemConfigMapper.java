package com.wangpo.platform.mapper;

import com.wangpo.base.excel.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SystemConfigMapper {

    @Select("select id,system_key,system_value,remarks from system_config")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "systemKey", column = "system_key"),
            @Result(property = "systemValue", column = "system_value"),
            @Result(property = "remarks", column = "remarks"),
    })
    List<SystemConfig> selectSystemConfig();

}
