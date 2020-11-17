package com.wangpo.platform.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ResourceBundle;

public class SysConfig {
	private final String is_test = "true";
	private final String rest_server = "open.ucpaas.com";
	private final String sid = "c5d4d176bab3446989bccea827117a8c";
	private final String token="a73df12e32acf8b94c7ba7554f1198ea";
	private final String appid="2cd504d41f794770a81b06245fcdb6d6";
	private final String templateid="483121";
	private final String time="120";
//	private final ResourceBundle props = null;// config.properties
	private static volatile SysConfig conf;
	private static JSONObject json;

	private SysConfig() {
		loadConfigProps();
	}

	public static SysConfig getInstance() {
		if (conf == null) {
			synchronized (SysConfig.class) {
				if (conf == null) {
					conf = new SysConfig();
				}
			}
		}
		return conf;
	}

	private void loadConfigProps() {
//		props = ResourceBundle.getBundle("telconf");
	}

	public String getProperty(String key) {
		String tmp = "";
		if(key.equals("is_test")) {
			tmp = is_test;
		}else if(key.equals("rest_server")) {
			tmp = rest_server;
		}else if(key.equals("sid")) {
			tmp = sid;
		}else if(key.equals("token")) {
			tmp = token;
		}else if(key.equals("appid")) {
			tmp = appid;
		}else if(key.equals("templateid")) {
			tmp = templateid;
		}else if(key.equals("time")) {
			tmp = time;
		}
		if (StringUtils.isNotEmpty(tmp)) {
			return tmp.trim();
		}
		return tmp;
	}

	public String getSysJson(String randId, String tel) {
		JSONObject jsonObject;
		if (null == json) {
			json = new JSONObject();
			json.put("sid", sid);
			json.put("token", token);
			json.put("appid", appid);
			json.put("templateid", templateid);
		}
		jsonObject = (JSONObject) json.clone();
		jsonObject.put("param", randId + "," + time);
		jsonObject.put("mobile", tel);
		return jsonObject.toJSONString();
	}

	public String getServerRemindSysJson(String message, String tel) {
		JSONObject jsonObject;
		if (null == json) {
			json = new JSONObject();
			json.put("sid", sid);
			json.put("token", token);
			json.put("appid", appid);
			json.put("templateid", 558744);
		}
		jsonObject = (JSONObject) json.clone();
		jsonObject.put("param", message);
		jsonObject.put("mobile", tel);
		return jsonObject.toJSONString();
	}
}
