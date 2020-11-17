package com.wangpo.billiard.mapper;

import com.wangpo.base.cms.CmsLotteryConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LotteryConfigMapper {

    @Select("SELECT id,chang,name,num,type,weight,grade FROM lottery_config")
    List<CmsLotteryConfig> getCmsLotteryConfig();

}
