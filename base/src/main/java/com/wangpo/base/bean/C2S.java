package com.wangpo.base.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class C2S implements Serializable {
	public static final int HEADER_LENGTH = 12;
	/** 服务器序列 */
	int sid;
	/** 消息号（各个服独立的，跨服之间可以重复，同一个服内不可重复） */
	int cid;
	/** 消息序号，用作客户端识别服务器响应 */
	int sequence;
	/** 消息体数据 */
	byte[] body;
	/** 玩家ID，登录后绑定 **/
	int uid;
}
