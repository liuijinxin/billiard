package com.wangpo.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.wangpo.platform.dao.WxFollower;

@Mapper
public interface WxFollowerMapper {
	
	@Select("select id, open_id,union_id,subscribe,subscribe_time,create_time from kys_wx_follower where union_id=#{unionId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "openId", column = "open_id"),
            @Result(property = "unionId", column = "union_id"),
            @Result(property = "subscribe", column = "subscribe"),
            @Result(property = "subscribeTime", column = "subscribe_time"),
            @Result(property = "createTime", column = "create_time"),
    })
	WxFollower selectByID(String unionId);

}
