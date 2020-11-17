package com.wangpo.platform.service;

public class Cmd {
	public static final int LOGIN = 100;
	public static final int UPDATE_GOLD = 101;
	public static final int UPDATE_DIAMOND = 102;
	public static final int UPDATE_RED_PACKET = 103;
	public static final int REQ_CONFIG = 104;
	public static final int HEART = 105;



	//获取任务
	public static final int GET_TASK = 201;
	//更新任务
	public static final int UPDATE_TASK = 202;
	//领取奖励
	public static final int GET_TASK_REWARD = 203;
	//领取活跃度奖励
	public static final int GET_ACTIVE_REWARD = 204;
	//推送新任务
	public static final int NEW_TASK = 205;
	//分享
	public static final int SHARE = 206;

	//抽奖
	public static final int DRAW_LOTTERY = 207;
	//获取红包墙
	public static final int GET_RED_PACKET = 208;
	//更新红包墙
	public static final int ADD_RED_PACKET = 209;

	//领取会员每日奖励
	public static final int MEMBER_AWARD = 210;
	//领取会员升级奖励
	public static final int MEMBER_UPGRADE = 211;
	//获取会员信息
	public static final int MEMBER_INFO = 215;
	//领取会员等级奖励
	public static final int LEVEL_AWARD = 216;

	//获取邮件
	public static final int GET_MAIL = 212;
	//领取邮件附件
	public static final int MAIL_AWARD = 213;
	//新邮件通知
	public static final int NEW_MAIL = 214;
	//签到
	public static final int SIGN = 217;
	//签到信息
	public static final int SIGN_INFO = 218;
	//支付信息
	public static final int PAY_INFO = 219;
	//商城购买
	public static final int BUY_SHOP = 220;
	//购买商品结束返回
	public static final int BUY_GOODS_END = 221;
	//获取支付宝账号信息
	public static final int ALI_INFO = 222;
	//兑换红包
	public static final int ALI_REDPACKAGE = 223;
 	//兑换红包
	public static final int REDPACKAGE = 223;
	//获取微信信息
	public static final int WX_INFO = 224;
	//绑定手机号
	public static final int BINGDING_PHONE = 225;
	//绑定手机号验证码
	public static final int PHONE_CODE = 226;
	//进入游戏推送信息
	public static final int PUSH_PLATFORM = 227;
	//实名认证
	public static final int AUTHENTICATION = 228;
	
	//游戏公告
	public static final int GET_NOTICE = 230;
	public static final int S2C_NOTICE = 231;


	
	//每日月卡礼包领取
	public static final int MONCARD_GIFTBAG= 232;
	//每日复活月卡礼包次数
	public static final int RESURRECTION_NUM = 233;
	//每日复活月卡礼包领取
	public static final int RESURRECTION_GIFTBAG = 234;
	//每日复活月卡奖励
	public static final int PUSH_MONCARD_AWARD = 235;


	//系统公告
	public static final int GET_SYSTEM_TIP = 236;
	public static final int S2C_SYSTEM_TIP = 237;
	//新手引导次数
	public static final int NOVICEGUIDE = 238;

}
