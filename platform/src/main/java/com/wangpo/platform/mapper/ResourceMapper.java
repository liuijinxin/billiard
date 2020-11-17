package com.wangpo.platform.mapper;

import com.wangpo.base.cms.ResourceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResourceMapper {

    @Select("select id,version,url from resource")
    List<ResourceConfig> getResourceConfig();

}
