package com.wangpo.base.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class S2C implements Serializable {
	public static final int HEADER_LENGTH = 16;
	/** 服务器序列 */
	int sid;
	/** 消息号（各个服独立的，跨服之间可以重复，同一个服内不可重复） */
	int cid;
	/** 消息序号，用作客户端识别服务器响应 */
	int sequence;
	/** 响应结果，0是成功，1是请求超时（消息已发出），2是请求错误（消息未发出），大于100是服务器响应返回的错误 */
	int code;
	/** 消息体数据 */
	byte[] body;
	/** 错误信息 */
	String errStr;
	/** 玩家ID，登录后绑定 **/
	int uid;
}
