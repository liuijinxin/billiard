package com.wangpo.platform.logic.notice;

import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.cms.CmsSystemNotice;
import com.wangpo.base.cms.Notice;
import com.wangpo.base.service.BilliardPushService;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerMgr;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class NoticeHandler {
	@Resource
	PlayerMgr playerMgr;

	public S2C getNotice(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null ) {
			//推送系统公告
			S2C s2c = new S2C();
			s2c.setUid(c2s.getUid());
			s2c.setCid(Cmd.S2C_NOTICE);
			PlatFormProto.S2C_SystemNotice.Builder b = PlatFormProto.S2C_SystemNotice.newBuilder();
			for(Notice n: BaseExcelMgr.NOTICE_MAP.values()) {
				b.addNotices(PlatFormProto.SystemNotice.newBuilder()
						.setCnTitle(n.getLabel())
						.setWyTitle(n.getUighur())
						.setCnContent(n.getContent())
						.setWyContent(n.getContent())
						.setOrder(n.getSort())
						.setForce(n.getEject())
						.build());
			}
			s2c.setBody(b.build().toByteArray());
			return s2c;
		}
		return null;
	}

	public S2C getSystemTip(C2S c2s) {
		Player player = playerMgr.getPlayerByID(c2s.getUid());
		if( player != null ) {
			//推送系统公告
			S2C s2c = new S2C();
			s2c.setUid(c2s.getUid());
			s2c.setCid(Cmd.S2C_SYSTEM_TIP);
			CmsSystemNotice n = null;
			for(CmsSystemNotice notice: BaseExcelMgr.SYSTEM_NOTICE_MAP.values()) {
				 n = notice;
			}
			if( n == null
					|| n.getCnTitle()==null
					|| "1".equals(n.getCnTitle().trim())  ) {
				//系统公告不存在或者不需要弹出
				s2c.setCode(1);
				return s2c;
			}

			boolean isNewNotice = false;
			if(player.getSystemNoticeTime()==null || !player.getSystemNoticeTime().equals(n.getDate())) {
				isNewNotice = true;
			}

			if( !isNewNotice ) {
				if( player.getLoginLog()!=null && player.getLoginLog().getLoginTimes()>1) {
					//系统公告不存在或者不需要弹出
					s2c.setCode(2);
					return s2c;
				}

				if( player.isSn() ) {
					//系统公告不存在或者不需要弹出
					s2c.setCode(3);
					return s2c;
				}
			}

			player.setSystemNoticeTime(n.getDate());
			player.setSn(true);
			PlatFormProto.S2C_SystemTip.Builder b = PlatFormProto.S2C_SystemTip.newBuilder();
			b.addNotices(PlatFormProto.SystemTip.newBuilder()
					.setCnTitle(n.getCnTitle())
					.setWyTitle(n.getWyTitle())
					.setCnContent(n.getCnContent())
					.setWyContent(n.getWyContent())
					.build());
			s2c.setBody(b.build().toByteArray());
			return s2c;
		}
		return null;
	}



}
