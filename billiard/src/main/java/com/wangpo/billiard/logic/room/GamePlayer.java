package com.wangpo.billiard.logic.room;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.billiard.logic.match.MatchPlayer;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ➀➁➂➃➄➅➆➇➈➉
 * ☯❤✈©☀☎✉✉✐☂♔🍎⚽
 */

@Data
public class GamePlayer {
    /**
     * DB属性
     */
    private int id;
    private String nick;
    private String head;
    private int roleId;
    private int exp;
    private JSONObject fightJson;//战力
    private int cueId;//球杆ID
    private int winNum;//赢球场次
    //当前可以进球的所有号码
    private List<GameBall> needBall = new ArrayList<>();
    //当前已经进的球
    private List<Integer> snookerList = new ArrayList<>();
    //抽牌
    private Set<Integer> set = new HashSet<>();
    //击球的单双 1-单色小球，2-双色大球
    private int side = 0;
    //再来一局 是否已经请求再来一局
    private int again;
    //犯规次数
    private int foul;
    //连击
    private int manyCue;
    //准备OK
    private boolean prepare =false;
    //击打黑8
    private boolean black = false;
    //当前局比赛结果
    private boolean win = false;
    //出杆次数
    private int cueNum;
    //进球个数
    protected int ballNum;
    //一杆清台,初始设置为true
    protected boolean clearance = true;
    //犯规判负
    private boolean foulFail;
    //当前杆是否需要同步
    private boolean needSyncGan = true;
    //是否强退
    private boolean isExit;
    //AI强度
    private boolean isStrongAI = false;

//    private int continueWin;
//    private int winChang;
//    private int totalChang;

//    public GameProto.GamePlayer.Builder toProto() {
//        GameProto.GamePlayer.Builder b = GameProto.GamePlayer.newBuilder();
//        b.setNick(this.nick)
//                .setPlayerId(this.id)
//                .setHead(this.head);
//        if( needBall.size()<=7) {
//            for(GameBall gb:needBall)
//                b.addBalls(gb.getNumber());
//        }
//        return b;
//    }

    public BilliardProto.GamePlayer.Builder toProto2(boolean isDivide) {
        BilliardProto.GamePlayer.Builder b = BilliardProto.GamePlayer.newBuilder();
        b.setNick(this.nick)
                .setRoleId(roleId)
                .setExp(exp)
                .setId(this.id)
                .setCueId(this.cueId)
                .setWinNum(winNum)
                .setFoul(foul)
                .setManyCue(manyCue)
                .setHead(this.head);
        b.addAllCards(set);


        if( needBall.size()<=7 && isDivide ) {
            for(GameBall gb:needBall) {
	            b.addBalls(gb.getNumber());
            }
        }
        return b;
    }

//    public void fromProto(GameProto.GamePlayer p) {
//        this.setId((int)p.getPlayerId());
//        this.setNick(p.getNick());
//        this.setHead(p.getHead());
////        this.setFight(p.getFight());
//    }

    public void fromMatchPlayer(MatchPlayer p, int aiRate) {
        this.id = p.getId();
        this.nick = p.getNick();
        this.roleId = p.getRoleId();
        this.exp = p.getExp();
        this.head = p.getHead();
        this.fightJson= p.getGameJson();
        this.cueId = p.getCueId();
        if( this.id < 0 ) {
	        this.isStrongAI = aiRate == 1;
        }
    }

    public void resetFoul() {
        this.foul = 0;
    }

    public void addFoul() {
        this.foul++;
    }

    public void resetManyCue(){
        this.manyCue = 0;
    }

    public void addManyCue(){
        this.manyCue++;
    }

    //每一杆打完后重置玩家数据
    public void resetGan() {
        this.needSyncGan = true;
    }

    public void reset() {
        this.again = 0;
        this.side = 0;
        this.cueNum = 0;
        this.black = false;
        this.win = false;
        this.foulFail = false;
        this.prepare = false;
        this.clearance = true;
        this.ballNum = 0;
        resetFoul();
        resetManyCue();
        needBall.clear();
        snookerList.clear();
        set.clear();
    }


    public  int remainDrawCard() {
        int remain = 0;
        for(GameBall gb: needBall) {
            for(int card:set) {
                int num;
                if( card%13 ==0 ) {
                    num = 13;
                } else {
                    num =  card==53?14:(card==54?15:(card%13));
                }
                if( gb.getNumber() == num ) {
                    remain++;
                }
            }
        }
        return remain;
    }
}
