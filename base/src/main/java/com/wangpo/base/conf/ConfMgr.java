package com.wangpo.base.conf;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfMgr {
    private static ConfMgr me = new ConfMgr();
    private ConfMgr(){
    }

    public static ConfMgr me() {
        return me;
    }

    Properties prop;

    public void init() {
        this.prop = new Properties();
        try {
            InputStream s = ConfMgr.class.getResourceAsStream("/config.properties");
            prop.load(s);
        } catch(IOException e) {
            log.error("读取配置文件错误：",e);
            System.exit(-1);
        }
    }

    public String getString(String key) {
        return prop.getProperty(key);
    }
}
