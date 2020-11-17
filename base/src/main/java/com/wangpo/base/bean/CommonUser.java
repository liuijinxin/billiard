package com.wangpo.base.bean;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

/**
 * 平台用户信息类，各个服公用
 */
@Data
public class CommonUser implements Serializable {
	/** 平台玩家唯一ID **/
	private Integer id;
	/** 微信token **/
	private String token;
	/** 平台玩家昵称 **/
	private String nick;
	/** 平台玩家头像 **/
	private String head;
	/**平台玩家性别**/
	private Integer sex;
	/** 平台玩家金币 **/
	private Integer gold;
	/** 平台玩家钻石 **/
	private Integer diamond;
	/** 平台玩家红包券 **/
	private Integer redPacket;
	/** 玩家状态 */
	private Integer status;
	/** 新手引导 */
	private JSONObject nociveGuideNum;
}