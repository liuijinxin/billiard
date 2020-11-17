/**
 * @author Tony
 * @date 2018-01-10
 * @project rest_demo
 */
package com.wangpo.platform.util;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TelVerify {

    private static final Logger logger = LoggerFactory.getLogger(TelVerify.class);

    static AbsRestClient InstantiationRestAPI() {
        return new JsonReqClient();
    }

    public static boolean sendSms(String param, String mobile, String uid) {
        try {
            String result = InstantiationRestAPI().sendSms(param, mobile, uid);
            logger.info("Response content is: " + result);
            return true;
        } catch (Exception e) {
            logger.error("发送短信验证码失败！", e);
            return false;
        }
    }

    public static boolean sendServerRemindSms(String param, String mobile, String uid) {
        try {
            String result = InstantiationRestAPI().sendServerRemindSms(param, mobile, uid);
            logger.info("Response content is: " + result);
            return true;
        } catch (Exception e) {
            logger.error("发送短信验证码失败！", e);
            return false;
        }
    }

    public static void testSendSmsBatch(String sid, String token, String appid, String templateid, String param, String mobile, String uid) {
        try {
            String result = InstantiationRestAPI().sendSmsBatch(sid, token, appid, templateid, param, mobile, uid);
            System.out.println("Response content is: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testAddSmsTemplate(String sid, String token, String appid, String type, String template_name, String autograph, String content) {
        try {
            String result = InstantiationRestAPI().addSmsTemplate(sid, token, appid, type, template_name, autograph, content);
            System.out.println("Response content is: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void testGetSmsTemplate(String sid, String token, String appid, String templateid, String page_num, String page_size) {
        try {
            String result = InstantiationRestAPI().getSmsTemplate(sid, token, appid, templateid, page_num, page_size);
            System.out.println("Response content is: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void testEditSmsTemplate(String sid, String token, String appid, String templateid, String type, String template_name, String autograph, String content) {
        try {
            String result = InstantiationRestAPI().editSmsTemplate(sid, token, appid, templateid, type, template_name, autograph, content);
            System.out.println("Response content is: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void testDeleterSmsTemplate(String sid, String token, String appid, String templateid) {
        try {
            String result = InstantiationRestAPI().deleterSmsTemplate(sid, token, appid, templateid);
            System.out.println("Response content is: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试说明  启动main方法后，请在控制台输入数字(数字对应 相应的调用方法)，回车键结束
     * 参数名称含义，请参考rest api 文档
     *
     * @throws IOException
     * @method main
     */
    public static void main(String[] args) throws IOException {

        System.out.println("请输入方法对应的数字(例如1),Enter键结束:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String methodNumber = br.readLine();

        if (StringUtils.isBlank(methodNumber)) {
            System.out.println("请输入正确的数字，不可为空");
            return;
        }

        if (methodNumber.equals("1")) {  //指定模板单发
            String sid = "c5d4d176bab3446989bccea827117a8c";
            String token = "a73df12e32acf8b94c7ba7554f1198ea";
            String appid = "2cd504d41f794770a81b06245fcdb6d6";
            String templateid = "483121";
            String param = "你好,他好";
            String mobile = "18030772152";
            String uid = "111111";
            sendSms(param, mobile, null);
        } else if (methodNumber.equals("2")) { //指定模板群发
            String sid = "";
            String token = "";
            String appid = "";
            String templateid = "";
            String param = "";
            String mobile = "";
            String uid = "";
            testSendSmsBatch(sid, token, appid, templateid, param, mobile, uid);
        } else if (methodNumber.equals("3")) {  //增加模板
            String sid = "";
            String token = "";
            String appid = "";
            String type = "";
            String template_name = "";
            String autograph = "";
            String content = "";
            testAddSmsTemplate(sid, token, appid, type, template_name, autograph, content);
        } else if (methodNumber.equals("4")) {  //查询模板
            String sid = "";
            String token = "";
            String appid = "";
            String templateid = "";
            String page_num = "";
            String page_size = "";
            testGetSmsTemplate(sid, token, appid, templateid, page_num, page_size);
        } else if (methodNumber.equals("5")) {  //编辑模板
            String sid = "";
            String token = "";
            String appid = "";
            String templateid = "";
            String type = "";
            String template_name = "";
            String autograph = "";
            String content = "";
            testEditSmsTemplate(sid, token, appid, templateid, type, template_name, autograph, content);
        } else if (methodNumber.equals("6")) {  //删除模板
            String sid = "";
            String token = "";
            String appid = "";
            String templateid = "";
            testDeleterSmsTemplate(sid, token, appid, templateid);
        }
    }
}
