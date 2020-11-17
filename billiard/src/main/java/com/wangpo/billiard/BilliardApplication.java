package com.wangpo.billiard;

import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.bean.PlayerCue;
import com.wangpo.billiard.config.ConfigMgr;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.match.MatchPool;
import com.wangpo.billiard.logic.match.RobotMgr;
import com.wangpo.billiard.service.BilliardCueService;
import com.wangpo.billiard.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bovy
 *                            _ooOoo_
 *                           o8888888o
 *                           88" . "88
 *                           (| -_- |)
 *                            O\ = /O
 *                        ____/`---'\____
 *                      .   ' \\| |// `.
 *                       / \\||| : |||// \
 *                     / _||||| -:- |||||- \
 *                       | | \\\ - /// | |
 *                     | \_| ''\---/'' | |
 *                      \ .-\__ `-` ___/-. /
 *                   ___`. .' /--.--\ `. . __
 *                ."" '< `.___\_<|>_/___.' >'"".
 *               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 *                 \ \ `-. \_ __\ /__ _/ .-` / /
 *         ======`-.____`-.___\_____/___.-`____.-'======
 *                            `=---='
 *
 *         .............................................
 *                  佛祖坐镇    必无Bug
 *
 *      TODO
 *      1,excel配置要支持热加载，这样很多配置就不需要在后台管理里面配 ✔
 *      2,统一玩家入库接口。
 */


@EnableDubbo
@SpringBootApplication
@Slf4j
@EnableScheduling
@EnableAsync
public class BilliardApplication {
    @Resource
    MatchPool matchPool;
    @Resource
    ExcelMgr excelMgr;
    @Resource
    RobotMgr robotMgr;
    @Resource
    PlayerMgr playerMgr;
    @Resource
    PlayerService playerService;
    @Resource
    BilliardCueService cueService;
    @Resource
    ConfigMgr configMgr;

    public static void main(String[] args) {
        SpringApplication.run(BilliardApplication.class,args) ;
    }

    /*@Component
    public class TcpSocketRunner implements CommandLineRunner {
        public void run(String... strings) {
            try {
                roomServer.start();
                Thread.currentThread().join();
            } catch (Exception e) {
                log.error("tcpSocketServer startup error!", e);
            }
        }
    }*/

    @Component
    public class OnInit implements  CommandLineRunner {

        @Override
        public void run(String... strings) throws Exception{

           try {
                //读取配置表
               excelMgr.readExcel();
               configMgr.initConfig();
               //初始化匹配池
               matchPool.start();
               //初始化机器人
               robotMgr.init();
               log.error("台球游戏初始化完成，游戏启动成功。。");
            } catch (Exception e) {
                log.error("游戏初始化失败，启动失败!", e);
                System.exit(1);
            }
        }
    }

   /* @Component
    public class MatchPoolRunner implements CommandLineRunner {
        public void run(String... strings) {
            try {
                matchPool.start();
            } catch (Exception e) {
                log.error("matchPool startup error!", e);
            }
        }
    }

    @Component
    public class ExcelRunner implements CommandLineRunner {
        public void run(String... strings) {
            try {
                excelMgr.readExcel();
            } catch (Exception e) {
                log.error("excel read error!", e);
            }
        }
    }*/


    @Component
    public class OnDestroy {
        @PreDestroy
        public void destroy() {
            log.info("台球服务器即将关闭，开始进行关闭前的保存工作。");
            //1，保存用户信息
            Map<Integer,Player> idMap = playerMgr.allPlayer();
            Iterator<Player> iterator = idMap.values().iterator();
            Player player;
            while(iterator.hasNext()) {
                player = iterator.next();
                playerService.updatePlayer(player);

                //同步球杆信息
                List<PlayerCue> cueList = player.getCueList();
                for (PlayerCue playerCue : cueList) {
                    cueService.updateCue(playerCue);
                }
            }
            log.info("保存用户数量：{}",idMap.size());
            //2,其他操作

            log.info("台球服务器即将关闭，关闭前的保存工作处理完毕。");
        }

    }

}
