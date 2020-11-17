package com.wangpo.platform.logic.redpacket;

import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.dao.RedPacketEntity;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.RedPacketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class RedPacketHandler {
    @Resource
    RedPacketService redPacketService;
    @Resource
    PlayerMgr playerMgr;
    @DubboReference
    private BilliardPushService billiardPushService;

    /**
     * 获取所有红包
     */
    public S2C allRedPacket(C2S c2s) {
        int uid = c2s.getUid();
        List<RedPacketEntity> list = redPacketService.selectAllRedPacket();
        PlatFormProto.C2S_GetRedPacket.Builder builder = PlatFormProto.C2S_GetRedPacket.newBuilder();
        for (RedPacketEntity entity : list) {
            builder.addRedPackets(entity.redPacket2Proto().build());
        }
//        log.info("红包数量：" + list.size());
        S2C s2c = new S2C();
        s2c.setCid(Cmd.GET_RED_PACKET);
        s2c.setUid(uid);
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 抽奖
     */
    public S2C drawLottery(C2S c2s) throws Exception {
        PlatFormProto.C2S_DrawLottery drawLottery = PlatFormProto.C2S_DrawLottery.parseFrom(c2s.getBody());
        int chang = drawLottery.getChang();
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.DRAW_LOTTERY);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setUid(uid);
            return s2c;
        }
        RedPacketEntity redPacket = new RedPacketEntity();
        redPacket.setPlayerId(uid);
        redPacket.setChang(chang);
        redPacket.setNick(player.getNick());
        redPacket.setTime(System.currentTimeMillis());
        redPacket.setVip(player.getVip());

        //红包数量随机生成，从10到500
        Random random = new Random();
        int num = random.nextInt(49) +1;
        redPacket.setNum(num * 10);
//        log.info("生成红包：{}",redPacket);
        redPacketService.insertRedPacket(redPacket);

        //推送给客户端
        pushRedPacket(uid, redPacket);
        return s2c;
    }

    /**
     * 抽奖之后调用，新增红包
     * @param player 玩家
     * @param chang 场次
     * @param num 数量
     */
    public void addRedPacket(Player player, int chang, int num){
        RedPacketEntity redPacket = new RedPacketEntity();
        redPacket.setPlayerId(player.getId());
        redPacket.setChang(chang);
        redPacket.setNick(player.getNick());
        redPacket.setTime(System.currentTimeMillis());
        redPacket.setVip(player.getVip());
        redPacket.setNum(num);
        redPacketService.insertRedPacket(redPacket);
        //推送给客户端
        pushRedPacket(player.getId(), redPacket);
    }

    /**
     * 推送红包墙信息给客户端
     * @param uid 玩家id
     * @param redPacket 红包
     */
    private void pushRedPacket(int uid, RedPacketEntity redPacket) {
        PlatFormProto.S2C_AddRedPacket.Builder builder = PlatFormProto.S2C_AddRedPacket.newBuilder();
        builder.setRedPacket(redPacket.redPacket2Proto().build());
        S2C s2c = new S2C();
        s2c.setCid(Cmd.ADD_RED_PACKET);
        s2c.setUid(uid);
        s2c.setBody(builder.build().toByteArray());
        billiardPushService.push(s2c);
    }


}
