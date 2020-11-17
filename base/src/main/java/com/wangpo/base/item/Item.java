package com.wangpo.base.item;

import lombok.Data;

@Data
public class Item {
	//道具唯一ID
	private int id;
	//道具配置ID
	private int modelId;
	//所属游戏ID，3-台球
	private int game;
	//道具数量
	private int num;
}
