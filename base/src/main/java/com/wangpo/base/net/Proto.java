package com.wangpo.base.net;

import lombok.Data;

import java.io.Serializable;


@Data
public class Proto implements Serializable {
    private short cmd;
    private byte[] body;
}