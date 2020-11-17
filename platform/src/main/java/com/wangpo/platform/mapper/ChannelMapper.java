package com.wangpo.platform.mapper;

import com.wangpo.base.cms.ChannelConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChannelMapper {

    @Select("select id,name,remarks,version,apk_version,download from channel")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "remarks", column = "remarks"),
            @Result(property = "version", column = "version"),
            @Result(property = "apkVersion", column = "apk_version"),
            @Result(property = "download", column = "download")
    })
    List<ChannelConfig> getChannelConfig();

}
