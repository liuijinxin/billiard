package com.wangpo.platform.logic.mail;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.Mail;
import com.wangpo.base.kits.FormatKit;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.service.MailService;
import com.wangpo.platform.service.PlayerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class MailMgr {
	@Resource
	MailService mailService;
	@Resource
	PlayerMgr playerMgr;
	@Resource
	MailHandler mailHandler;

	private static final List<Mail> systemMailList = new ArrayList<>();

	/**
	 * 服务器启动的时候从数据库查询出系统邮件
	 */
	public void init() {
		List<Mail> mailList = mailService.selectSystemMail();
		Iterator<Mail> iterator = mailList.iterator();
		while (iterator.hasNext()) {
			Mail mail = iterator.next();
			//删除过期邮件
			if (mail.getEndTime().compareTo(new Date()) == -1) {
				mailService.deleteMailById(mail.getId());
				iterator.remove();
			}
		}
		systemMailList.addAll(mailList);
	}

	/**
	 * 新增指定玩家的邮件
	 * @param mail 邮件
	 */
	public void addPersonalMail(Mail mail) {
		//将后台发送过来的玩家id分割，邮件发送给对应的玩家
		String[] playerIds = mail.getPlayerIds().split(",");
		for (String playerId : playerIds) {
			if (!"".equals(playerId)) {
				Mail m = new Mail();
				m.setPlayerId(Integer.parseInt(playerId));
				m.setTitle(mail.getTitle());
				m.setContent(mail.getContent());
				String items = mail.getItems();
				m.setItem(str2JsonObject(items));
				m.setMailType(1);
				m.setMailState(0);
				m.setTime(mail.getEndTime().getTime());
				m.setEndTime(mail.getEndTime());
				mailService.insertMail(m);
				Player player = playerMgr.getPlayerByID(Integer.valueOf(playerId));
				if (player != null) {
					player.getMailList().add(m);
					mailHandler.pushNewMail(player,m);
				}
			}
		}
	}

	/**
	 * 新增系统邮件
	 * @param mail
	 */
	public void addSystemMail(Mail mail) {
		Mail m = new Mail();
		m.setPlayerId(0);
		m.setTitle(mail.getTitle());
		m.setContent(mail.getContent());
		String items = mail.getItems();
		m.setItem(str2JsonObject(items));
		m.setMailType(0);
		m.setMailState(0);
		m.setTime(mail.getEndTime().getTime());
		m.setEndTime(mail.getEndTime());
		mailService.insertMail(m);
		systemMailList.add(m);

		//玩家在线，直接推送
		Map<Integer, Player> playerMap = playerMgr.getIdMap();
		for (Player player : playerMap.values()) {
			Mail mail1 = new Mail();
			mail1.setPlayerId(player.getId());
			mail1.setTitle(m.getTitle());
			mail1.setContent(m.getContent());
			mail1.setItem(m.getItem());
			mail1.setMailType(1);
			mail1.setMailState(0);
			mail1.setTime(m.getTime());
			mail1.setSystemId((int) m.getId());
			mail1.setEndTime(m.getEndTime());
			mailService.insertMail(mail1);
			//加入到玩家对象
			player.getMailList().add(mail1);
			mailHandler.pushNewMail(player,mail1);
		}
	}

	/**
	 * 初始化玩家的系统邮件
	 * @param player 玩家
	 */
	public void initPlayerSystemMail(Player player) {
		Iterator<Mail> iterator = systemMailList.iterator();
		while (iterator.hasNext()) {
			Mail mail = iterator.next();
			//删除过期的系统邮件
			if (mail.getEndTime().compareTo(new Date()) == -1) {
				mailService.deleteMailById(mail.getId());
				iterator.remove();
				continue;
			}
			//判断玩家身上是否有对应的系统邮件
			boolean has = player.getMailList().stream().anyMatch(mail2->mail2.getSystemId()==mail.getId());
			if( !has ) {
				Mail m = new Mail();
				m.setPlayerId(player.getId());
				m.setTitle(mail.getTitle());
				m.setContent(mail.getContent());
				m.setItem(mail.getItem());
				m.setMailType(1);
				m.setMailState(0);
				m.setTime(mail.getTime());
				m.setSystemId((int) mail.getId());
				m.setEndTime(mail.getEndTime());
				mailService.insertMail(m);
				//加入到玩家对象
				player.getMailList().add(m);
			}
		}
	}

	/**
	 * 将后台发送过来的奖励封装成JSONObject
	 * @param items1 后台发送过来的奖励
	 * @return 奖励的JSONObject
	 */
	private JSONObject str2JsonObject(String items1) {
		try {
			if (!"".equals(items1)) {
				Map<String,Object> map = new HashMap<>();
				String[] items = items1.split(";");
				for (String item : items) {
					if (!"".equals(item)) {
						String[] str = item.split(",");
						if (str.length == 2 && !"".equals(str[0]) && !"".equals(str[1])) {
							map.put(str[0],str[1]);
						}
					}
				}
				return (JSONObject) JSON.toJSON(map);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

}
