package com.wangpo.base.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;


@Data
public class Mail implements Serializable {
	private long id;
	private int playerId;
	private int systemId;
	private int mailType;
	private int mailState;
	private String title;
	private String content;
	private long time;
	private JSONObject item = new JSONObject();
	private Date createTime;
	private Date endTime;

	//后台管理系统发送过来的字段
	private String playerIds;
	private String items;

	public PlatFormProto.Mail.Builder mail2Proto() {
		PlatFormProto.Mail.Builder bb = PlatFormProto.Mail.newBuilder()
				.setMailId(id)
				.setTitle(title)
				.setContent(content)
				.setTime(time)
				.setMailState(mailState);
		if (item != null) {
			for (Map.Entry<String, Object> entry : item.entrySet()) {
				PlatFormProto.Award.Builder builder = PlatFormProto.Award.newBuilder();
				builder.setId(Integer.parseInt(entry.getKey())).setNum(Integer.parseInt(String.valueOf(entry.getValue())));
				bb.addAwards(builder.build());
			}
		}
		return bb;
	}

}
