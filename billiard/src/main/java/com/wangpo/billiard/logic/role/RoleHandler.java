package com.wangpo.billiard.logic.role;

import com.wangpo.base.service.BilliardPushService;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.enums.GlobalEnum;
import com.wangpo.base.excel.GlobalConfig;
import com.wangpo.base.excel.BilliardRoleConfig;
import com.wangpo.billiard.bean.Player;
import com.wangpo.billiard.bean.Role;
import com.wangpo.billiard.excel.ExcelMgr;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.logic.PlayerMgr;
import com.wangpo.billiard.logic.player.PlayerHandler;
import com.wangpo.billiard.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class RoleHandler {
    @Resource
    PlayerMgr playerMgr;
    @Resource
    ExcelMgr excelMgr;
    @Resource
    PlayerHandler playerHandler;
    @Resource
    RoleService roleService;
    @Resource
    BilliardPushService billiardPushService;

    /**
     * 获取角色列表
     */
    public S2C getRole(C2S c2s) {
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.GET_ROLE);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setCode(1);
            return s2c;
        }
        List<Role> roleList = player.getRoleList();
        if (roleList.size() < 2) {
            GlobalConfig globalConfig = excelMgr.getGlobal(GlobalEnum.ROLE_ID.code);
            int roleId = 1001;
            if (globalConfig != null) {
                roleId = globalConfig.intValue();
            }
            BilliardRoleConfig roleConfig = excelMgr.getRoleById(1001);
            addNewRole(player, roleConfig, roleId);
            BilliardRoleConfig roleConfig1 = excelMgr.getRoleById(2001);
            addNewRole(player, roleConfig1, roleId);
        }
        BilliardProto.S2C_getRole.Builder builder = BilliardProto.S2C_getRole.newBuilder();
        for (Role role : roleList) {
            builder.addRole(role.role2Proto().build());
        }
        s2c.setBody(builder.build().toByteArray());
        return s2c;
    }

    /**
     * 使用角色
     */
    public S2C useRole(C2S c2s) throws Exception {
        BilliardProto.C2S_UseRole builder = BilliardProto.C2S_UseRole.parseFrom(c2s.getBody());
        int id = builder.getId();
        int uid = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.USE_ROLE);
        s2c.setUid(uid);
        Player player = playerMgr.getPlayerByID(uid);
        if (player == null) {
            log.error("玩家不存在");
            s2c.setCode(1);
            return s2c;
        }
        List<Role> roleList = player.getRoleList();
        Role role = null;
        for (Role playerRole : roleList) {
            if (id == playerRole.getId()) {
                role = playerRole;
                break;
            }
        }
        if (role == null) {
            log.error("玩家未拥有该角色");
            s2c.setCode(2);
            return s2c;
        }
        if (role.getIsUse() == 1) {
            log.error("角色已使用");
            s2c.setCode(3);
            return s2c;
        } else {
            role.setIsUse(1);
            roleService.updateRole(role);
        }
        for (Role playerRole : roleList) {
            if (role.getId() != playerRole.getId() && playerRole.getIsUse() == 1) {
                playerRole.setIsUse(0);
                roleService.updateRole(playerRole);
            }
        }
        BilliardProto.S2C_UseRole.Builder useRole = BilliardProto.S2C_UseRole.newBuilder();
        useRole.setId(id);
        s2c.setBody(useRole.build().toByteArray());
        return s2c;
    }

//    /**
//     * 购买角色
//     */
//    public S2C buyRole(C2S c2s) throws Exception {
//        BilliardProto.C2S_buyRole buyRole = BilliardProto.C2S_buyRole.parseFrom(c2s.getBody());
//        int roleId = buyRole.getRoleId();
//        int uid = c2s.getUid();
//        S2C s2c = new S2C();
//        s2c.setCid(Cmd.BUY_ROLE);
//        s2c.setUid(uid);
//        Player player = playerMgr.getPlayerByID(uid);
//        if (player == null) {
//            log.error("玩家不存在");
//            s2c.setCode(1);
//            return s2c;
//        }
//        Role role = excelMgr.getRoleById(roleId);
//        if (role == null) {
//            log.error("角色不存在");
//            s2c.setCode(2);
//            return s2c;
//        }
//        boolean flag = playerHandler.modifyPlayerGold(player,s2c,role.getBuyType(), -role.getBuyPrice(),true, GameEventEnum.BUY_ROLE);
//        if (flag) return s2c;
//
//        PlayerRole playerRole = new PlayerRole();
//        playerRole.setPlayerId(role.getId());
//        playerRole.setRoleId(role.getId());
//        playerRole.setExp(role.getExp());
//        playerRole.setIsUse(0);
//        roleService.insertRole(playerRole);
//        player.getRoleList().add(playerRole);
//
//        BilliardProto.S2C_buyRole.Builder builder = BilliardProto.S2C_buyRole.newBuilder();
//        builder.setRole(playerRole.role2Proto().build());
//        s2c.setBody(builder.build().toByteArray());
//        return s2c;
//    }


    /**
     * 添加新角色
     * @param player 玩家
     * @param roleConfig 角色配置
     */
    private void addNewRole(Player player, BilliardRoleConfig roleConfig, int roleId) {
        Role role = new Role();
        role.setPlayerId(player.getId());
        role.setRoleId(roleConfig.getId());
        role.setExp(roleConfig.getRoleExp());
        if (roleConfig.getId() == roleId) {
            role.setIsUse(1);
        } else {
            role.setIsUse(0);
        }
        roleService.insertRole(role);
        player.getRoleList().add(role);
    }

    /**
     * 更新角色
     * @param player 玩家
     * @param exp 经验
     */
    public void updateRole(Player player, int exp) {
        if (player != null) {
            List<Role> roleList = player.getRoleList();
            Role playerRole = useRole(player);
            if (playerRole != null) {
                int nowExp = playerRole.getExp() + exp;
                playerRole.setExp(nowExp);
                BilliardRoleConfig roleConfig = excelMgr.getRoleById(playerRole.getRoleId() + 1);
                if (roleConfig != null) {
                    //判断角色经验是否大于
                    if (nowExp >= roleConfig.getRoleExp()) {
                        playerRole.setExp(nowExp);
                        playerRole.setRoleId(roleConfig.getId());
                    }
                }
                roleService.updateRole(playerRole);
                BilliardProto.S2C_updateRole.Builder builder = BilliardProto.S2C_updateRole.newBuilder();
                builder.setRole(playerRole.role2Proto().build());
                S2C s2c = new S2C();
                s2c.setCid(Cmd.UPDATE_ROLE);
                s2c.setUid(player.getId());
                s2c.setBody(builder.build().toByteArray());
                billiardPushService.push(s2c);
            }
        }
    }

    /**
     * 查看玩家使用的角色
     * @param player 玩家
     * @return 使用的角色
     */
    public Role useRole(Player player) {
        Role playerRole = null;
        for (Role role : player.getRoleList()) {
            if (role.getIsUse() == 1) {
                playerRole = role;
            }
        }
        return playerRole;
    }

}
