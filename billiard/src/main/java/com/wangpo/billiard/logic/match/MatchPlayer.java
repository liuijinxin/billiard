package com.wangpo.billiard.logic.match;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.bean.PlayerCue;
import com.wangpo.billiard.logic.util.FightUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MatchPlayer {
    private int second;
//    private GameProto.GamePlayer gp;

    private int id;
    private String nick;
    private String head;
    private int roleId;
    private int exp;
    /** 玩家游戏属性：战力相关 **/
    private JSONObject gameJson;
    private List<Integer> lastThree = new ArrayList<>();

    private int cueId;

    public void fromPlayer(Player player, int chang) {
        id = player.getId();
        nick = player.getUser().getNick();
        head = player.getUser().getHead();
        if(player.getCueList()!=null && player.getCueList().size()>0) {
            boolean ok = false;
            for(PlayerCue playerCue:player.getCueList()) {
                if( playerCue.getIsUse() == 1) {
                    cueId = playerCue.getCueID();
                    ok=  true;
                    break;
                }
            }
            if( !ok) {
                cueId = 201;
            }
        }  else {
            //默认球杆
            cueId = 101;
        }

        gameJson = player.getFight().getJSONObject(String.valueOf(FightUtil.chang2game(chang)));
        lastThree.addAll(player.getLastThree());
//         = player.getFight().containsKey(chang)?player.getFight().getInteger(chang):200;
    }

//    public GameProto.GamePlayer.Builder toProto() {
//        return GameProto.GamePlayer.newBuilder()
//                .setNick(gp.getNick())
//                .setPlayerId(gp.getPlayerId())
//                .setFight(gp.getFight())
//                .setChang(gp.getChang())
//                .setHead(gp.getHead());
//    }
}
