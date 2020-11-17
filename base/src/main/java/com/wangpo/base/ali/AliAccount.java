package com.wangpo.base.ali;


import java.io.Serializable;

import lombok.Data;

@Data
public class AliAccount implements Serializable {
	
	private String account;
	
	private String name;

}
