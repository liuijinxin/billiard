package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChannelConfig implements Serializable {
    private int id;
    private String name;
    private String remarks;
    private String version;
    private String apkVersion;
    private String download;

}
