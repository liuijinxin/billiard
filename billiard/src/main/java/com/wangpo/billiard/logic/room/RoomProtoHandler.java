package com.wangpo.billiard.logic.room;

import com.wangpo.base.net.HostCmd;
import com.wangpo.base.net.IProtoHandler;
import com.wangpo.base.net.Proto;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 游戏服协议处理器，
 */
@Slf4j
@Component
public class RoomProtoHandler implements IProtoHandler {
//    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Resource
    GameMgr roomMgr;

    @Resource
    LinkMgr linkMgr;

    @Override
    public void handle(ChannelHandlerContext context, Proto proto) {
//        executor.submit(()-> handleProto(context, proto));
    }

    private void handleProto(ChannelHandlerContext context, Proto proto) {
        try {
            switch (proto.getCmd()) {
                case HostCmd.LINK:
                    saveLink(context, proto);
                    break;
                case HostCmd.M2R_MATCH:
                    match(proto);
                    break;
                case HostCmd.TRANSFER:
                    transfer(context, proto);
                    break;
                case HostCmd.SNOOKER:
                    snooker(proto);
                    break;
                case HostCmd.SYNC_POS:
                    syncPos(proto);
                    break;
                case HostCmd.DESK_INFO:
                    syncDeskInfo(proto);
                    break;
                case HostCmd.LAY_BALL:
                    //摆球
                    layBall(proto);
                    break;
                case HostCmd.L2R_INIT_ROOM:
                    getRoomInfo(context, proto);
                    break;
                case HostCmd.L2R_NEW_ROUND:
                    newRound(context, proto);
                    break;
                case HostCmd.L2R_DISMISS:
                    dismiss(context, proto);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            log.error("RoomProtoHandler 处理异常，指令{}：", proto.getCmd(), e);
        }
    }

    /**
     * 再来一局
     *
     * @param context
     * @param proto
     */
    public void newRound(ChannelHandlerContext context, Proto proto) throws Exception {
//        GameProto.RoomOpt msg = GameProto.RoomOpt.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        if (gr != null && gr.isGameOver()) {
//            gr.newRound((int)msg.getPlayerId());
//        }
    }

    //房间解散
    public void dismiss(ChannelHandlerContext context, Proto proto) throws Exception {
//        GameProto.RoomOpt msg = GameProto.RoomOpt.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        roomMgr.removeRoom(msg.getRoomNo());
//        log.error("房间解散成功，房间ID：{}", msg.getRoomNo());
    }

    /**
     * 登陆服获取游戏房间信息，一般是在用户断线重连调用
     *
     * @param context
     * @param proto
     */
    private void getRoomInfo(ChannelHandlerContext context, Proto proto) throws Exception {
//        GameProto.L2R_InitRoom req = GameProto.L2R_InitRoom.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(req.getRoomNo());
//        if (gr != null) {
//            context.writeAndFlush(gr.buildGameInit((int)req.getPlayerId()));
//            context.writeAndFlush(gr.buildOptPlayer((int)req.getPlayerId()));
//        }
    }

    /**
     * 转发包，以下情况调用
     * 1，移杆
     * 2，击球
     *
     * @param context
     * @param proto
     */
    public void transfer(ChannelHandlerContext context, Proto proto) throws Exception {
//        GameProto.C2S_Message msg = GameProto.C2S_Message.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        if (gr.isGameOver()) {
//            //游戏结束，操作不再转发
//            return;
//        }
////        gr.transfer(msg.getJson());
//        //
//        JSONObject jsonObject = JSON.parseObject(msg.getJson());
//        int cmd = jsonObject.getInteger("cmd");
//        if (cmd == 1104) {
////            log.info("玩家操作：{},json:{}", msg.getRoomNo(), jsonObject.get("data"));
//            gr.opt();
//        } else if (cmd == 1107) {
//            //已取消，玩家是否犯规，由服务端判定
//            gr.emptyRod((int)msg.getPlayerId());
//        }
////            context.writeAndFlush(roomMgr.transfer(proto));
    }

    public void snooker(Proto proto) throws Exception {
//        GameProto.C2S_Message msg = GameProto.C2S_Message.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        gr.snooker((int)msg.getPlayerId(), msg.getJson());
    }

    public void syncPos(Proto proto) throws Exception {
//        GameProto.C2S_Message msg = GameProto.C2S_Message.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        gr.syncPos(msg.getPlayerId(), msg.getJson());
    }

    public void syncDeskInfo(Proto proto) throws Exception {
//        GameProto.C2S_Message msg = GameProto.C2S_Message.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        gr.syncDeskInfo(msg.getJson());
    }

    public void layBall(Proto proto) throws Exception {
//        GameProto.C2S_Message msg = GameProto.C2S_Message.parseFrom(proto.getBody());
//        IGameRoom gr = roomMgr.get(msg.getRoomNo());
//        boolean ok = gr.layBall(msg.getPlayerId(),msg.getJson());
//        if( ok ) {
//            gr.transfer(msg.getJson());
//        }

    }


    /**
     * 匹配成功，创建房间
     *
     * @param proto
     */
    public void match(Proto proto) throws Exception {
//        //创建房间
//        GameProto.M2R_Match match = GameProto.M2R_Match.parseFrom(proto.getBody());
//        int chang = match.getChang();
//        if ( chang == 101 ) {
//            roomMgr.newGame(match);
//        } else if( chang == 201 ) {
//            roomMgr.newRedBall(match);
//        }
//        //发送匹配成功
//        ChannelHandlerContext link = linkMgr.getLink(HostUrl.LOGIN_PREFIX);
//        if (link != null) {
//            Proto ok = new Proto();
//            GameProto.R2L_MatchOk.Builder b = GameProto.R2L_MatchOk.newBuilder();
//            b.setRoomNo(match.getRoomNo());
//            b.addAllPlayers(match.getGpList());
//            ok.setCmd(HostCmd.R2L_NEW_ROOM_OK);
//            ok.setBody(b.build().toByteArray());
//            link.writeAndFlush(ok);
//        }
    }

    public void saveLink(ChannelHandlerContext ctx, Proto proto) throws Exception {
//        GameProto.HostUrl url = GameProto.HostUrl.parseFrom(proto.getBody());
//        linkMgr.putLink(url.getHostUrl(), ctx);
    }
}
