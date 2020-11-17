package com.wangpo.billiard.mapper;

import com.wangpo.base.cms.MatchConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MatchMapper {

    @Select("select id,cue_weight,win_weight,streak_weight,is_open from match_config")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "cueWeight", column = "cue_weight"),
            @Result(property = "winWeight", column = "win_weight"),
            @Result(property = "streakWeight", column = "streak_weight"),
            @Result(property = "isOpen", column = "is_open")
    })
    List<MatchConfig> selectAllMatchConfig();

}
