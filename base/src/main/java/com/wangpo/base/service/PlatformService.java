package com.wangpo.base.service;

import com.wangpo.base.bean.*;
import com.wangpo.base.cms.*;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.base.excel.SystemConfig;

/**
 * 模拟平台通用 RPC
 */
public interface PlatformService {
	/**
	 * 根据 token 查询平台玩家信息
	 * @param c2s C2S
	 * @return  User 平台玩家基础信息
	 */
	CommonUser login(C2S c2s);

	void afterLogin(int uid);

	void logout(int uid);

	S2C request(C2S c2s);
	/**
	 * 根据 玩家ID 查询平台玩家信息
	 * @param uid 平台用户id
	 * @return  User 平台玩家基础信息
	 */
	CommonUser queryUserByID(Integer uid);

	CommonUser modifyGold(int uid, int modifyNum, String reason);

    CommonUser modifyDiamond(int uid, int modifyNum, String reason);

    void finishTask(int uid, int sid, TaskData taskData);

	void addItem(int uid,int modelId,int num, GameEventEnum eventEnum);

	boolean useItem(int uid, int modelId, int num);

	void sendMail(Mail mail);

	CommonUser modifyRedPacket(int id, int num, String reason);

	void addRedPacket(int uid,int chang,int num);

	/**
	 * 修改商城配置
	 * @param type 1增加，2删除，3修改
	 * @param shopConfig 商城配置
	 */
	void modifyShopConfig(int type, ShopConfig shopConfig);

	/**
	 * 修改系统配置
	 * @param type 1增加，2删除，3修改
	 * @param systemConfig 系统配置
	 */
	void modifySystemConfig(int type, SystemConfig systemConfig);

	/**
	 * 修改版本配置
	 * @param type 1增加，2删除，3修改
	 * @param appVersion 版本配置
	 */
	void modifyAppVersion(int type, APPVersion appVersion);

	/**
	 * 修改渠道配置
	 * @param type 1增加，2删除，3修改
	 * @param channelConfig 渠道配置
	 */
	void modifyChannelConfig(int type, ChannelConfig channelConfig);

	/**
	 * 修改资源配置
	 * @param type 1增加，2删除，3修改
	 * @param resourceConfig 资源配置
	 */
	void modifyResourceConfig(int type, ResourceConfig resourceConfig);

	/**
	 * 修改公告信息
	 * @param type 1增加，2删除，3修改
	 * @param notice 公告信息
	 */
	void modifyNotice(int type, Notice notice);

	/**
	 * 修改系统公告
	 * * @param type 1增加，2删除，3修改
	 * @param notice 公告信息
	 */
	void modifyCmsSystemNotice(int type, CmsSystemNotice notice);

	boolean getUserName(int id);

	int getOnlineCount(String origin);

	void freezePlayer(int id,int status);

}
