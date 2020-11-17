package com.wangpo.billiard.mapper;

import com.wangpo.billiard.dao.BilliardUserEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BilliardUserMapper {
	@Insert("insert to user(id,token,nick,head,gold,diamond,exp,fight) values(#{id},#{token},#{nick},#{head},#{gold},#{diamond}，#{exp},#{fight})")
	int insert(BilliardUserEntity userEntity);

	@Delete("delete from user where id=#{id}")
	int delete(long id);

	@Select("select id, token,nick,head,gold,diamond,exp,fight from user where id=#{id}")
	BilliardUserEntity selectByID(long id);
}
