package com.wangpo.base.cms;

import lombok.Data;
import java.io.Serializable;

@Data
public class ResourceConfig implements Serializable {
    private int id;
    private String version;
    private String url;

}
