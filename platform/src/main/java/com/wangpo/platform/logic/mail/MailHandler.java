package com.wangpo.platform.logic.mail;

import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.*;
import com.wangpo.base.enums.GameEventEnum;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.logic.item.ItemMgr;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.MailService;
import com.wangpo.platform.service.PlayerMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class MailHandler {
	@Resource
	PlayerMgr playerMgr;
	@DubboReference
	private BilliardPushService billiardPushService;
	@Resource
	ItemMgr itemMgr;
	@Resource
	MailService mailService;

	/**
	 * 客户端获取邮件列表
	 * @param c2s
	 * @return
	 */
	public S2C getMail(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player!=null ) {
			PlatFormProto.C2S_GetMail.Builder b = PlatFormProto.C2S_GetMail.newBuilder();
			for(Mail mail:player.getMailList()) {
				PlatFormProto.Mail.Builder builder = mail.mail2Proto();
				b.addMails(builder);
			}
			S2C s2c = new S2C();
			s2c.setCid(c2s.getCid());
			s2c.setUid(c2s.getUid());
			s2c.setBody(b.build().toByteArray());
			return s2c;
		}
		return null;
	}

	/**
	 * 领取邮件附件
	 */
	public S2C mailAward(C2S c2s) throws Exception{
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if (player!=null) {
			//邮件id，为0代表一键提取所有
			long mailId = PlatFormProto.C2S_MailAward.parseFrom(c2s.getBody()).getMailId();
			PlatFormProto.S2C_Award.Builder b = PlatFormProto.S2C_Award.newBuilder();
			b.setId((int) mailId);
			//存储所有奖励
			Map<Integer,Integer> map = new HashMap<>();
			for(Mail mail:player.getMailList()) {
				if( mail.getMailState() == 2 ) {
					continue;
				}
				if( mail.getId() == mailId || mailId == 0) {
					mail.setMailState(2);
					mailService.updateMail(mail);
					for (String key : mail.getItem().keySet()) {
						int id = Integer.parseInt(key);
						int num = mail.getItem().getInteger(key);
						if (map.containsKey(id)) {
							int num1 = map.get(id) + num;
							map.put(id,num1);
						} else {
							map.put(id,num);
						}
						itemMgr.addItem(player, id, num, GameEventEnum.MAIL);
					}
				}
			}
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				b.addAwards(PlatFormProto.Award.newBuilder().setId(entry.getKey()).setNum(entry.getValue()).build());
			}
			S2C s2c = new S2C();
			s2c.setCid(Cmd.GET_TASK_REWARD);
			s2c.setUid(c2s.getUid());
			s2c.setBody(b.build().toByteArray());
			billiardPushService.push(s2c);
		}
		return null;
	}

    public void pushNewMail(Player player, Mail mail) {
		PlatFormProto.S2C_NewMail.Builder builder = PlatFormProto.S2C_NewMail.newBuilder().setMail(mail.mail2Proto().build());
		S2C s2c = new S2C();
		s2c.setCid(Cmd.NEW_MAIL);
		s2c.setUid(player.getId());
		s2c.setBody(builder.build().toByteArray());
		billiardPushService.push(s2c);
	}

	/**
	 * 获取邮件列表
	 * @param player 玩家
	 */
	public void getMailList(Player player) {
		List<Mail> mailList = mailService.selectMailByPlayerId(player.getId());
		Iterator<Mail> iterator = mailList.iterator();
		while (iterator.hasNext()) {
			Mail mail = iterator.next();
			//判断邮件是否已过期，删除过期邮件
			if (mail.getEndTime().compareTo(new Date()) == -1) {
				mailService.deleteMailById(mail.getId());
				iterator.remove();
			}
		}
		player.setMailList(mailList);
	}
}
