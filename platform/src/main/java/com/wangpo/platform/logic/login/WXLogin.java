package com.wangpo.platform.logic.login;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class WXLogin {
	public static final String APPID = "wx004ea14efad44abc";
	public static final String AppSecret = "4e7abe4ebc2b502ef1427b676474f217";

	@Resource
	AuthUtil authUtil;
	//TODO 刷新token？
	public JSONObject weChatLogin(String code) throws Exception{
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + AuthUtil.APPID + "&secret="
				+ AuthUtil.APPSECRET + "&code=" + code + "&grant_type=authorization_code";
		JSONObject jsonObject = authUtil.doGetJson(url);
		if( !jsonObject.containsKey("access_token")){
			log.error("微信获取access_token错误，{}",jsonObject);
			return null;
		}
		String openid = jsonObject.getString("openid");
		String token = jsonObject.getString("access_token");
		String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + token + "&openid=" + openid
				+ "&lang=zh_CN";
		jsonObject =  authUtil.doGetJson(infoUrl);
		if( !jsonObject.containsKey("openid")){
			log.error("微信获取用户信息错误，{}",jsonObject);
			return null;
		}
		jsonObject.put("access_token",token);
		return jsonObject;
	}
}
