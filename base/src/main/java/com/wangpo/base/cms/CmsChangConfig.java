package com.wangpo.base.cms;

import lombok.Data;

import java.io.Serializable;

@Data
public class CmsChangConfig implements Serializable {
    /** 场次id */
    private int id;
    /** 场次 */
    private int chang;
    /** 场次描述 */
    private String changDesc;
    /** ai开启 */
    private int aiOpen;
    /** 强AI概率，0-100，对应弱AI概率为100-强ai概率 */
    private int strongRate;
    private int upLimit;
    private int downLimit;


}
