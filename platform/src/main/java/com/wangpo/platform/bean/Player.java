package com.wangpo.platform.bean;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.CommonUser;
import com.wangpo.base.bean.Mail;
import com.wangpo.base.bean.Task;
import com.wangpo.base.item.Item;
import com.wangpo.platform.dao.UserEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Player {
	//全服公用
	private int id;
	private int parentId;
	private String origin;
	private String newOrigin;
	private String token;
	private String openId;
	private String unionId;
	private String nick;
	private String head;
	private int sex;//普通用户性别，1为男性，2为女性
	private int gold;
	private int diamond;
	private int redPacket;
	private JSONObject alipay = new JSONObject();
	private String phone;
	private String name;
	private String idcard;
	private int status;//状态，0正常，1封号
	//平台服
//	private int userId;
	private int dayActive;//日活跃度
	private int dayActiveStatus;//日活跃度领取状态
	private int weekActive;
	private int weekActiveStatus;
	private int signDay;//签到天数
	private int signStatus;//签到状态
	private String lastDay;
	private String lastMonday;
	private int totalGame;
	private Date loginTime;
	private Date logoutTime;
	private Date updateTime;
	private Date createTime;
	private JSONObject noviceGuide = new JSONObject();//新手引导次数

	//是否在线
	private boolean online;
	//离线时间
	private long offlineTime;
	//心跳时间
	private long heartTime;
	private int vip;//会员等级
	private long telCodeTime;//发送手机验证码时间
	private TelCacheBean telCacheBean;

	//系统公告时间，用于更新
	private Date systemNoticeTime;
	//系统公告是否已弹出
	private boolean sn;

	public long offlineTime(){
		if( this.online ) {
			return 0;
		}
		return (System.currentTimeMillis()-this.offlineTime)/1000;
	}

//	private UserEntity userEntity;
	private PlayerVip playerVip;
	//玩家任务列表
	private List<Task> taskList = new ArrayList<>();
	//
//	private List<Item> itemList = new ArrayList<>();
	//邮件列表
	private List<Mail> mailList = new ArrayList<>();
	//登录日志
	private LoginLog loginLog;

	public CommonUser toCommonUser() {
		CommonUser c = new CommonUser();
		c.setId(id);
		c.setToken(openId==null?token:openId);
		c.setNick(nick);
		c.setHead(head);
		c.setSex(sex);
		c.setGold(gold);
		c.setDiamond(diamond);
		c.setRedPacket(redPacket);
		c.setStatus(status);
		c.setNociveGuideNum(noviceGuide);
		return c;
	}
}
