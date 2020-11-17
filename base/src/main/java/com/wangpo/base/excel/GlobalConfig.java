package com.wangpo.base.excel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class GlobalConfig implements IConfig{
    private int id;
    private String num;
    private String remarks;

    public int intValue() {
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            log.error("解析配置异常，id:{},num:{}",id,num);
        }
        return 0;
    }

    @Override
    public void explain() {

    }
}
