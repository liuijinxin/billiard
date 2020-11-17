package com.wangpo.platform.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.Mail;
import com.wangpo.base.kits.FormatKit;
import com.wangpo.platform.bean.ResponseData;
import com.wangpo.platform.logic.mail.MailMgr;

@Controller
@RequestMapping("/mail")
public class MailController {
	
	@Resource
	MailMgr mailMgr;
	
    /**
     * 发送邮件
     */
    @RequestMapping(value = "/send")
    @ResponseBody
    public ResponseData add(Mail mail) {
		if (mail.getEndTime() == null) {
			mail.setEndTime(FormatKit.nextYears(10));
		}
		//判断是系统邮件，还是个人邮件
		if ("".equals(mail.getPlayerIds())) {
			mailMgr.addSystemMail(mail);
		} else {
			mailMgr.addPersonalMail(mail);
		}
		ResponseData res = new ResponseData("200","发送成功");
		return res;
    }
    
    /**
     * 发送邮件
     */
    @RequestMapping(value = "/sendMail")
    @ResponseBody
    public ResponseData add(@RequestParam(value = "id", required = false) String id,@RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "content", required = false) String content,@RequestParam(value = "item", required = false) String item,
                            @RequestParam(value = "endTime", required = false) Date endTime) {
        Mail mail = new Mail();
        mail.setPlayerIds(id);
        mail.setTitle(title);
        mail.setContent(content);
        mail.setItems(item);
        mail.setEndTime(endTime);
        if (mail.getEndTime() == null) {
			mail.setEndTime(FormatKit.nextYears(10));
		}
		//判断是系统邮件，还是个人邮件
		if ("".equals(mail.getPlayerIds())) {
			mailMgr.addSystemMail(mail);
		} else {
			mailMgr.addPersonalMail(mail);
		}
		ResponseData res = new ResponseData("200","发送成功");
		return res;
    }

}
