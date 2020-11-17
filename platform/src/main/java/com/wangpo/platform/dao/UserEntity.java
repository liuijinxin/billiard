package com.wangpo.platform.dao;

import com.wangpo.base.bean.CommonUser;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserEntity implements Serializable {
	private Integer id;
	private String nick;
	private String token;
	private String head;
	private Integer sex;
	private Integer gold;
	private Integer diamond;
	private Date updateTime;
	private Date createTime;

	public CommonUser entity2User( ) {
		CommonUser user = new CommonUser();
		user.setId(id);
		user.setNick(nick);
		user.setHead(head);
		user.setSex(sex);
		user.setGold(gold);
		user.setDiamond(diamond);
		return user;
	}
}
