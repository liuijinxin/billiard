package com.wangpo.platform.service;


import com.alibaba.fastjson.JSONObject;
import com.wangpo.platform.bean.Player;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlayerService {

    Player selectPlayerById(int id);

    Player selectPlayerByToken(String token);
    Player selectPlayerByOpenid(String openId);
    Player selectPlayerByPhone(String phone);

    int insertPlayer(Player player);

    int updatePlayer(Player player);

    int updateGoldById(int id,int goldNum);

    int updateDiamondById(int id,int diamondNum);

    int updateRedPacketById(int id,int redPacketNum);

    int updateAlipayById(int id,JSONObject alipay);

    int updateIdcard(Player player);
    
    int updatePhoneById(int id,String phone);

}
