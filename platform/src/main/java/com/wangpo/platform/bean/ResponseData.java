package com.wangpo.platform.bean;

import lombok.Data;

@Data
public class ResponseData {
	
	private String code;
	
	private String msg;

	public ResponseData(String code, String msg) {
		super();
		this.code = code;
		this.msg = msg;
	}
	
	

}
