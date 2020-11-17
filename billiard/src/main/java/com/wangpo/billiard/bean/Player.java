package com.wangpo.billiard.bean;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.CommonUser;
import com.wangpo.base.item.Item;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Player {
	/**
	 * mysql存储数据
	 */
	private Integer id;
	private String nick;
	private String head;
	private int sex;
	private int gold;
	private int diamond;
	private CommonUser user;
	private int exp;//根据经验算出等级和当前升级所需经验
	/** 场次战力相关 **/
	private JSONObject fight = new JSONObject() ;
	/** 场次游戏次数 **/
	private JSONObject chang = new JSONObject();
	private Date updateTime;
	private Date createTime;
	//道具列表
	private List<Item> itemList = new ArrayList<>();
	private Date loginTime;
	private Date logoutTime;

	/**
	 * 游戏内存数据
	 */
	private int roomNo;
	/** 匹配状态：0-未匹配，1-已加入匹配 **/
	private int matchStatus;
	/** 匹配场次ID **/
	private int matchPoolId;
	private int level;
	private int currentExp;
	private int currentNeedExp;
	private boolean online;
	private int vipLevel;
	private long offlineTime;
	private long onlineTime;
	//角色
	private List<Role> roleList = new ArrayList<>();
	// 上三把对手
	private List<Integer> lastThree = new ArrayList<>();
	//玩家球杆
	private List<PlayerCue> cueList = new ArrayList<>();
	//幸运一杆
	private LuckyCue luckyCue ;

	public long offlineTime(){
		if( this.online ) {
			return 0;
		}
		return (System.currentTimeMillis()-this.offlineTime)/1000;
	}

	public void addRival(int id) {
		if( lastThree.size()==3) {
			lastThree.remove(2);
		}
		lastThree.add(id);
	}

	public int useRoleId() {
		List<Role> roleList = getRoleList();
		for (Role role : roleList) {
			if (role.getIsUse() == 1) {
				return role.getRoleId();
			}
		}
		return 1001;
	}
}
