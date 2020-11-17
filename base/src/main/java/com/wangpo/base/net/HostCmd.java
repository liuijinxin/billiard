package com.wangpo.base.net;

public class HostCmd {
    /**
     * L2M，M2L
     * 登录服 -> 匹配服
     * 匹配服 -> 登录服
     */
    public static final short L2M_MATCH = 101;
    public static final short L2M_MATCH_PLAYER = 102;
    public static final short M2L_TIME_OUT = 151;

    /**
     * L2R，R2L
     * 登录服 -> 房间服
     * 房间服 -> 登录服
     */
    //请求房间初始化
    public static final short L2R_INIT_ROOM = 201;
    //再来一局
    public static final short L2R_NEW_ROUND = 202;
    //解散房间
    public static final short L2R_DISMISS = 203;
    //游戏结束
    public static final short R2L_GAME_OVER = 251;
    //球桌信息
    public static final short R2L_INIT_ROOM = 252;
    //当前操作玩家
    public static final short R2L_OPT_PLAYER = 253;
    public static final short R2L_NEW_ROOM_OK = 254;
    //落袋
    public static final short SNOOKER = 255;
    public static final short SYNC_POS = 256;
    //摆白球
    public static final short LAY_BALL = 257;
    //同步球桌信息
    public static final short DESK_INFO = 258;
    //玩家击球列表
    public static final short BALL_LIST = 259;
    //解散房间
    public static final short DISMISS = 260;

    /**
     * L2M，M2L
     * 匹配服 -> 房间服
     * 房间服 -> 匹配服
     */
    //匹配成功
    public static final short M2R_MATCH = 301;
    /**
     * 转发消息
     */
    public static final short TRANSFER = 401;
    public static final short LINK = 402;
}
