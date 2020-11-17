package com.wangpo.billiard.bean;

import com.wangpo.base.bean.BilliardProto;
import lombok.Data;

import java.util.Date;

/**
 * 角色
 */
@Data
public class Role {

    /** 数据库id */
    private int id;
    /** 角色id，对应配置 */
    private int roleId;
    /** 玩家id */
    private int playerId;
    /** 是否使用 */
    private int isUse;
    /** 经验 */
    private int exp;
    private Date updateTime;
    private Date createTime;

    public BilliardProto.Role.Builder role2Proto() {
        return BilliardProto.Role.newBuilder()
                .setId(id)
                .setRoleId(roleId)
                .setPlayerId(playerId)
                .setIsUse(isUse)
                .setExp(exp);
    }

}
