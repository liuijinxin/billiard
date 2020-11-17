package com.wangpo.platform;

import com.wangpo.base.bean.Mail;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.bean.PlayerVip;
import com.wangpo.platform.config.ConfigMgr;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.logic.mail.MailMgr;
import com.wangpo.platform.service.MailService;
import com.wangpo.platform.service.MemberService;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@EnableDubbo
@SpringBootApplication
@ServletComponentScan
@ConfigurationPropertiesScan
@Slf4j
@EnableScheduling
public class PlatformApplication {
	@Resource
	BaseExcelMgr baseExcelMgr;
	@Resource
	PlayerMgr playerMgr;
	@Resource
	PlayerService playerService;
	@Resource
	MailMgr mailMgr;
	@Resource
	MailService mailService;
	@Resource
	MemberService memberService;
	@Resource
	ConfigMgr configMgr;


	public static void main(String[] args) {
		SpringApplication.run(PlatformApplication.class, args);
	}

	@Component
	public class OnInit implements CommandLineRunner {
		@Override
		public void run(String... strings) {
			try {
				//读取配置文件
				baseExcelMgr.readExcel();
				configMgr.initConfig();
				//查询系统邮件
				mailMgr.init();
				log.error("平台服初始完成，启动成功。。。");
			} catch (Exception e) {
				log.error("平台服初始化失败：", e);
				System.exit(1);
			}
		}
	}

	@Component
	public class OnDestroy {
		@PreDestroy
		public void destroy() {
			log.error("台球服务器即将关闭，开始进行关闭前的保存工作。");
			//1，保存用户信息
			Map<Integer, Player> idMap = playerMgr.allPlayer();
			Iterator<Player> iterator = idMap.values().iterator();
			Player player;
			while(iterator.hasNext()) {
				player = iterator.next();
				playerService.updatePlayer(player);
				//更新邮件状态
				List<Mail> mailList = player.getMailList();
				for (Mail mail : mailList) {
					mailService.updateMail(mail);
				}
				//更新vip状态
				PlayerVip playerVip = player.getPlayerVip();
				memberService.updateMember(playerVip);
			}
			log.error("保存用户数量：{}",idMap.size());
			//2,其他操作



			log.error("台球服务器即将关闭，关闭前的保存工作处理完毕。");
		}
	}

}
