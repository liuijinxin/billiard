package com.wangpo.platform.util;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.util.DigestUtils;

public class HTTPUtil {

    /**
     * HTTP GET请求
     *
     * @param strUrl
     * @return
     */
    public static String wxHttpGetRequest(String strUrl) throws Exception {
        String resp = null;
        URL httpUrl = new URL(strUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(8000);
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.connect();

        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
        StringBuffer stringBuffer = new StringBuffer();
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        resp = stringBuffer.toString();
        if (stringBuffer != null) {
            try {
                bufferedReader.close();
            } catch (IOException var18) {
                var18.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException var17) {
                var17.printStackTrace();
            }
        }
        return resp;

    }

    /**
     * HTTP POST请求
     *
     * @param strUrl
     * @return
     */
    public static String wxHttpPostRequest(String strUrl, String reqBody) {
        String resp = null;
        try {
            URL httpUrl = new URL(strUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(reqBody.getBytes("UTF8"));
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            resp = stringBuffer.toString();
            if (stringBuffer != null) {
                try {
                    bufferedReader.close();
                } catch (IOException var18) {
                    var18.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException var17) {
                    var17.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException var16) {
                    var16.printStackTrace();
                }
            }
        } catch (Exception e) {

        }
        return resp;
    }
    
    public static String getNewSign(Map<String, Object> data, String key) {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            sb.append(k).append("=").append(data.get(k)).append("&");
        }
        sb.append("secret=").append(key);
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }
    
	public static String buildReqString(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keyIterator = data.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            sb.append(key).append("=").append(data.get(key)).append("&");
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        return str;
    }
}
