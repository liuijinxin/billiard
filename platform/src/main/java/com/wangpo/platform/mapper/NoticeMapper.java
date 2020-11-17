package com.wangpo.platform.mapper;

import com.wangpo.base.cms.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoticeMapper {

    @Select("select id,label,uighur,content,uighur_content,eject,sort from game_notice")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "label", column = "label"),
            @Result(property = "uighur", column = "uighur"),
            @Result(property = "content", column = "content"),
            @Result(property = "uighur_content", column = "uighurContent"),
            @Result(property = "eject", column = "eject"),
            @Result(property = "sort", column = "sort")
    })
    List<Notice> getAllNotice();

}
