package com.wangpo.platform.phone;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.PlatFormProto.C2S_Authentication;
import com.wangpo.base.bean.PlatFormProto.C2S_BingdingPhone;
import com.wangpo.base.bean.PlatFormProto.C2S_PhoneCode;
import com.wangpo.base.bean.PlatFormProto.S2C_Authentication;
import com.wangpo.base.bean.PlatFormProto.S2C_PhoneCodeSuccess;
import com.wangpo.base.bean.S2C;
import com.wangpo.platform.bean.Player;
import com.wangpo.platform.bean.TelCacheBean;
import com.wangpo.platform.enums.ErrorTipsEnum;
import com.wangpo.platform.pay.PayHandler;
import com.wangpo.platform.service.Cmd;
import com.wangpo.platform.service.PlayerMgr;
import com.wangpo.platform.service.PlayerService;
import com.wangpo.platform.util.HTTPUtil;
import com.wangpo.platform.util.TelVerify;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PhoneHandler {
	
    @Resource
    PlayerMgr playerMgr;
    
	@Resource
	PlayerService playerService;
	
	public S2C getSmsCode(C2S c2s) throws Exception {
		log.info("请求手机号绑定");
		int userId = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.BINGDING_PHONE);
		s2c.setUid(userId);
		C2S_BingdingPhone c2sPhone = C2S_BingdingPhone.parseFrom(c2s.getBody());
		String tel = c2sPhone.getPhone();
        //校验玩家是否已经绑定过电话了
		Player player = playerMgr.getPlayerByID(userId);
        if (player.getPhone() != null) {
            s2c.setCode(ErrorTipsEnum.TIPS_INFO1006.getTipsType());
            return s2c;
        }
        //检查这个手机号码是否已经被绑定过了
        Player playerTel = playerService.selectPlayerByPhone(tel);
        if (playerTel != null) {
            log.info("玩家{}绑定手机号{}时，发现该手机号已经被绑定过了！", userId, tel);
            s2c.setCode(ErrorTipsEnum.TIPS_INFO1007.getTipsType());
            return s2c;
        }
        //校验玩家是否刚发过校验码
        long nowTime = System.currentTimeMillis();
        if (nowTime - player.getTelCodeTime() < 60000) {
        	s2c.setCode(ErrorTipsEnum.TIPS_INFO1008.getTipsType());
            return s2c;
        }
        //生成随机码
        Random rd = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6; i++)
            sb.append(rd.nextInt(10));
        String randCode = sb.toString();
        if(!TelVerify.sendSms(randCode, tel, null)) {
        	return null;
        }
        //将ranCode缓存
    	TelCacheBean telBean = new TelCacheBean();
    	telBean.setRanCode(randCode);
    	telBean.setUserId(userId);
    	telBean.setTel(tel);
    	player.setTelCacheBean(telBean);
        //记录本次发码时间
        return null;
    }
	
	public static void main(String[] args) {
//		Random rd = new Random();
//        StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < 6; i++)
//            sb.append(rd.nextInt(10));
//        String randCode = sb.toString();
//        if(!TelVerify.sendSms(randCode, "18398334010", null)) {
//        	System.err.println("成功");
//        }else {
//        	System.err.println("失败");
//        }
	}
	
	public S2C getBindTel(C2S c2s) throws Exception {
		log.info("验证手机号");
		int uid = c2s.getUid();
		S2C s2c = new S2C();
		s2c.setCid(Cmd.PHONE_CODE);
		s2c.setUid(uid);
		C2S_PhoneCode c2sPhoneCode = C2S_PhoneCode.parseFrom(c2s.getBody());
		int userId = c2s.getUid();
        //校验玩家是否已经绑定过电话了
		Player player = playerMgr.getPlayerByID(userId);
        if (player.getPhone() != null) {
            s2c.setCode(ErrorTipsEnum.TIPS_INFO1006.getTipsType());
            return s2c;
        }
        //根据userid取出缓存中信息
        TelCacheBean tcb = player.getTelCacheBean();
        S2C_PhoneCodeSuccess.Builder s2cPhoneSucess = S2C_PhoneCodeSuccess.newBuilder();
        if (null != tcb) {
            if (!c2sPhoneCode.getPoneCode().equals(tcb.getRanCode())) {
                log.info("玩家{}绑定手机号校验失败！玩家传码{}，实际缓存码为{}", userId, c2sPhoneCode.getPoneCode(), tcb.getRanCode());
                s2c.setCode(ErrorTipsEnum.TIPS_INFO1009.getTipsType());
                return s2c;
            }
            log.info("验证成功");
            //校验成功，将手机号码换进入
            player.setPhone(tcb.getTel());
            playerService.updatePhoneById(userId, tcb.getTel());
            s2cPhoneSucess.setSuccess(1);
            s2c.setBody(s2cPhoneSucess.build().toByteArray());
            return s2c;
        } else {
        	log.info("没有验证码缓存");
        	s2c.setCode(3);
            s2c.setErrStr("绑定过手机号失败");
            return s2c;
        }
    }
	
	/**
	 * 实名认证
	 * @param c2s
	 * @return
	 * @throws Exception
	 */
	public S2C getAuthentication(C2S c2s) throws Exception {
		int userId = c2s.getUid();
        S2C s2c = new S2C();
        s2c.setCid(Cmd.AUTHENTICATION);
        s2c.setUid(userId);
		C2S_Authentication c2sAuthentication = C2S_Authentication.parseFrom(c2s.getBody());
		String name = c2sAuthentication.getName();
		String idCard = c2sAuthentication.getIdCard();
        Map<String, Object> data = new HashMap<>();
        data.put("app_id", PayHandler.appId);
        data.put("name", name);
        data.put("card", idCard);
        String sign = HTTPUtil.getNewSign(data, PayHandler.sign);
        data.put("sign", sign);
        String resStr = HTTPUtil.wxHttpPostRequest("http://kyspay.uybeliq.com/api/identity",
        		HTTPUtil.buildReqString(data));
        JSONObject response = JSONObject.parseObject(resStr);
        log.info("下单成功，下单返回参数：{}",response.toString());
        int returnCode = response.getIntValue("code");    //获取返回码
        //若返回码为SUCCESS，则会返回一个result_code,再对该result_code进行判断
        S2C_Authentication.Builder s2cAuthentication = S2C_Authentication.newBuilder();
        if (returnCode == 1) {
        	Player player = playerMgr.getPlayerByID(userId);
        	s2cAuthentication.setSuccess(1);
        	player.setIdcard(idCard);
        	player.setName(name);
        	playerService.updateIdcard(player);
        }else {
        	s2c.setCode(ErrorTipsEnum.TIPS_INFO1015.getTipsType());
        	return s2c;
        }
        s2c.setBody(s2cAuthentication.build().toByteArray());
		return s2c;
	}

}
