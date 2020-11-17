package com.wangpo.platform.mapper;

import com.wangpo.base.cms.CmsSystemNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SystemNoticeMapper {

    @Select("SELECT id,cn_title AS 'cnTitle',wy_title AS 'wyTitle',cn_content AS 'cnContent',wy_content AS 'wyContent', update_time AS 'date' FROM system_notice")
    List<CmsSystemNotice> getSystemNotice();

}
