package com.wangpo.platform.mapper;

import com.wangpo.base.cms.APPVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface APPMapper {

    @Select("select id,version,download,manifest,remarks from version")
    List<APPVersion> selectAPPVersion();


}

