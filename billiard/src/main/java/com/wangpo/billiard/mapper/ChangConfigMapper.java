package com.wangpo.billiard.mapper;

import com.wangpo.base.cms.CmsChangConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChangConfigMapper {

    @Select("SELECT id,chang,chang_desc AS 'changDesc',ai_open AS 'aiOpen',strong_rate AS 'strongRate',up_limit as 'upLimit',down_limit as 'downLimit' FROM chang_config")
    List<CmsChangConfig> getChangConfig();

}
