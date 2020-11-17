package com.wangpo.platform.logic.login;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@Slf4j
public class AuthUtil {
	//成都
	public static final String APPID = "wx004ea14efad44abc";
	public static final String APPSECRET = "4e7abe4ebc2b502ef1427b676474f217";
	public JSONObject doGetJson(String URL) throws IOException {
		JSONObject jsonObject = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		BufferedReader br = null;
		StringBuilder result = new StringBuilder();
		try  {
			//创建远程url连接对象
			java.net.URL url = new URL(URL);
			//通过远程url连接对象打开一个连接，强转成HTTPURLConnection类
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			//设置连接超时时间和读取超时时间
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(60000);
			conn.setRequestProperty("Accept", "application/json");
			//发送请求
			conn.connect();
			//通过conn取得输入流，并使用Reader读取
			if (200 == conn.getResponseCode()) {
				is = conn.getInputStream();
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line;
				while ((line = br.readLine()) != null) {
					result.append(line);
				}
			} else {
				log.error("获取链接错误异常，错误码：{}，链接：{}",conn.getResponseCode(),URL);
//				System.out.println("ResponseCode is an error code:" + conn.getResponseCode());
			}
		} catch (Exception e) {
			log.error("doGetJson error，url:{}",URL,e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			conn.disconnect();
		}
		jsonObject = JSONObject.parseObject(result.toString());
		return jsonObject;
	}
}
