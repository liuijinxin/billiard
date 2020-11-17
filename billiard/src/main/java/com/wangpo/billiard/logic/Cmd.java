package com.wangpo.billiard.logic;

public class Cmd {
    /**
     * 客户端请求协议号，由1000开头
     * 请求和应答同一个协议号
     */
    //请求登录&应答
    public static final int LOGIN = 100;
    //请求匹配&应答
    public static final int MATCH = 1101;
    //进入房间请求&应答
    public static final int INIT_ROOM = 1102;
    //球杆移动&应答(仅转发)
    public static final int CUE_MOVE = 1103;
    //玩家击球&应答(仅转发)
    public static final int PLAYER_OPT = 1104;
    //台球落袋SNOOKER斯诺克
    public static final int SNOOKER = 1105;
    //同步台球位置，仅客户端用
    public static final int SYNC_POS = 1106;
    //空杆&应答
    public static final int EMPTY_ROD = 1107;
    //选球&应答
    public static final int SELECT_BALL = 1108;
    //再来一局
    public static final int NEW_ROUND = 1109;
    //离开游戏房间
    public static final int EXIT_ROOM = 1110;
    //同步台球位置,所有球静止后的同步，同时轮到下一个瓦纳基操作
    public static final int SYNC_POS2 = 1111;
    //玩家摆球，摆白球
    public static final int LAY_BALL = 1112;
    //同步球桌信息，球袋位置，球桌大小
    public static final int DESK_INFO = 1113;

    //请求加倍
    public static final int REQ_DOUBLE = 1114;
    //应答加倍
    public static final int RESP_DOUBLE = 1115;
    //台球信息
    public static final int BILLIARD_INFO = 1116;
    //玩家摆球，摆白球
    public static final int SLEEP_LAY_BALL = 1117;

    //我的球杆
    public static final int MY_CUE = 1150;
    //购买球杆
    public static final int BUY_CUE = 1151;
    //出售球杆
    public static final int SELL_CUE = 1152;
    //升级球杆
    public static final int UPGRADE_CUE = 1153;
    //使用球杆
    public static final int USE_CUE = 1154;
    //查看所有球杆
    public static final int ALL_CUE = 1155;
    //维护球杆
    public static final int DEFEND_CUE = 1156;

    //更新道具
    public static final int UPDATE_ITEM = 1158;
    //获取道具
    public static final int ALL_ITEM = 1159;
    //获取角色
    public static final int GET_ROLE = 1160;
    //使用角色
    public static final int USE_ROLE = 1161;
    //更新角色
    public static final int UPDATE_ROLE = 1162;
    //购买角色
    public static final int BUY_ROLE = 1163;

    //抽奖
    public static final int LOTTERY = 1164;

    //幸运一杆
    public static final int LUCKY_CUE = 1165;
    //逃跑
    public static final int EXIT_GAME = 1166;
    //发送表情
    public static final int EMOJI = 1167;

    //取消匹配
    public static final int CANCEL_MATCH = 1168;

    //维护球杆
    public static final int DAMAGE_CUE = 1169;
    //幸运一杆扣次数
    public static final int LUCKY_CUE_GO = 1170;
    
    //新手引导请求匹配
    public static final int NOCIVE_GUIDE_MATCH = 1171;
    //新手引导抽奖
    public static final int NOCIVE_GUIDE_LOTTERY = 1172;


    //请求策划配置
    public static final int REQ_CONFIG = 1201;

    /**
     * 服务器推送，由2000开头
     */
    //游戏开始
    public static final int GAME_START = 2001;
    //当前操作玩家
    public static final int OPT_PLAYER = 2002;
    //当前选球玩家
    public static final int SELECT_PLAYER = 2003;
    //匹配超时
    public static final int MATCH_TIME_OUT = 2004;
    //匹配成功
    public static final int MATCH_OK = 2005;
    //推送当前玩家击球列表
    public static final int BIG_SMALL = 2006;
    public static final int FRAME = 2007;
    //抽牌
    public static final int DRAW_CARD = 2008;
    //游戏结算
    public static final int GAME_SETTLE = 2010;
    //游戏公告
    public static final int Notice = 2011;

    //抽奖奖励
    public static final int LOTTERY_AWARD = 2012;
    //游戏次数
    public static final int GAME_TIMES = 2013;

    //首杆进黑八，直接重置。
    public static final int RESET_GAME = 2014;
    //更新幸运一杆数据
    public static final int LUCKY_CUE_DATA = 2015;
    //推送奖励
    public static final int S2C_AWARD = 2016;
    //推送表情
    public static final int S2C_EMOJI = 2017;
    //游戏中切换球杆
    public static final int CHANGE_CUE = 2018;


}
