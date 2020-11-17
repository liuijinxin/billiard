package com.wangpo.base.kits;

import lombok.Data;

@Data
public class BindTwo<T> {
	private T obj1;
	private T obj2;
	public BindTwo(T o1,T o2) {
		this.obj1 = o1;
		this.obj2 = o2;
	}
	public BindTwo(){

	}
}
