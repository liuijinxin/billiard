package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class APPVersion implements Serializable {
    private int id;
    private String version;
    private String download;
    private String manifest;
    private String remarks;

}
