package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CmsSystemNotice implements Serializable {
    private int id;
    private String cnTitle;
    private String wyTitle;
    private String cnContent;
    private String wyContent;
    private Date date;

}
