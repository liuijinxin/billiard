package com.wangpo.base.excel;

import lombok.Data;
import java.io.Serializable;

@Data
public class SystemConfig implements Serializable {
    private int id;
    private String systemKey;
    private String systemValue;
    private String remarks;

}
